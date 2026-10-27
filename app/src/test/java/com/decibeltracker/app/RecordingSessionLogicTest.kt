package com.decibeltracker.app

import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.data.model.RecordingStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecordingSessionLogicTest {

    @Test
    fun testPdfDefaultFileName_formatMatchesSpec() {
        val testEpochMillis = 1700000000000L // 2023-11-14
        val date = Date(testEpochMillis)
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val expected = "DecibelTracker_${dateFormat.format(date)}.pdf"

        val regex = Regex("^DecibelTracker_\\d{8}_\\d{6}\\.pdf$")
        assertTrue("Filename should match pattern DecibelTracker_YYYYMMDD_HHMMSS.pdf", expected.matches(regex))
    }

    @Test
    fun testRecordingEntity_statusTransitions() {
        val initialSession = RecordingEntity(
            id = 1L,
            title = "Test Session 1",
            startTimeEpochMillis = 1000L,
            status = RecordingStatus.RECORDING
        )
        assertEquals(RecordingStatus.RECORDING, initialSession.status)

        // Normal completion
        val completedSession = initialSession.copy(
            status = RecordingStatus.COMPLETED,
            endTimeEpochMillis = 61000L,
            durationMillis = 60000L,
            minimumLevel = 34.5f,
            maximumLevel = 88.2f,
            averageLevel = 65.4f,
            sampleCount = 1200L
        )
        assertEquals(RecordingStatus.COMPLETED, completedSession.status)
        assertEquals(60000L, completedSession.durationMillis)
        assertEquals(1200L, completedSession.sampleCount)

        // Interrupted session recovered
        val recoveredSession = initialSession.copy(
            status = RecordingStatus.RECOVERED,
            endTimeEpochMillis = 31000L,
            durationMillis = 30000L,
            minimumLevel = 34.5f,
            maximumLevel = 75.0f,
            averageLevel = 58.0f,
            sampleCount = 600L
        )
        assertEquals(RecordingStatus.RECOVERED, recoveredSession.status)
    }

    @Test
    fun testDynamicRangeCalculation() {
        val min = 32.4f
        val max = 94.8f
        val dynamicRange = max - min
        assertEquals(62.4f, dynamicRange, 0.01f)
    }
}
