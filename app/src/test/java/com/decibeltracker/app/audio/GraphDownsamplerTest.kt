package com.decibeltracker.app.audio

import com.decibeltracker.app.data.db.MeasurementSampleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GraphDownsamplerTest {

    @Test
    fun testDownsample_smallListReturnsAllPoints() {
        val samples = listOf(
            MeasurementSampleEntity(1, 1, 100, 1000, 50.0f),
            MeasurementSampleEntity(2, 1, 200, 1100, 55.0f),
            MeasurementSampleEntity(3, 1, 300, 1200, 60.0f)
        )

        val result = GraphDownsampler.downsampleForDisplay(samples, targetPoints = 100)
        assertEquals(3, result.size)
        assertEquals(50.0f, result[0].levelDb, 0.01f)
        assertEquals(55.0f, result[1].levelDb, 0.01f)
        assertEquals(60.0f, result[2].levelDb, 0.01f)
    }

    @Test
    fun testDownsample_preservesChronologicalOrderAndPeaks() {
        // Create 2000 synthetic samples with an embedded sharp peak and trough
        val samples = ArrayList<MeasurementSampleEntity>()
        for (i in 0 until 2000) {
            val level = when (i) {
                550 -> 105.0f // Peak
                1250 -> 22.0f // Trough
                else -> 50.0f + (i % 10).toFloat()
            }
            samples.add(
                MeasurementSampleEntity(
                    id = i.toLong(),
                    recordingId = 1L,
                    elapsedMillis = i * 50L,
                    timestampEpochMillis = 1000000L + (i * 50L),
                    levelValue = level
                )
            )
        }

        val targetPoints = 200
        val downsampled = GraphDownsampler.downsampleForDisplay(samples, targetPoints = targetPoints)

        assertTrue(downsampled.size <= targetPoints)

        // Verify strictly chronological ordering
        for (i in 1 until downsampled.size) {
            assertTrue(
                "Elapsed time must be monotonically non-decreasing",
                downsampled[i].elapsedMillis >= downsampled[i - 1].elapsedMillis
            )
        }

        // Verify peak and trough are preserved
        val maxObserved = downsampled.maxOf { it.levelDb }
        val minObserved = downsampled.minOf { it.levelDb }
        assertEquals(105.0f, maxObserved, 0.01f)
        assertEquals(22.0f, minObserved, 0.01f)
    }
}
