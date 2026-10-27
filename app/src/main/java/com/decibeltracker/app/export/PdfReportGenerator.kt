package com.decibeltracker.app.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.decibeltracker.app.audio.GraphDownsampler
import com.decibeltracker.app.audio.GraphPoint
import com.decibeltracker.app.audio.SoundLevelThresholds
import com.decibeltracker.app.data.db.MeasurementSampleEntity
import com.decibeltracker.app.data.db.RecordingEntity
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

object PdfReportGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in points
    private const val PAGE_HEIGHT = 842 // Standard A4 height in points

    fun generateReport(
        context: Context,
        recording: RecordingEntity,
        samples: List<MeasurementSampleEntity>,
        outputStream: OutputStream
    ) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        drawReportPage(canvas, recording, samples)

        document.finishPage(page)
        document.writeTo(outputStream)
        document.close()
    }

    private fun drawReportPage(
        canvas: Canvas,
        recording: RecordingEntity,
        samples: List<MeasurementSampleEntity>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        // 1. Header Banner
        paint.color = Color.rgb(13, 17, 23) // Deep charcoal (#0D1117)
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), 70f, paint)

        // Title
        paint.color = Color.WHITE
        paint.textSize = 18f
        paint.isFakeBoldText = true
        canvas.drawText("DECIBEL TRACKER", 28f, 34f, paint)

        paint.textSize = 9f
        paint.isFakeBoldText = false
        paint.color = Color.rgb(164, 175, 189) // Cool grey
        canvas.drawText("Acoustic Measurement & Sound Recording Report", 28f, 52f, paint)

        // Report generated date on top right
        val generatedDateStr = "Report Generated: " + dateFormat.format(Date())
        paint.textSize = 8f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(generatedDateStr, PAGE_WIDTH - 28f, 40f, paint)
        paint.textAlign = Paint.Align.LEFT

        var curY = 90f

        // 2. Metadata Section (2 columns)
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 13f
        paint.isFakeBoldText = true
        canvas.drawText(recording.title, 28f, curY, paint)
        curY += 16f

        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8.5f
        paint.isFakeBoldText = false

        val startDateStr = dateFormat.format(Date(recording.startTimeEpochMillis))
        val endDateStr = recording.endTimeEpochMillis?.let { dateFormat.format(Date(it)) } ?: "In Progress"
        val durationFormatted = formatDuration(recording.durationMillis)

        // Col 1
        canvas.drawText("Start Time: $startDateStr", 28f, curY, paint)
        canvas.drawText("End Time:   $endDateStr", 28f, curY + 13f, paint)
        canvas.drawText("Status:     ${recording.status.name}", 28f, curY + 26f, paint)

        // Col 2
        canvas.drawText("Duration:     $durationFormatted", 320f, curY, paint)
        canvas.drawText("Samples:      ${recording.sampleCount}", 320f, curY + 13f, paint)
        canvas.drawText("Calibration:  +${recording.calibrationOffset.toInt()} dB offset (${recording.measurementUnit})", 320f, curY + 26f, paint)

        curY += 46f

        // 3. Statistics Cards
        val cardWidth = (PAGE_WIDTH - 56f - 30f) / 4f
        val cardHeight = 48f

        drawStatCard(canvas, 28f, curY, cardWidth, cardHeight, "MIN LEVEL", String.format(Locale.US, "%.1f dB", recording.minimumLevel ?: 0f), SoundLevelThresholds.INT_COLOR_GREEN)
        drawStatCard(canvas, 28f + cardWidth + 10f, curY, cardWidth, cardHeight, "MAX LEVEL", String.format(Locale.US, "%.1f dB", recording.maximumLevel ?: 0f), SoundLevelThresholds.INT_COLOR_RED)
        drawStatCard(canvas, 28f + (cardWidth + 10f) * 2, curY, cardWidth, cardHeight, "LEQ (ENERGY AVG)", String.format(Locale.US, "%.1f dB", recording.averageLevel ?: 0f), Color.rgb(0x22, 0xC7, 0xC7))
        val range = if (recording.maximumLevel != null && recording.minimumLevel != null) recording.maximumLevel - recording.minimumLevel else 0f
        drawStatCard(canvas, 28f + (cardWidth + 10f) * 3, curY, cardWidth, cardHeight, "DYNAMIC RANGE", String.format(Locale.US, "%.1f dB", range), Color.rgb(139, 92, 246))

        curY += cardHeight + 24f

        // 4. Graph Section
        paint.color = Color.rgb(30, 41, 59)
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("SOUND LEVEL PROFILE (dB vs ELAPSED TIME)", 28f, curY, paint)
        curY += 14f

        val graphLeft = 58f
        val graphTop = curY
        val graphRight = PAGE_WIDTH - 28f
        val graphBottom = curY + 360f

        drawGraph(canvas, graphLeft, graphTop, graphRight, graphBottom, recording, samples)

        curY = graphBottom + 30f

        // 5. Sound Level Reference Scale / Legend (Matching 4 Threshold Bands)
        drawReferenceBar(canvas, 28f, curY, PAGE_WIDTH - 56f)
        curY += 50f

        // 6. Methodology & Limitations Note (Footnote)
        val noteRect = RectF(28f, curY, PAGE_WIDTH - 28f, curY + 68f)
        paint.color = Color.rgb(248, 250, 252) // Slate 50
        canvas.drawRoundRect(noteRect, 6f, 6f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(noteRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 7.5f
        paint.isFakeBoldText = true
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("METHODOLOGY & CALIBRATION NOTICE", 38f, curY + 16f, paint)

        paint.isFakeBoldText = false
        paint.textSize = 6.8f
        paint.color = Color.rgb(100, 116, 139)
        val noticeLines = arrayOf(
            "• Leq (Equivalent Continuous Sound Level) is computed using logarithmic energy integration: Leq = 10 * log10( (1/N) * Σ 10^(Li/10) ).",
            "• Graph line segments are color-coded by measured level: Green (< 60 dB), Blue (60–85 dB), Yellow (85–100 dB), Red (≥ 100 dB).",
            "• Measurements are derived from uncompressed 16-bit PCM microphone samples with a calibrated software offset (+${recording.calibrationOffset.toInt()} dB).",
            "• Digital mobile microphones have hardware automatic gain adjustments and are not certified for legal or occupational IEC 61672 Class 1/Type 1 standards."
        )
        var noticeY = curY + 28f
        for (line in noticeLines) {
            canvas.drawText(line, 38f, noticeY, paint)
            noticeY += 9.5f
        }
    }

    private fun drawStatCard(
        canvas: Canvas,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        title: String,
        value: String,
        accentColor: Int
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = RectF(x, y, x + w, y + h)

        // Card background
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(rect, 4f, 4f, paint)

        // Card border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        paint.color = Color.rgb(226, 232, 240)
        canvas.drawRoundRect(rect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        // Top accent line
        paint.color = accentColor
        paint.strokeWidth = 3f
        canvas.drawLine(x + 4f, y, x + w - 4f, y, paint)

        // Label
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 7f
        paint.isFakeBoldText = true
        canvas.drawText(title, x + 8f, y + 18f, paint)

        // Value
        paint.color = Color.rgb(15, 23, 42)
        paint.textSize = 14f
        paint.isFakeBoldText = true
        canvas.drawText(value, x + 8f, y + 38f, paint)
    }

    private fun drawGraph(
        canvas: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        recording: RecordingEntity,
        samples: List<MeasurementSampleEntity>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        val graphRect = RectF(left, top, right, bottom)
        paint.color = Color.rgb(13, 17, 23) // Deep charcoal background
        canvas.drawRoundRect(graphRect, 6f, 6f, paint)

        // Determine Y Range
        val minDb = recording.minimumLevel ?: 30f
        val maxDb = recording.maximumLevel ?: 100f
        val yFloor = max(0f, floor((minDb - 5f) / 10f) * 10f)
        val yCeil = ceil((maxDb + 10f) / 10f) * 10f
        val ySpan = max(20f, yCeil - yFloor)

        val totalDurationMillis = max(1000L, recording.durationMillis)

        // Draw horizontal grid lines & Y labels
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(148, 163, 184)
            textSize = 7.5f
            textAlign = Paint.Align.RIGHT
        }

        val stepY = if (ySpan > 80) 20f else 10f
        var currentYDb = yFloor
        while (currentYDb <= yCeil) {
            val normalizedY = (currentYDb - yFloor) / ySpan
            val yPos = bottom - (normalizedY * (bottom - top))

            paint.color = Color.rgb(34, 44, 56)
            paint.strokeWidth = 1f
            canvas.drawLine(left, yPos, right, yPos, paint)

            canvas.drawText("${currentYDb.toInt()} dB", left - 6f, yPos + 3f, labelPaint)
            currentYDb += stepY
        }

        // Draw vertical grid lines & X labels
        labelPaint.textAlign = Paint.Align.CENTER
        val timeStepMillis = calculateTimeStep(totalDurationMillis)
        var curTime = 0L
        while (curTime <= totalDurationMillis) {
            val normalizedX = curTime.toFloat() / totalDurationMillis.toFloat()
            val xPos = left + (normalizedX * (right - left))

            paint.color = Color.rgb(34, 44, 56)
            paint.strokeWidth = 1f
            canvas.drawLine(xPos, top, xPos, bottom, paint)

            val timeLabel = formatDurationShort(curTime)
            canvas.drawText(timeLabel, xPos, bottom + 12f, labelPaint)
            curTime += timeStepMillis
        }

        // Downsample points for sharp vector rendering
        val graphPoints = GraphDownsampler.downsampleForDisplay(samples, targetPoints = 700)
        if (graphPoints.isEmpty()) {
            labelPaint.color = Color.rgb(148, 163, 184)
            labelPaint.textSize = 12f
            canvas.drawText("No sample data recorded", (left + right) / 2f, (top + bottom) / 2f, labelPaint)
            return
        }

        // Draw subtle translucent fill under curve
        val fillPath = Path()
        var first = true
        var startX = left
        var lastX = left

        for (pt in graphPoints) {
            val normX = (pt.elapsedMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
            val x = left + normX * (right - left)

            val normY = ((pt.levelDb - yFloor) / ySpan).coerceIn(0f, 1f)
            val y = bottom - (normY * (bottom - top))

            if (first) {
                fillPath.moveTo(x, bottom)
                fillPath.lineTo(x, y)
                startX = x
                first = false
            } else {
                fillPath.lineTo(x, y)
            }
            lastX = x
        }

        fillPath.lineTo(lastX, bottom)
        fillPath.lineTo(startX, bottom)
        fillPath.close()

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = Color.argb(25, 255, 255, 255) // Translucent light overlay
        }
        canvas.drawPath(fillPath, fillPaint)

        // Draw Multi-Colored Graph Line Segments (Green, Blue, Yellow, Red)
        val segmentPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.6f
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
        }

        var prevX = 0f
        var prevY = 0f
        var prevLevel = 0f
        var hasPrev = false

        for (pt in graphPoints) {
            val normX = (pt.elapsedMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
            val curX = left + normX * (right - left)

            val normY = ((pt.levelDb - yFloor) / ySpan).coerceIn(0f, 1f)
            val curY = bottom - (normY * (bottom - top))

            if (hasPrev) {
                val segments = SoundLevelThresholds.createColoredSegments(
                    prevX, prevY, prevLevel,
                    curX, curY, pt.levelDb
                )
                for (seg in segments) {
                    segmentPaint.color = seg.colorInt
                    canvas.drawLine(seg.startX, seg.startY, seg.endX, seg.endY, segmentPaint)
                }
            }

            prevX = curX
            prevY = curY
            prevLevel = pt.levelDb
            hasPrev = true
        }

        // Draw Leq reference line if available
        recording.averageLevel?.let { leq ->
            val normLeqY = ((leq - yFloor) / ySpan).coerceIn(0f, 1f)
            val leqY = bottom - (normLeqY * (bottom - top))

            val dashedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                color = Color.rgb(0x22, 0xC7, 0xC7) // Brand turquoise
                pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
            }
            canvas.drawLine(left, leqY, right, leqY, dashedPaint)

            val leqTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(0x22, 0xC7, 0xC7)
                textSize = 7f
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("Leq: ${String.format(Locale.US, "%.1f", leq)} dB", right - 8f, leqY - 4f, leqTextPaint)
        }
    }

    private fun drawReferenceBar(canvas: Canvas, x: Float, y: Float, width: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = Color.rgb(100, 116, 139)
        paint.textSize = 8f
        paint.isFakeBoldText = true
        canvas.drawText("SOUND LEVEL GRAPH LEGEND & THRESHOLD BANDS", x, y, paint)

        val barY = y + 8f
        val levels = arrayOf(
            Pair("< 60 dB (Normal)", SoundLevelThresholds.INT_COLOR_GREEN),
            Pair("60 - 85 dB (Moderate)", SoundLevelThresholds.INT_COLOR_BLUE),
            Pair("85 - 100 dB (Loud)", SoundLevelThresholds.INT_COLOR_YELLOW),
            Pair("≥ 100 dB (Hazardous)", SoundLevelThresholds.INT_COLOR_RED)
        )

        val itemWidth = width / levels.size
        for (i in levels.indices) {
            val item = levels[i]
            val itemX = x + (i * itemWidth)

            paint.color = item.second
            canvas.drawCircle(itemX + 4f, barY + 6f, 3.5f, paint)

            paint.color = Color.rgb(51, 65, 85)
            paint.textSize = 6.8f
            paint.isFakeBoldText = false
            canvas.drawText(item.first, itemX + 12f, barY + 9f, paint)
        }
    }

    private fun calculateTimeStep(totalDurationMillis: Long): Long {
        val durationSecs = totalDurationMillis / 1000
        return when {
            durationSecs <= 15 -> 2000L
            durationSecs <= 60 -> 10000L
            durationSecs <= 300 -> 30000L // 30s
            durationSecs <= 900 -> 60000L // 1 min
            durationSecs <= 3600 -> 300000L // 5 min
            else -> 600000L // 10 min
        }
    }

    private fun formatDuration(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format(Locale.US, "%d hr %02d min %02d sec", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d min %02d sec", mins, secs)
        }
    }

    private fun formatDurationShort(millis: Long): String {
        val totalSecs = millis / 1000
        val hours = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return if (hours > 0) {
            String.format(Locale.US, "%02d:%02d:%02d", hours, mins, secs)
        } else {
            String.format(Locale.US, "%02d:%02d", mins, secs)
        }
    }
}
