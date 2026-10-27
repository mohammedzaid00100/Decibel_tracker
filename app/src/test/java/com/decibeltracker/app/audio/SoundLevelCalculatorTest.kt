package com.decibeltracker.app.audio

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

class SoundLevelCalculatorTest {

    @Test
    fun testComputeRms_silence() {
        val buffer = ShortArray(1024) { 0 }
        val rms = SoundLevelCalculator.computeRms(buffer, buffer.size)
        assertEquals(0.0, rms, 1e-6)
    }

    @Test
    fun testComputeRms_knownSineWave() {
        val size = 1000
        val buffer = ShortArray(size)
        val amplitude = 16384 // half-scale (0.5 normalized)
        for (i in 0 until size) {
            val angle = 2.0 * Math.PI * i / 50.0
            buffer[i] = (amplitude * sin(angle)).toInt().toShort()
        }
        val rms = SoundLevelCalculator.computeRms(buffer, size)
        // RMS of sine with amplitude A is A / sqrt(2) = 0.5 / 1.4142 ~ 0.3535
        assertEquals(0.3535, rms, 0.01)
    }

    @Test
    fun testRmsToDbFs_fullScaleAndSilence() {
        // Full scale RMS = 1.0 -> 0 dBFS
        val dbFull = SoundLevelCalculator.rmsToDbFs(1.0)
        assertEquals(0.0f, dbFull, 0.01f)

        // Half scale RMS = 0.5 -> -6.02 dBFS
        val dbHalf = SoundLevelCalculator.rmsToDbFs(0.5)
        assertEquals(-6.02f, dbHalf, 0.05f)

        // Zero / silence -> -96 dBFS
        val dbZero = SoundLevelCalculator.rmsToDbFs(0.0)
        assertEquals(SoundLevelCalculator.NOISE_FLOOR_DBFS, dbZero, 0.01f)
    }

    @Test
    fun testDbFsToEstimatedSpl_defaultCalibration() {
        val dbFs = -30.0f
        val offset = 90.0f
        val spl = SoundLevelCalculator.dbFsToEstimatedSpl(dbFs, offset)
        assertEquals(60.0f, spl, 0.01f)
    }

    @Test
    fun testComputeLeq_energyAveraging() {
        // Equal levels: 60 dB + 60 dB -> Leq = 60.0 dB
        val equalLevels = listOf(60.0f, 60.0f, 60.0f)
        val leqEqual = SoundLevelCalculator.computeLeq(equalLevels)
        assertNotNull(leqEqual)
        assertEquals(60.0f, leqEqual!!, 0.01f)

        // 60 dB and 70 dB:
        // Acoustic energy: 10^6 and 10^7. Mean energy = (1.1 * 10^7) / 2 = 5.5 * 10^6
        // Leq = 10 * log10(5.5 * 10^6) = 10 * 6.74036 = 67.40 dB (NOT arithmetic mean 65.0 dB)
        val testLevels = listOf(60.0f, 70.0f)
        val leq = SoundLevelCalculator.computeLeq(testLevels)
        assertNotNull(leq)
        assertEquals(67.40f, leq!!, 0.05f)

        // Empty list should safely return null
        val emptyLeq = SoundLevelCalculator.computeLeq(emptyList())
        assertNull(emptyLeq)
    }

    @Test
    fun testEnergyAccumulator_matchesBatchCalculation() {
        val accumulator = SoundLevelCalculator.EnergyAccumulator()
        val samples = listOf(45f, 52f, 68f, 74f, 81f, 62f, 55f)

        for (s in samples) {
            accumulator.addSample(s)
        }

        val streamLeq = accumulator.currentLeq()
        val batchLeq = SoundLevelCalculator.computeLeq(samples)

        assertNotNull(streamLeq)
        assertNotNull(batchLeq)
        assertEquals(batchLeq!!, streamLeq!!, 0.01f)
        assertEquals(45f, accumulator.min, 0.01f)
        assertEquals(81f, accumulator.max, 0.01f)
        assertEquals(samples.size.toLong(), accumulator.count)
    }
}
