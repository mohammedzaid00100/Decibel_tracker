package com.decibeltracker.app.audio

import androidx.compose.ui.graphics.Color

/**
 * Shared source of truth for sound-level thresholds and visual band colors:
 * - Below 60 dB: Green (#22C55E)
 * - 60 dB to below 85 dB: Blue (#3B82F6)
 * - 85 dB to below 100 dB: Yellow (#FACC15)
 * - 100 dB and above: Red (#EF4444)
 */
object SoundLevelThresholds {

    const val THRESHOLD_GREEN_MAX = 60.0f
    const val THRESHOLD_BLUE_MAX = 85.0f
    const val THRESHOLD_YELLOW_MAX = 100.0f

    // Hex values
    const val HEX_GREEN = 0xFF22C55E
    const val HEX_BLUE = 0xFF3B82F6
    const val HEX_YELLOW = 0xFFFACC15
    const val HEX_RED = 0xFFEF4444

    // Android Graphics Color Ints (for Canvas / PdfDocument)
    const val INT_COLOR_GREEN: Int = 0xFF22C55E.toInt()
    const val INT_COLOR_BLUE: Int = 0xFF3B82F6.toInt()
    const val INT_COLOR_YELLOW: Int = 0xFFFACC15.toInt()
    const val INT_COLOR_RED: Int = 0xFFEF4444.toInt()

    // Compose Colors
    val COLOR_GREEN = Color(HEX_GREEN)
    val COLOR_BLUE = Color(HEX_BLUE)
    val COLOR_YELLOW = Color(HEX_YELLOW)
    val COLOR_RED = Color(HEX_RED)

    enum class SoundBand(
        val label: String,
        val shortLabel: String,
        val composeColor: Color,
        val androidColor: Int
    ) {
        GREEN("Below 60 dB (Normal)", "< 60 dB", COLOR_GREEN, INT_COLOR_GREEN),
        BLUE("60 - 85 dB (Moderate)", "60-85 dB", COLOR_BLUE, INT_COLOR_BLUE),
        YELLOW("85 - 100 dB (Loud)", "85-100 dB", COLOR_YELLOW, INT_COLOR_YELLOW),
        RED("100+ dB (Hazardous)", "≥ 100 dB", COLOR_RED, INT_COLOR_RED)
    }

    /**
     * Returns the SoundBand for a given decibel reading.
     * Boundaries:
     * - < 60.0f: GREEN
     * - 60.0f .. < 85.0f: BLUE
     * - 85.0f .. < 100.0f: YELLOW
     * - >= 100.0f: RED
     */
    fun getBand(levelDb: Float): SoundBand {
        return when {
            levelDb < THRESHOLD_GREEN_MAX -> SoundBand.GREEN
            levelDb < THRESHOLD_BLUE_MAX -> SoundBand.BLUE
            levelDb < THRESHOLD_YELLOW_MAX -> SoundBand.YELLOW
            else -> SoundBand.RED
        }
    }

    fun getColor(levelDb: Float): Color = getBand(levelDb).composeColor

    fun getAndroidColor(levelDb: Float): Int = getBand(levelDb).androidColor

    /**
     * Description of the sound environment based on the level.
     */
    fun getAcousticDescription(levelDb: Float): String {
        return when {
            levelDb < 35f -> "Very Quiet (Whisper / Library)"
            levelDb < 50f -> "Quiet (Quiet Home / Office)"
            levelDb < 60f -> "Normal (Moderate Speech)"
            levelDb < 75f -> "Moderate (Conversation / TV)"
            levelDb < 85f -> "Loud (Busy Street / Alarm)"
            levelDb < 100f -> "Very Loud (Hearing Risk / Tools)"
            else -> "Hazardous (Hearing Damage Risk)"
        }
    }

    /**
     * Represents a single colored line segment in 2D space.
     */
    data class ColoredSegment(
        val startX: Float,
        val startY: Float,
        val endX: Float,
        val endY: Float,
        val colorInt: Int,
        val composeColor: Color
    )

    /**
     * Splits line segments at exact threshold boundaries (60, 85, 100 dB)
     * so that the graph line transitions smoothly and accurately between color bands.
     */
    fun createColoredSegments(
        x1: Float,
        y1: Float,
        level1: Float,
        x2: Float,
        y2: Float,
        level2: Float
    ): List<ColoredSegment> {
        val band1 = getBand(level1)
        val band2 = getBand(level2)

        // If both endpoints are in the same band, no threshold crossing
        if (band1 == band2 || level1 == level2) {
            val band = getBand((level1 + level2) / 2f)
            return listOf(
                ColoredSegment(x1, y1, x2, y2, band.androidColor, band.composeColor)
            )
        }

        // Identify which thresholds are crossed between level1 and level2
        val thresholds = floatArrayOf(THRESHOLD_GREEN_MAX, THRESHOLD_BLUE_MAX, THRESHOLD_YELLOW_MAX)
        val minLevel = minOf(level1, level2)
        val maxLevel = maxOf(level1, level2)

        val crossedThresholds = mutableListOf<Float>()
        for (t in thresholds) {
            if (t > minLevel && t < maxLevel) {
                crossedThresholds.add(t)
            }
        }

        // Sort thresholds in the direction of the segment (increasing or decreasing)
        if (level1 > level2) {
            crossedThresholds.sortByDescending { it }
        } else {
            crossedThresholds.sortBy { it }
        }

        val result = mutableListOf<ColoredSegment>()
        var curX = x1
        var curY = y1
        var curLevel = level1

        for (t in crossedThresholds) {
            val fraction = ((t - curLevel) / (level2 - curLevel)).coerceIn(0f, 1f)
            val nextX = curX + fraction * (x2 - curX)
            val nextY = curY + fraction * (y2 - curY)

            val midLevel = (curLevel + t) / 2f
            val band = getBand(midLevel)

            result.add(ColoredSegment(curX, curY, nextX, nextY, band.androidColor, band.composeColor))

            curX = nextX
            curY = nextY
            curLevel = t
        }

        // Final segment from last crossing to (x2, y2)
        val finalMidLevel = (curLevel + level2) / 2f
        val finalBand = getBand(finalMidLevel)
        result.add(ColoredSegment(curX, curY, x2, y2, finalBand.androidColor, finalBand.composeColor))

        return result
    }
}
