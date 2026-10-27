package com.decibeltracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.audio.GraphDownsampler
import com.decibeltracker.app.audio.SoundLevelThresholds
import com.decibeltracker.app.data.db.MeasurementSampleEntity
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.GridLineColor
import com.decibeltracker.app.ui.theme.LevelBlue
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SoftWhiteText
import com.decibeltracker.app.ui.theme.SubtleMutedText
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

@Composable
fun SavedGraphView(
    recording: RecordingEntity,
    samples: List<MeasurementSampleEntity>,
    modifier: Modifier = Modifier
) {
    var touchX by remember { mutableStateOf<Float?>(null) }

    val graphPoints = remember(samples) {
        GraphDownsampler.downsampleForDisplay(samples, targetPoints = 800)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurface)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        touchX = offset.x
                        tryAwaitRelease()
                        touchX = null
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset -> touchX = offset.x },
                    onDragEnd = { touchX = null },
                    onDragCancel = { touchX = null },
                    onDrag = { change, _ ->
                        change.consume()
                        touchX = change.position.x
                    }
                )
            }
    ) {
        if (graphPoints.isEmpty()) {
            Text(
                text = "No stored measurement data",
                color = SubtleMutedText,
                fontSize = 13.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftPadding = 75f
            val rightPadding = 25f
            val topPadding = 25f
            val bottomPadding = 40f

            val graphWidth = canvasWidth - leftPadding - rightPadding
            val graphHeight = canvasHeight - topPadding - bottomPadding

            if (graphWidth <= 0 || graphHeight <= 0) return@Canvas

            // Compute bounds
            val minDb = recording.minimumLevel ?: 30f
            val maxDb = recording.maximumLevel ?: 90f
            val yFloor = max(0f, floor((minDb - 5f) / 10f) * 10f)
            val yCeil = ceil((maxDb + 10f) / 10f) * 10f
            val ySpan = max(20f, yCeil - yFloor)
            val totalDurationMillis = max(1000L, recording.durationMillis)

            // Y grid & labels
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(164, 175, 189) // CoolGrey
                textSize = 22f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }

            val stepY = if (ySpan > 80) 20f else 10f
            var curYDb = yFloor
            while (curYDb <= yCeil) {
                val normY = (curYDb - yFloor) / ySpan
                val y = topPadding + (1f - normY) * graphHeight

                drawLine(
                    color = GridLineColor,
                    start = Offset(leftPadding, y),
                    end = Offset(canvasWidth - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${curYDb.toInt()} dB",
                    leftPadding - 8f,
                    y + 7f,
                    textPaint
                )
                curYDb += stepY
            }

            // X grid & time labels
            val timePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(107, 122, 141) // SubtleMuted
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val numXTicks = 5
            for (i in 0..numXTicks) {
                val frac = i.toFloat() / numXTicks.toFloat()
                val x = leftPadding + frac * graphWidth
                val timeAtTick = (frac * totalDurationMillis).toLong()

                drawLine(
                    color = GridLineColor,
                    start = Offset(x, topPadding),
                    end = Offset(x, topPadding + graphHeight),
                    strokeWidth = 1.dp.toPx()
                )

                val label = formatTime(timeAtTick)
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    canvasHeight - 10f,
                    timePaint
                )
            }

            if (graphPoints.isEmpty()) return@Canvas

            // Multi-colored segments based on threshold crossings
            var prevX = 0f
            var prevY = 0f
            var prevLevel = 0f
            var hasPrev = false

            for (pt in graphPoints) {
                val normX = (pt.elapsedMillis.toFloat() / totalDurationMillis.toFloat()).coerceIn(0f, 1f)
                val curX = leftPadding + normX * graphWidth

                val normY = ((pt.levelDb - yFloor) / ySpan).coerceIn(0f, 1f)
                val curY = topPadding + (1f - normY) * graphHeight

                if (hasPrev) {
                    val segments = SoundLevelThresholds.createColoredSegments(
                        prevX, prevY, prevLevel,
                        curX, curY, pt.levelDb
                    )
                    for (seg in segments) {
                        drawLine(
                            color = seg.composeColor,
                            start = Offset(seg.startX, seg.startY),
                            end = Offset(seg.endX, seg.endY),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
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
                val leqY = topPadding + (1f - normLeqY) * graphHeight

                drawLine(
                    color = BrandTurquoise,
                    start = Offset(leftPadding, leqY),
                    end = Offset(canvasWidth - rightPadding, leqY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                val leqPaint = android.graphics.Paint().apply {
                    color = android.graphics.Color.rgb(0x22, 0xC7, 0xC7) // BrandTurquoise
                    textSize = 20f
                    textAlign = android.graphics.Paint.Align.RIGHT
                    isAntiAlias = true
                }
                drawContext.canvas.nativeCanvas.drawText(
                    "Leq: ${String.format(Locale.US, "%.1f", leq)} dB",
                    canvasWidth - rightPadding - 6f,
                    leqY - 5f,
                    leqPaint
                )
            }

            // Interactive scrubber / tooltip on touch
            touchX?.let { tx ->
                val clampedTx = tx.coerceIn(leftPadding, canvasWidth - rightPadding)
                val frac = (clampedTx - leftPadding) / graphWidth
                val targetElapsed = (frac * totalDurationMillis).toLong()

                val closestPt = graphPoints.minByOrNull { kotlin.math.abs(it.elapsedMillis - targetElapsed) }
                closestPt?.let { pt ->
                    val normY = ((pt.levelDb - yFloor) / ySpan).coerceIn(0f, 1f)
                    val ptY = topPadding + (1f - normY) * graphHeight
                    val ptColor = SoundLevelThresholds.getColor(pt.levelDb)

                    // Vertical guide line
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(clampedTx, topPadding),
                        end = Offset(clampedTx, topPadding + graphHeight),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Point circle
                    drawCircle(
                        color = ptColor,
                        radius = 4.5.dp.toPx(),
                        center = Offset(clampedTx, ptY)
                    )

                    // Tooltip text
                    val tooltipPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textSize = 22f
                        isFakeBoldText = true
                        isAntiAlias = true
                    }
                    val info = String.format(Locale.US, "%.1f dB @ %s", pt.levelDb, formatTime(pt.elapsedMillis))
                    val tooltipX = if (clampedTx > canvasWidth - 160f) clampedTx - 200f else clampedTx + 10f
                    drawContext.canvas.nativeCanvas.drawText(
                        info,
                        tooltipX,
                        topPadding + 24f,
                        tooltipPaint
                    )
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSecs = millis / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    return String.format(Locale.US, "%02d:%02d", mins, secs)
}
