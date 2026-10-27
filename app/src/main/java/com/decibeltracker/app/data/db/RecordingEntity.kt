package com.decibeltracker.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.decibeltracker.app.data.model.RecordingStatus

@Entity(tableName = "recordings")
data class RecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val startTimeEpochMillis: Long,
    val endTimeEpochMillis: Long? = null,
    val durationMillis: Long = 0L,
    val status: RecordingStatus = RecordingStatus.RECORDING,
    val minimumLevel: Float? = null,
    val maximumLevel: Float? = null,
    val averageLevel: Float? = null,
    val sampleCount: Long = 0L,
    val measurementUnit: String = "dB (Estimated SPL)",
    val calibrationOffset: Float = 90.0f
)
