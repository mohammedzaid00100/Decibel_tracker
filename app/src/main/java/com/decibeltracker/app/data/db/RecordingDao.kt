package com.decibeltracker.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.decibeltracker.app.data.model.RecordingStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM recordings ORDER BY startTimeEpochMillis DESC")
    fun getAllRecordingsFlow(): Flow<List<RecordingEntity>>

    @Query("SELECT * FROM recordings WHERE id = :id")
    suspend fun getRecordingById(id: Long): RecordingEntity?

    @Query("SELECT * FROM recordings WHERE id = :id")
    fun getRecordingByIdFlow(id: Long): Flow<RecordingEntity?>

    @Query("SELECT * FROM recordings WHERE status = 'RECORDING'")
    suspend fun getIncompleteRecordings(): List<RecordingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingEntity): Long

    @Update
    suspend fun updateRecording(recording: RecordingEntity)

    @Delete
    suspend fun deleteRecording(recording: RecordingEntity)

    @Query("DELETE FROM recordings WHERE id = :id")
    suspend fun deleteRecordingById(id: Long)

    @Query("UPDATE recordings SET title = :newTitle WHERE id = :id")
    suspend fun renameRecording(id: Long, newTitle: String)

    @Query("UPDATE recordings SET status = :status, endTimeEpochMillis = :endTime, durationMillis = :duration, minimumLevel = :minLevel, maximumLevel = :maxLevel, averageLevel = :avgLevel, sampleCount = :sampleCount WHERE id = :id")
    suspend fun completeRecording(
        id: Long,
        status: RecordingStatus,
        endTime: Long,
        duration: Long,
        minLevel: Float?,
        maxLevel: Float?,
        avgLevel: Float?,
        sampleCount: Long
    )
}
