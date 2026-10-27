package com.decibeltracker.app.audio

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.SystemClock
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LiveMeasurement(
    val currentDb: Float = 0f,
    val minDb: Float = 0f,
    val maxDb: Float = 0f,
    val avgDb: Float = 0f,
    val timestampMillis: Long = System.currentTimeMillis(),
    val elapsedRealtimeMillis: Long = SystemClock.elapsedRealtime(),
    val isMeasuring: Boolean = false,
    val errorMessage: String? = null
)

class AudioMeasurementEngine(
    private val scope: CoroutineScope
) {
    private val _liveMeasurement = MutableStateFlow(LiveMeasurement())
    val liveMeasurement: StateFlow<LiveMeasurement> = _liveMeasurement.asStateFlow()

    // High-frequency sample flow for recording persistence and graph points
    private val _sampleFlow = MutableSharedFlow<MeasurementSamplePoint>(extraBufferCapacity = 64)
    val sampleFlow: SharedFlow<MeasurementSamplePoint> = _sampleFlow.asSharedFlow()

    data class MeasurementSamplePoint(
        val timestampMillis: Long,
        val elapsedRealtimeMillis: Long,
        val levelDb: Float
    )

    var calibrationOffset: Float = SoundLevelCalculator.DEFAULT_CALIBRATION_OFFSET
        set(value) {
            field = value
        }

    private var measurementJob: Job? = null
    private var audioRecord: AudioRecord? = null

    // Session energy accumulator for session min/max/Leq
    private val energyAccumulator = SoundLevelCalculator.EnergyAccumulator()
    private var smoothedLevel: Float = 0f

    @Volatile
    private var isRunning = false

    @SuppressLint("MissingPermission")
    fun startMeasurement() {
        if (isRunning) return
        isRunning = true
        energyAccumulator.reset()
        smoothedLevel = 0f

        measurementJob = scope.launch(Dispatchers.Default) {
            runMeasurementLoop()
        }
    }

    fun stopMeasurement() {
        isRunning = false
        measurementJob?.cancel()
        measurementJob = null
        releaseAudioRecord()
        _liveMeasurement.value = _liveMeasurement.value.copy(
            isMeasuring = false
        )
    }

    fun resetSessionStats() {
        energyAccumulator.reset()
        smoothedLevel = 0f
        val current = _liveMeasurement.value
        _liveMeasurement.value = current.copy(
            minDb = current.currentDb,
            maxDb = current.currentDb,
            avgDb = current.currentDb
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun runMeasurementLoop() = withContext(Dispatchers.IO) {
        val sampleRates = intArrayOf(44100, 48000, 22050, 16000)
        var record: AudioRecord? = null
        var selectedBufferSize = 0

        for (sampleRate in sampleRates) {
            val minBuf = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            if (minBuf > 0) {
                // Buffer size 2048 or 4096 samples
                val bufSize = maxOf(minBuf * 2, 4096)
                try {
                    val candidate = AudioRecord(
                        MediaRecorder.AudioSource.MIC,
                        sampleRate,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufSize
                    )
                    if (candidate.state == AudioRecord.STATE_INITIALIZED) {
                        record = candidate
                        selectedBufferSize = bufSize
                        break
                    } else {
                        candidate.release()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "AudioRecord init failed for sampleRate $sampleRate", e)
                }
            }
        }

        if (record == null || record.state != AudioRecord.STATE_INITIALIZED) {
            _liveMeasurement.value = _liveMeasurement.value.copy(
                isMeasuring = false,
                errorMessage = "Microphone initialization failed. Please check permissions or device availability."
            )
            isRunning = false
            return@withContext
        }

        audioRecord = record

        try {
            record.startRecording()
        } catch (e: Exception) {
            _liveMeasurement.value = _liveMeasurement.value.copy(
                isMeasuring = false,
                errorMessage = "Cannot start audio recording: ${e.message}"
            )
            releaseAudioRecord()
            isRunning = false
            return@withContext
        }

        _liveMeasurement.value = _liveMeasurement.value.copy(
            isMeasuring = true,
            errorMessage = null
        )

        val readBuffer = ShortArray(2048)
        var lastEmitTime = 0L
        val emitIntervalMs = 50L // ~20 Hz updates for graph and display

        try {
            while (isActive && isRunning) {
                val readCount = record.read(readBuffer, 0, readBuffer.size)

                if (readCount > 0) {
                    val rms = SoundLevelCalculator.computeRms(readBuffer, readCount)
                    val dbFs = SoundLevelCalculator.rmsToDbFs(rms)
                    val rawEstimatedSpl = SoundLevelCalculator.dbFsToEstimatedSpl(dbFs, calibrationOffset)

                    // Exponential smoothing for human display readout stability
                    smoothedLevel = if (smoothedLevel <= 0f) {
                        rawEstimatedSpl
                    } else {
                        (smoothedLevel * 0.65f) + (rawEstimatedSpl * 0.35f)
                    }

                    val nowWallClock = System.currentTimeMillis()
                    val nowMono = SystemClock.elapsedRealtime()

                    energyAccumulator.addSample(rawEstimatedSpl)

                    // Emit to sample flow for recordings and graph updates
                    _sampleFlow.tryEmit(
                        MeasurementSamplePoint(
                            timestampMillis = nowWallClock,
                            elapsedRealtimeMillis = nowMono,
                            levelDb = rawEstimatedSpl
                        )
                    )

                    // Throttle UI StateFlow updates to ~20 Hz
                    if (nowMono - lastEmitTime >= emitIntervalMs) {
                        lastEmitTime = nowMono
                        val currentLeq = energyAccumulator.currentLeq() ?: smoothedLevel
                        _liveMeasurement.value = LiveMeasurement(
                            currentDb = smoothedLevel,
                            minDb = energyAccumulator.min.takeIf { it != Float.MAX_VALUE } ?: smoothedLevel,
                            maxDb = energyAccumulator.max.takeIf { it != Float.MIN_VALUE } ?: smoothedLevel,
                            avgDb = currentLeq,
                            timestampMillis = nowWallClock,
                            elapsedRealtimeMillis = nowMono,
                            isMeasuring = true,
                            errorMessage = null
                        )
                    }
                } else if (readCount == AudioRecord.ERROR_INVALID_OPERATION || readCount == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "AudioRecord read error code: $readCount")
                    delay(100)
                }
            }
        } catch (e: CancellationException) {
            // Normal coroutine cancellation
        } catch (e: Exception) {
            Log.e(TAG, "Error in audio measurement loop", e)
            _liveMeasurement.value = _liveMeasurement.value.copy(
                errorMessage = "Measurement error: ${e.message}"
            )
        } finally {
            releaseAudioRecord()
        }
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.let {
                if (it.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    it.stop()
                }
                it.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception releasing AudioRecord", e)
        } finally {
            audioRecord = null
        }
    }

    companion object {
        private const val TAG = "AudioMeasurementEngine"
    }
}
