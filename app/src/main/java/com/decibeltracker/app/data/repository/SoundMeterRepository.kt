package com.decibeltracker.app.data.repository

import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.decibeltracker.app.audio.AudioMeasurementEngine
import com.decibeltracker.app.audio.LiveMeasurement
import com.decibeltracker.app.audio.SoundLevelCalculator
import com.decibeltracker.app.data.db.AppDatabase
import com.decibeltracker.app.data.db.MeasurementSampleEntity
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.data.model.RecordingStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ActiveRecordingState(
    val isRecording: Boolean = false,
    val recordingId: Long? = null,
    val title: String = "",
    val startWallClockMillis: Long = 0L,
    val elapsedDurationMillis: Long = 0L,
    val sampleCount: Long = 0L,
    val currentLeq: Float? = null,
    val minLevel: Float? = null,
    val maxLevel: Float? = null
)

class SoundMeterRepository(
    private val context: Context,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val audioEngine = AudioMeasurementEngine(scope)

    private val recordingDao = database.recordingDao()
    private val sampleDao = database.sampleDao()

    private val _activeRecordingState = MutableStateFlow(ActiveRecordingState())
    val activeRecordingState: StateFlow<ActiveRecordingState> = _activeRecordingState.asStateFlow()

    val liveMeasurement: StateFlow<LiveMeasurement> = audioEngine.liveMeasurement

    private var sampleCollectorJob: Job? = null
    private var durationTimerJob: Job? = null
    private val sampleBuffer = mutableListOf<MeasurementSampleEntity>()
    private val bufferMutex = Mutex()
    private var sessionStartElapsedRealtime = 0L
    private val sessionEnergyAccumulator = SoundLevelCalculator.EnergyAccumulator()

    fun startLiveMeasurement() {
        audioEngine.startMeasurement()
    }

    fun stopLiveMeasurement() {
        audioEngine.stopMeasurement()
    }

    fun setCalibrationOffset(offset: Float) {
        audioEngine.calibrationOffset = offset
    }

    fun getCalibrationOffset(): Float = audioEngine.calibrationOffset

    suspend fun startRecording(customTitle: String? = null): Long = withContext(Dispatchers.IO) {
        if (_activeRecordingState.value.isRecording) {
            return@withContext _activeRecordingState.value.recordingId ?: -1L
        }

        startLiveMeasurement()

        val nowWallClock = System.currentTimeMillis()
        sessionStartElapsedRealtime = SystemClock.elapsedRealtime()
        sessionEnergyAccumulator.reset()

        val title = customTitle?.takeIf { it.isNotBlank() } ?: run {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            "Recording " + dateFormat.format(Date(nowWallClock))
        }

        val recordingEntity = RecordingEntity(
            title = title,
            startTimeEpochMillis = nowWallClock,
            status = RecordingStatus.RECORDING,
            calibrationOffset = audioEngine.calibrationOffset
        )

        val recordingId = recordingDao.insertRecording(recordingEntity)

        _activeRecordingState.value = ActiveRecordingState(
            isRecording = true,
            recordingId = recordingId,
            title = title,
            startWallClockMillis = nowWallClock,
            elapsedDurationMillis = 0L,
            sampleCount = 0L
        )

        startSampleCollection(recordingId)
        startDurationTimer()

        Log.d(TAG, "Started recording session $recordingId: $title")
        return@withContext recordingId
    }

    private fun startSampleCollection(recordingId: Long) {
        sampleCollectorJob?.cancel()
        sampleCollectorJob = scope.launch(Dispatchers.IO) {
            var lastFlushTime = SystemClock.elapsedRealtime()

            audioEngine.sampleFlow.collect { samplePoint ->
                if (!_activeRecordingState.value.isRecording) return@collect

                val elapsedFromSessionStart = samplePoint.elapsedRealtimeMillis - sessionStartElapsedRealtime
                val sampleEntity = MeasurementSampleEntity(
                    recordingId = recordingId,
                    elapsedMillis = elapsedFromSessionStart,
                    timestampEpochMillis = samplePoint.timestampMillis,
                    levelValue = samplePoint.levelDb
                )

                sessionEnergyAccumulator.addSample(samplePoint.levelDb)

                bufferMutex.withLock {
                    sampleBuffer.add(sampleEntity)
                    val now = SystemClock.elapsedRealtime()
                    if (sampleBuffer.size >= BATCH_SIZE || (now - lastFlushTime >= FLUSH_INTERVAL_MS && sampleBuffer.isNotEmpty())) {
                        flushBufferLocked()
                        lastFlushTime = now
                    }
                }

                _activeRecordingState.value = _activeRecordingState.value.copy(
                    sampleCount = sessionEnergyAccumulator.count,
                    currentLeq = sessionEnergyAccumulator.currentLeq(),
                    minLevel = sessionEnergyAccumulator.min.takeIf { it != Float.MAX_VALUE },
                    maxLevel = sessionEnergyAccumulator.max.takeIf { it != Float.MIN_VALUE }
                )
            }
        }
    }

    private fun startDurationTimer() {
        durationTimerJob?.cancel()
        durationTimerJob = scope.launch(Dispatchers.Default) {
            while (isActive && _activeRecordingState.value.isRecording) {
                delay(200L)
                val duration = SystemClock.elapsedRealtime() - sessionStartElapsedRealtime
                _activeRecordingState.value = _activeRecordingState.value.copy(
                    elapsedDurationMillis = duration
                )
            }
        }
    }

    suspend fun stopRecording(): RecordingEntity? = withContext(Dispatchers.IO) {
        val currentState = _activeRecordingState.value
        if (!currentState.isRecording || currentState.recordingId == null) {
            return@withContext null
        }

        val recordingId = currentState.recordingId
        val endWallClock = System.currentTimeMillis()
        val duration = SystemClock.elapsedRealtime() - sessionStartElapsedRealtime

        // Stop collectors
        sampleCollectorJob?.cancel()
        sampleCollectorJob = null
        durationTimerJob?.cancel()
        durationTimerJob = null

        // Flush remaining buffer
        bufferMutex.withLock {
            if (sampleBuffer.isNotEmpty()) {
                flushBufferLocked()
            }
        }

        // Finalize statistics directly from persisted samples
        val minMax = sampleDao.getMinMaxStats(recordingId)
        val allLevels = sampleDao.getAllLevelsForRecording(recordingId)
        val leqAverage = SoundLevelCalculator.computeLeq(allLevels)
        val sampleCount = minMax?.count ?: allLevels.size.toLong()

        recordingDao.completeRecording(
            id = recordingId,
            status = RecordingStatus.COMPLETED,
            endTime = endWallClock,
            duration = duration,
            minLevel = minMax?.minLevel,
            maxLevel = minMax?.maxLevel,
            avgLevel = leqAverage,
            sampleCount = sampleCount
        )

        _activeRecordingState.value = ActiveRecordingState()

        val completed = recordingDao.getRecordingById(recordingId)
        Log.d(TAG, "Completed recording $recordingId: duration=${duration}ms, samples=$sampleCount, Leq=$leqAverage")
        return@withContext completed
    }

    private suspend fun flushBufferLocked() {
        if (sampleBuffer.isEmpty()) return
        val toInsert = ArrayList(sampleBuffer)
        sampleBuffer.clear()
        try {
            sampleDao.insertBatch(toInsert)
        } catch (e: Exception) {
            Log.e(TAG, "Error flushing sample batch to database", e)
        }
    }

    fun recoverIncompleteSessions() {
        scope.launch(Dispatchers.IO) {
            try {
                val incomplete = recordingDao.getIncompleteRecordings()
                for (rec in incomplete) {
                    val minMax = sampleDao.getMinMaxStats(rec.id)
                    val allLevels = sampleDao.getAllLevelsForRecording(rec.id)
                    val leq = SoundLevelCalculator.computeLeq(allLevels)
                    val sampleCount = minMax?.count ?: 0L
                    val lastSample = sampleDao.getSamplesForRecording(rec.id).lastOrNull()
                    val recoveredDuration = lastSample?.elapsedMillis ?: rec.durationMillis
                    val endTime = rec.startTimeEpochMillis + recoveredDuration

                    recordingDao.completeRecording(
                        id = rec.id,
                        status = RecordingStatus.RECOVERED,
                        endTime = endTime,
                        duration = recoveredDuration,
                        minLevel = minMax?.minLevel,
                        maxLevel = minMax?.maxLevel,
                        avgLevel = leq,
                        sampleCount = sampleCount
                    )
                    Log.i(TAG, "Recovered previously interrupted recording session ${rec.id}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed recovering incomplete recordings", e)
            }
        }
    }

    // History & Detail queries
    fun getAllRecordingsFlow(): Flow<List<RecordingEntity>> = recordingDao.getAllRecordingsFlow()

    suspend fun getRecordingById(id: Long): RecordingEntity? = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(id)
    }

    fun getRecordingByIdFlow(id: Long): Flow<RecordingEntity?> = recordingDao.getRecordingByIdFlow(id)

    suspend fun getSamplesForRecording(id: Long): List<MeasurementSampleEntity> = withContext(Dispatchers.IO) {
        sampleDao.getSamplesForRecording(id)
    }

    suspend fun deleteRecording(id: Long) = withContext(Dispatchers.IO) {
        recordingDao.deleteRecordingById(id)
    }

    suspend fun renameRecording(id: Long, newTitle: String) = withContext(Dispatchers.IO) {
        recordingDao.renameRecording(id, newTitle.trim())
    }

    companion object {
        private const val TAG = "SoundMeterRepository"
        private const val BATCH_SIZE = 40
        private const val FLUSH_INTERVAL_MS = 1000L
    }
}
