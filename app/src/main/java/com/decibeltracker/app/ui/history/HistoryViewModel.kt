package com.decibeltracker.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.decibeltracker.app.DecibelTrackerApp
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.data.repository.SoundMeterRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repository: SoundMeterRepository = DecibelTrackerApp.instance.repository
) : ViewModel() {

    val recordings: StateFlow<List<RecordingEntity>> = repository.getAllRecordingsFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteRecording(id: Long) {
        viewModelScope.launch {
            repository.deleteRecording(id)
        }
    }

    fun renameRecording(id: Long, newTitle: String) {
        if (newTitle.isNotBlank()) {
            viewModelScope.launch {
                repository.renameRecording(id, newTitle)
            }
        }
    }
}
