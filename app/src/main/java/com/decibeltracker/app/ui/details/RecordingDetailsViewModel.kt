package com.decibeltracker.app.ui.details

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decibeltracker.app.DecibelTrackerApp
import com.decibeltracker.app.data.db.MeasurementSampleEntity
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.data.repository.SoundMeterRepository
import com.decibeltracker.app.export.PdfReportGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val error: String) : ExportState()
}

class RecordingDetailsViewModel(
    private val recordingId: Long,
    private val repository: SoundMeterRepository = DecibelTrackerApp.instance.repository
) : ViewModel() {

    private val _recording = MutableStateFlow<RecordingEntity?>(null)
    val recording: StateFlow<RecordingEntity?> = _recording.asStateFlow()

    private val _samples = MutableStateFlow<List<MeasurementSampleEntity>>(emptyList())
    val samples: StateFlow<List<MeasurementSampleEntity>> = _samples.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        loadRecording()
    }

    private fun loadRecording() {
        viewModelScope.launch {
            val rec = repository.getRecordingById(recordingId)
            _recording.value = rec
            val sampleList = repository.getSamplesForRecording(recordingId)
            _samples.value = sampleList
        }
    }

    fun renameRecording(newTitle: String) {
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                repository.renameRecording(recordingId, newTitle)
                _recording.value = _recording.value?.copy(title = newTitle.trim())
            }
        }
    }

    fun deleteRecording(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteRecording(recordingId)
            onDeleted()
        }
    }

    fun generateDefaultPdfFileName(): String {
        val rec = _recording.value
        val date = rec?.startTimeEpochMillis?.let { Date(it) } ?: Date()
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return "DecibelTracker_${dateFormat.format(date)}.pdf"
    }

    fun exportPdfToUri(context: Context, destinationUri: Uri) {
        val currentRec = _recording.value
        val sampleList = _samples.value

        if (currentRec == null) {
            _exportState.value = ExportState.Error("Recording details not loaded")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            _exportState.value = ExportState.Exporting
            var outputStream: OutputStream? = null
            try {
                outputStream = context.contentResolver.openOutputStream(destinationUri)
                if (outputStream == null) {
                    _exportState.value = ExportState.Error("Cannot open destination file")
                    return@launch
                }

                PdfReportGenerator.generateReport(
                    context = context,
                    recording = currentRec,
                    samples = sampleList,
                    outputStream = outputStream
                )

                _exportState.value = ExportState.Success("PDF report successfully exported!")
            } catch (e: Exception) {
                _exportState.value = ExportState.Error("Export failed: ${e.message}")
            } finally {
                try {
                    outputStream?.close()
                } catch (_: Exception) {}
            }
        }
    }

    fun clearExportState() {
        _exportState.value = ExportState.Idle
    }
}
