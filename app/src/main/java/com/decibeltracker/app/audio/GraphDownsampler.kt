package com.decibeltracker.app.audio

import com.decibeltracker.app.data.db.MeasurementSampleEntity

data class GraphPoint(
    val elapsedMillis: Long,
    val levelDb: Float
)

object GraphDownsampler {

    /**
     * Downsamples a large list of measurement samples to a target point count
     * while strictly preserving acoustic peaks, troughs, and chronological ordering.
     */
    fun downsampleForDisplay(
        samples: List<MeasurementSampleEntity>,
        targetPoints: Int = 800
    ): List<GraphPoint> {
        if (samples.size <= targetPoints) {
            return samples.map { GraphPoint(it.elapsedMillis, it.levelValue) }
        }

        val result = ArrayList<GraphPoint>(targetPoints)
        val bucketCount = targetPoints / 2
        val bucketSize = samples.size.toDouble() / bucketCount

        for (b in 0 until bucketCount) {
            val startIdx = (b * bucketSize).toInt()
            val endIdx = minOf(((b + 1) * bucketSize).toInt(), samples.size)
            if (startIdx >= endIdx) continue

            var minSample = samples[startIdx]
            var maxSample = samples[startIdx]

            for (i in startIdx + 1 until endIdx) {
                val s = samples[i]
                if (s.levelValue < minSample.levelValue) {
                    minSample = s
                }
                if (s.levelValue > maxSample.levelValue) {
                    maxSample = s
                }
            }

            // Maintain strict chronological order of the min and max within this window
            if (minSample.elapsedMillis <= maxSample.elapsedMillis) {
                result.add(GraphPoint(minSample.elapsedMillis, minSample.levelValue))
                if (minSample != maxSample) {
                    result.add(GraphPoint(maxSample.elapsedMillis, maxSample.levelValue))
                }
            } else {
                result.add(GraphPoint(maxSample.elapsedMillis, maxSample.levelValue))
                if (minSample != maxSample) {
                    result.add(GraphPoint(minSample.elapsedMillis, minSample.levelValue))
                }
            }
        }

        return result
    }
}
