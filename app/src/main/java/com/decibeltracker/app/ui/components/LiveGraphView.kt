package com.decibeltracker.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.audio.GraphPoint
import com.decibeltracker.app.audio.SoundLevelThresholds
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.GridLineColor
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SubtleMutedText
import kotlin.math.max

@Composable
fun LiveGraphView(
    points: List<GraphPoint>,
    windowDurationMillis: Long = 30000L,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SlateSurface)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(12.dp))
    ) {
        if (points.isEmpty()) {
            Text(
                text = "Listening for sound data...",
                color = SubtleMutedText,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            val leftPadding = 75f
            val rightPadding = 20f
            val topPadding = 20f
            val bottomPadding = 36f

            val graphWidth = canvasWidth - leftPadding - rightPadding
            val graphHeight = canvasHeight - topPadding - bottomPadding

            if (graphWidth <= 0 || graphHeight <= 0) return@Canvas

            val minDb = 20f
            val maxDb = 110f
            val dbRange = maxDb - minDb

            // 1. Horizontal Grid Lines & Y-axis Labels
            val yLevels = floatArrayOf(20f, 40f, 60f, 80f, 100f)
            val textPaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(164, 175, 189) // CoolGreyText
                textSize = 22f
                textAlign = android.graphics.Paint.Align.RIGHT
                isAntiAlias = true
            }

            for (db in yLevels) {
                val normY = (db - minDb) / dbRange
                val y = topPadding + (1f - normY) * graphHeight

                drawLine(
                    color = GridLineColor,
                    start = Offset(leftPadding, y),
                    end = Offset(canvasWidth - rightPadding, y),
                    strokeWidth = 1.dp.toPx()
                )

                drawContext.canvas.nativeCanvas.drawText(
                    "${db.toInt()} dB",
                    leftPadding - 8f,
                    y + 7f,
                    textPaint
                )
            }

            // 2. Vertical Grid Lines & Time Labels (X-axis)
            val timePaint = android.graphics.Paint().apply {
                color = android.graphics.Color.rgb(107, 122, 141) // SubtleMutedText
                textSize = 20f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }

            val timeIntervals = longArrayOf(30000L, 20000L, 10000L, 0L)
            for (offsetMillis in timeIntervals) {
                val fraction = 1f - (offsetMillis.toFloat() / windowDurationMillis.toFloat())
                val x = leftPadding + fraction * graphWidth

                drawLine(
                    color = GridLineColor,
                    start = Offset(x, topPadding),
                    end = Offset(x, topPadding + graphHeight),
                    strokeWidth = 1.dp.toPx()
                )

                val label = if (offsetMillis == 0L) "Now" else "-${offsetMillis / 1000}s"
                drawContext.canvas.nativeCanvas.drawText(
                    label,
                    x,
                    canvasHeight - 10f,
                    timePaint
                )
            }

            if (points.isEmpty()) return@Canvas

            // 3. Draw Multi-Colored Sound Data Curve
            val latestTime = points.last().elapsedMillis
            val windowStartTime = max(0L, latestTime - windowDurationMillis)

            var prevX = 0f
            var prevY = 0f
            var prevLevel = 0f
            var hasPrev = false

            for (pt in points) {
                if (pt.elapsedMillis < windowStartTime) continue

                val timeFraction = ((pt.elapsedMillis - windowStartTime).toFloat() / windowDurationMillis.toFloat()).coerceIn(0f, 1f)
                val curX = leftPadding + timeFraction * graphWidth

                val dbFraction = ((pt.levelDb - minDb) / dbRange).coerceIn(0f, 1f)
                val curY = topPadding + (1f - dbFraction) * graphHeight

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
                            strokeWidth = 2.2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                prevX = curX
                prevY = curY
                prevLevel = pt.levelDb
                hasPrev = true
            }

            // 4. Draw glowing pulse dot at the latest point
            if (hasPrev) {
                val latestPt = points.last()
                val latestBandColor = SoundLevelThresholds.getColor(latestPt.levelDb)

                drawCircle(
                    color = latestBandColor.copy(alpha = 0.35f),
                    radius = 5.dp.toPx(),
                    center = Offset(prevX, prevY)
                )
                drawCircle(
                    color = latestBandColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(prevX, prevY)
                )
            }
        }
    }
}
