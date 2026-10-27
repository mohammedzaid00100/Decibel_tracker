package com.decibeltracker.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class MinMaxResult(
    val minLevel: Float?,
    val maxLevel: Float?,
    val count: Long
)

@Dao
interface MeasurementSampleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(samples: List<MeasurementSampleEntity>)

    @Query("SELECT * FROM measurement_samples WHERE recordingId = :recordingId ORDER BY elapsedMillis ASC")
    suspend fun getSamplesForRecording(recordingId: Long): List<MeasurementSampleEntity>

    @Query("SELECT * FROM measurement_samples WHERE recordingId = :recordingId ORDER BY elapsedMillis ASC")
    fun getSamplesForRecordingFlow(recordingId: Long): Flow<List<MeasurementSampleEntity>>

    @Query("SELECT COUNT(*) FROM measurement_samples WHERE recordingId = :recordingId")
    suspend fun getSampleCount(recordingId: Long): Long

    @Query("SELECT levelValue FROM measurement_samples WHERE recordingId = :recordingId ORDER BY elapsedMillis ASC")
    suspend fun getAllLevelsForRecording(recordingId: Long): List<Float>

    @Query("SELECT MIN(levelValue) as minLevel, MAX(levelValue) as maxLevel, COUNT(*) as count FROM measurement_samples WHERE recordingId = :recordingId")
    suspend fun getMinMaxStats(recordingId: Long): MinMaxResult?

    @Query("DELETE FROM measurement_samples WHERE recordingId = :recordingId")
    suspend fun deleteSamplesForRecording(recordingId: Long)
}
