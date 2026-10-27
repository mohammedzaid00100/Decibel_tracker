package com.decibeltracker.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "measurement_samples",
    foreignKeys = [
        ForeignKey(
            entity = RecordingEntity::class,
            parentColumns = ["id"],
            childColumns = ["recordingId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["recordingId"]),
        Index(value = ["recordingId", "elapsedMillis"])
    ]
)
data class MeasurementSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val recordingId: Long,
    val elapsedMillis: Long,
    val timestampEpochMillis: Long,
    val levelValue: Float
)
