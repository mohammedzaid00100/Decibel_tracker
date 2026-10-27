package com.decibeltracker.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SoundLevelThresholdsTest {

    @Test
    fun testExactThresholdBoundaries() {
        // Examples specifically required:
        // 30 dB -> Green
        assertEquals(SoundLevelThresholds.SoundBand.GREEN, SoundLevelThresholds.getBand(30.0f))
        assertEquals(SoundLevelThresholds.COLOR_GREEN, SoundLevelThresholds.getColor(30.0f))

        // 45 dB -> Green
        assertEquals(SoundLevelThresholds.SoundBand.GREEN, SoundLevelThresholds.getBand(45.0f))

        // 59.9 dB -> Green
        assertEquals(SoundLevelThresholds.SoundBand.GREEN, SoundLevelThresholds.getBand(59.9f))
        assertEquals(SoundLevelThresholds.COLOR_GREEN, SoundLevelThresholds.getColor(59.9f))

        // 60.0 dB -> Blue (inclusive boundary)
        assertEquals(SoundLevelThresholds.SoundBand.BLUE, SoundLevelThresholds.getBand(60.0f))
        assertEquals(SoundLevelThresholds.COLOR_BLUE, SoundLevelThresholds.getColor(60.0f))

        // 72.0 dB -> Blue
        assertEquals(SoundLevelThresholds.SoundBand.BLUE, SoundLevelThresholds.getBand(72.0f))

        // 84.9 dB -> Blue
        assertEquals(SoundLevelThresholds.SoundBand.BLUE, SoundLevelThresholds.getBand(84.9f))
        assertEquals(SoundLevelThresholds.COLOR_BLUE, SoundLevelThresholds.getColor(84.9f))

        // 85.0 dB -> Yellow (inclusive boundary)
        assertEquals(SoundLevelThresholds.SoundBand.YELLOW, SoundLevelThresholds.getBand(85.0f))
        assertEquals(SoundLevelThresholds.COLOR_YELLOW, SoundLevelThresholds.getColor(85.0f))

        // 95.0 dB -> Yellow
        assertEquals(SoundLevelThresholds.SoundBand.YELLOW, SoundLevelThresholds.getBand(95.0f))

        // 99.9 dB -> Yellow
        assertEquals(SoundLevelThresholds.SoundBand.YELLOW, SoundLevelThresholds.getBand(99.9f))
        assertEquals(SoundLevelThresholds.COLOR_YELLOW, SoundLevelThresholds.getColor(99.9f))

        // 100.0 dB -> Red (inclusive boundary)
        assertEquals(SoundLevelThresholds.SoundBand.RED, SoundLevelThresholds.getBand(100.0f))
        assertEquals(SoundLevelThresholds.COLOR_RED, SoundLevelThresholds.getColor(100.0f))

        // 105.0 dB -> Red
        assertEquals(SoundLevelThresholds.SoundBand.RED, SoundLevelThresholds.getBand(105.0f))
        assertEquals(SoundLevelThresholds.COLOR_RED, SoundLevelThresholds.getColor(105.0f))
    }

    @Test
    fun testCreateColoredSegments_singleBand() {
        val segments = SoundLevelThresholds.createColoredSegments(
            x1 = 0f, y1 = 50f, level1 = 65f,
            x2 = 100f, y2 = 40f, level2 = 75f
        )
        assertEquals(1, segments.size)
        assertEquals(SoundLevelThresholds.COLOR_BLUE, segments[0].composeColor)
        assertEquals(0f, segments[0].startX, 0.01f)
        assertEquals(100f, segments[0].endX, 0.01f)
    }

    @Test
    fun testCreateColoredSegments_crossSingleThreshold() {
        // From 50 dB (Green) to 70 dB (Blue), crossing 60 dB at the midpoint
        val segments = SoundLevelThresholds.createColoredSegments(
            x1 = 0f, y1 = 100f, level1 = 50f,
            x2 = 100f, y2 = 60f, level2 = 70f
        )
        assertEquals(2, segments.size)
        // First segment: Green
        assertEquals(SoundLevelThresholds.COLOR_GREEN, segments[0].composeColor)
        assertEquals(0f, segments[0].startX, 0.01f)
        assertEquals(50f, segments[0].endX, 0.01f)

        // Second segment: Blue
        assertEquals(SoundLevelThresholds.COLOR_BLUE, segments[1].composeColor)
        assertEquals(50f, segments[1].startX, 0.01f)
        assertEquals(100f, segments[1].endX, 0.01f)
    }

    @Test
    fun testCreateColoredSegments_crossAllThresholdsAscending() {
        // From 40 dB to 110 dB: crosses 60 dB, 85 dB, and 100 dB
        val segments = SoundLevelThresholds.createColoredSegments(
            x1 = 0f, y1 = 100f, level1 = 40f,
            x2 = 100f, y2 = 0f, level2 = 110f
        )
        assertEquals(4, segments.size)
        assertEquals(SoundLevelThresholds.COLOR_GREEN, segments[0].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_BLUE, segments[1].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_YELLOW, segments[2].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_RED, segments[3].composeColor)

        // Verify segments are continuous
        for (i in 0 until segments.size - 1) {
            assertEquals(segments[i].endX, segments[i + 1].startX, 0.01f)
            assertEquals(segments[i].endY, segments[i + 1].startY, 0.01f)
        }
    }

    @Test
    fun testCreateColoredSegments_crossAllThresholdsDescending() {
        // From 105 dB to 50 dB: crosses 100 dB, 85 dB, and 60 dB in reverse
        val segments = SoundLevelThresholds.createColoredSegments(
            x1 = 0f, y1 = 0f, level1 = 105f,
            x2 = 100f, y2 = 100f, level2 = 50f
        )
        assertEquals(4, segments.size)
        assertEquals(SoundLevelThresholds.COLOR_RED, segments[0].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_YELLOW, segments[1].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_BLUE, segments[2].composeColor)
        assertEquals(SoundLevelThresholds.COLOR_GREEN, segments[3].composeColor)
    }
}
