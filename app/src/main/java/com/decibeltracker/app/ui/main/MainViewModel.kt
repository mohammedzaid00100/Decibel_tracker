package com.decibeltracker.app.ui.main

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decibeltracker.app.DecibelTrackerApp
import com.decibeltracker.app.audio.GraphPoint
import com.decibeltracker.app.audio.LiveMeasurement
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.data.repository.ActiveRecordingState
import com.decibeltracker.app.data.repository.SoundMeterRepository
import com.decibeltracker.app.service.SoundMeterService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList

class MainViewModel(
    private val repository: SoundMeterRepository = DecibelTrackerApp.instance.repository
) : ViewModel() {

    val liveMeasurement: StateFlow<LiveMeasurement> = repository.liveMeasurement
    val activeRecordingState: StateFlow<ActiveRecordingState> = repository.activeRecordingState

    private val _liveGraphPoints = MutableStateFlow<List<GraphPoint>>(emptyList())
    val liveGraphPoints: StateFlow<List<GraphPoint>> = _liveGraphPoints.asStateFlow()

    private val _lastSavedRecording = MutableStateFlow<RecordingEntity?>(null)
    val lastSavedRecording: StateFlow<RecordingEntity?> = _lastSavedRecording.asStateFlow()

    private val rollingPointBuffer = LinkedList<GraphPoint>()
    private val maxBufferTimeMillis = 35000L // 35 seconds of rolling window

    init {
        // Collect high-frequency sample points for the live graph
        viewModelScope.launch {
            repository.audioEngine.sampleFlow.collect { samplePoint ->
                val newPoint = GraphPoint(samplePoint.elapsedRealtimeMillis, samplePoint.levelDb)
                rollingPointBuffer.addLast(newPoint)

                // Evict points older than 35s
                val cutoff = samplePoint.elapsedRealtimeMillis - maxBufferTimeMillis
                while (rollingPointBuffer.isNotEmpty() && rollingPointBuffer.first().elapsedMillis < cutoff) {
                    rollingPointBuffer.removeFirst()
                }

                _liveGraphPoints.value = ArrayList(rollingPointBuffer)
            }
        }
    }

    fun startMeasurementIfPermitted() {
        repository.startLiveMeasurement()
    }

    fun stopMeasurement() {
        if (!activeRecordingState.value.isRecording) {
            repository.stopLiveMeasurement()
        }
    }

    fun startRecording(context: Context) {
        viewModelScope.launch {
            repository.startRecording()
            SoundMeterService.start(context)
        }
    }

    fun stopRecording(context: Context) {
        viewModelScope.launch {
            val completed = repository.stopRecording()
            SoundMeterService.stop(context)
            _lastSavedRecording.value = completed
        }
    }

    fun resetStats() {
        repository.audioEngine.resetSessionStats()
    }

    fun setCalibrationOffset(offset: Float) {
        repository.setCalibrationOffset(offset)
    }

    fun getCalibrationOffset(): Float = repository.getCalibrationOffset()

    fun clearLastSavedRecording() {
        _lastSavedRecording.value = null
    }
}
