package com.decibeltracker.app.audio

import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Acoustical & audio measurement calculations:
 * - 16-bit PCM RMS calculation
 * - Digital Full-Scale decibels (dBFS)
 * - Estimated Sound Pressure Level (dB SPL) with explicit calibration offset
 * - Equivalent continuous sound level (Leq energy average)
 */
object SoundLevelCalculator {

    const val DEFAULT_CALIBRATION_OFFSET = 90.0f
    const val MIN_DECIBEL_FLOOR = 0.0f
    const val MAX_DECIBEL_CEILING = 140.0f
    const val NOISE_FLOOR_DBFS = -96.0f

    /**
     * Computes the Root Mean Square (RMS) of 16-bit PCM samples normalized to [-1.0, 1.0].
     */
    fun computeRms(buffer: ShortArray, readSize: Int): Double {
        if (readSize <= 0) return 0.0
        var sumSquares = 0.0
        for (i in 0 until readSize) {
            val normalized = buffer[i] / 32768.0
            sumSquares += normalized * normalized
        }
        val meanSquare = sumSquares / readSize
        return sqrt(meanSquare)
    }

    /**
     * Converts an RMS amplitude value into decibels relative to full scale (dBFS).
     * 0 dBFS is full-scale sine/square wave. Values are typically <= 0.
     */
    fun rmsToDbFs(rms: Double): Float {
        if (rms <= 1e-5) {
            return NOISE_FLOOR_DBFS
        }
        val db = 20.0 * log10(rms)
        return db.toFloat().coerceIn(NOISE_FLOOR_DBFS, 0.0f)
    }

    /**
     * Converts dBFS to estimated dB SPL using an explicit calibration offset.
     * Note: Digital readings from mobile microphones depend on device hardware,
     * gain control, and microphone port acoustics.
     */
    fun dbFsToEstimatedSpl(dbFs: Float, calibrationOffset: Float = DEFAULT_CALIBRATION_OFFSET): Float {
        val estimated = dbFs + calibrationOffset
        return estimated.coerceIn(MIN_DECIBEL_FLOOR, MAX_DECIBEL_CEILING)
    }

    /**
     * Computes the equivalent continuous sound level (Leq) using energy averaging:
     * Leq = 10 * log10( (1/N) * sum( 10^(Li / 10) ) )
     *
     * Where Li are the decibel-level samples.
     * Returns null if levels list is empty or invalid.
     */
    fun computeLeq(levels: Collection<Float>): Float? {
        if (levels.isEmpty()) return null
        var totalEnergy = 0.0
        var validCount = 0
        for (level in levels) {
            if (!level.isNaN() && !level.isInfinite()) {
                val energy = 10.0.pow(level.toDouble() / 10.0)
                totalEnergy += energy
                validCount++
            }
        }
        if (validCount == 0 || totalEnergy <= 0.0) return null
        val meanEnergy = totalEnergy / validCount
        val leq = 10.0 * log10(meanEnergy)
        return leq.toFloat().coerceIn(MIN_DECIBEL_FLOOR, MAX_DECIBEL_CEILING)
    }

    /**
     * Running accumulator for energy average (Leq) to avoid storing all samples in memory
     * during live long-running measurements.
     */
    class EnergyAccumulator {
        private var sumEnergy = 0.0
        var count: Long = 0L
            private set
        var min: Float = Float.MAX_VALUE
            private set
        var max: Float = Float.MIN_VALUE
            private set

        fun addSample(level: Float) {
            if (level.isNaN() || level.isInfinite()) return
            val energy = 10.0.pow(level.toDouble() / 10.0)
            sumEnergy += energy
            count++
            if (level < min) min = level
            if (level > max) max = level
        }

        fun currentLeq(): Float? {
            if (count == 0L || sumEnergy <= 0.0) return null
            val meanEnergy = sumEnergy / count
            val leq = 10.0 * log10(meanEnergy)
            return leq.toFloat().coerceIn(MIN_DECIBEL_FLOOR, MAX_DECIBEL_CEILING)
        }

        fun reset() {
            sumEnergy = 0.0
            count = 0L
            min = Float.MAX_VALUE
            max = Float.MIN_VALUE
        }
    }
}
