package com.decibeltracker.app.data.model

data class MeasurementStats(
    val minLevel: Float? = null,
    val maxLevel: Float? = null,
    val averageLevel: Float? = null, // Leq energy average
    val sampleCount: Long = 0L,
    val durationMillis: Long = 0L
)
