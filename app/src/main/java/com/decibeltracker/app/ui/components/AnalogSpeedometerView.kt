package com.decibeltracker.app.ui.components

import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.audio.SoundLevelThresholds
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.LevelBlue
import com.decibeltracker.app.ui.theme.LevelGreen
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.LevelYellow
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SoftWhiteText
import com.decibeltracker.app.ui.theme.SubtleMutedText
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Analog speedometer-style sound-level gauge.
 * Features:
 * - Semicircular graduated arc spanning 0 to 120 dB with major 10 dB increments and minor ticks.
 * - Semicircular color bands: Green (<60 dB), Blue (60-85 dB), Yellow (85-100 dB), Red (>=100 dB).
 * - Smoothly moving needle from a central pivot driven by real measurement data.
 * - Prominent digital readout with unit and acoustic category context.
 */
@Composable
fun AnalogSpeedometerView(
    currentDb: Float,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier
) {
    val displayDb = if (isMeasuring) currentDb.coerceIn(0f, 130f) else 0f
    val animatedDb by animateFloatAsState(
        targetValue = displayDb,
        animationSpec = tween(durationMillis = 100),
        label = "gaugeNeedleAnimation"
    )

    val currentBand = SoundLevelThresholds.getBand(if (isMeasuring) currentDb else 0f)
    val bandColor = if (isMeasuring) currentBand.composeColor else SubtleMutedText
    val acousticContext = if (isMeasuring) {
        SoundLevelThresholds.getAcousticDescription(currentDb)
    } else {
        "Microphone Standby"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SlateSurface)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(14.dp))
            .padding(top = 10.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Speedometer Canvas (Semicircle + Ticks + Needle + Pivot)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Center pivot near the bottom center of the gauge canvas
                    val pivotX = w / 2f
                    val pivotY = h - 18f
                    val radius = minOf(w * 0.44f, pivotY - 14f)

                    if (radius <= 0) return@Canvas

                    // 1. Draw Background Arc Track
                    val arcThickness = 6.dp.toPx()
                    val arcRectTopLeft = Offset(pivotX - radius, pivotY - radius)
                    val arcSize = Size(radius * 2, radius * 2)

                    // Draw colored threshold bands along the arc:
                    // Scale: 0 to 120 dB over 180 degrees sweep (from 180° to 360°)
                    // Degrees per dB = 180 / 120 = 1.5 deg/dB
                    // Band 1: 0 to 60 dB   -> sweep = 60 * 1.5 = 90°  (start = 180°)
                    // Band 2: 60 to 85 dB  -> sweep = 25 * 1.5 = 37.5°(start = 270°)
                    // Band 3: 85 to 100 dB -> sweep = 15 * 1.5 = 22.5°(start = 307.5°)
                    // Band 4: 100 to 120 dB-> sweep = 20 * 1.5 = 30°  (start = 330°)

                    // Green Band (<60 dB)
                    drawArc(
                        color = LevelGreen.copy(alpha = 0.85f),
                        startAngle = 180f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = arcThickness, cap = StrokeCap.Round)
                    )

                    // Blue Band (60 - 85 dB)
                    drawArc(
                        color = LevelBlue.copy(alpha = 0.85f),
                        startAngle = 270f,
                        sweepAngle = 37.5f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = arcThickness, cap = StrokeCap.Square)
                    )

                    // Yellow Band (85 - 100 dB)
                    drawArc(
                        color = LevelYellow.copy(alpha = 0.85f),
                        startAngle = 307.5f,
                        sweepAngle = 22.5f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = arcThickness, cap = StrokeCap.Square)
                    )

                    // Red Band (>= 100 dB)
                    drawArc(
                        color = LevelRed.copy(alpha = 0.85f),
                        startAngle = 330f,
                        sweepAngle = 30f,
                        useCenter = false,
                        topLeft = arcRectTopLeft,
                        size = arcSize,
                        style = Stroke(width = arcThickness, cap = StrokeCap.Round)
                    )

                    // 2. Draw Graduated Tick Marks & Numeric Labels
                    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                        color = android.graphics.Color.rgb(164, 175, 189) // CoolGrey
                        textSize = 24f
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    // Ticks every 2 dB from 0 to 120
                    for (dbVal in 0..120 step 2) {
                        val angleDeg = 180f + (dbVal.toFloat() / 120f) * 180f
                        val angleRad = Math.toRadians(angleDeg.toDouble())

                        val isMajor = (dbVal % 10 == 0)
                        val isMid = (!isMajor && dbVal % 5 == 0)

                        val tickLength = when {
                            isMajor -> 10.dp.toPx()
                            isMid -> 6.dp.toPx()
                            else -> 4.dp.toPx()
                        }

                        val strokeW = when {
                            isMajor -> 2.dp.toPx()
                            isMid -> 1.2.dp.toPx()
                            else -> 0.8.dp.toPx()
                        }

                        val tickBand = SoundLevelThresholds.getBand(dbVal.toFloat())
                        val tickColor = if (isMajor) tickBand.composeColor else CoolGreyText.copy(alpha = 0.5f)

                        val startR = radius - 8.dp.toPx()
                        val endR = startR - tickLength

                        val x1 = (pivotX + startR * cos(angleRad)).toFloat()
                        val y1 = (pivotY + startR * sin(angleRad)).toFloat()
                        val x2 = (pivotX + endR * cos(angleRad)).toFloat()
                        val y2 = (pivotY + endR * sin(angleRad)).toFloat()

                        drawLine(
                            color = tickColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = strokeW,
                            cap = StrokeCap.Round
                        )

                        // Draw Number Labels on Major Ticks: 0, 10, 20, ..., 120
                        if (isMajor) {
                            val labelR = endR - 12.dp.toPx()
                            val lx = (pivotX + labelR * cos(angleRad)).toFloat()
                            val ly = (pivotY + labelR * sin(angleRad)).toFloat() + 8f

                            textPaint.color = if (dbVal >= 100) {
                                SoundLevelThresholds.INT_COLOR_RED
                            } else {
                                android.graphics.Color.rgb(243, 246, 250)
                            }

                            drawContext.canvas.nativeCanvas.drawText(
                                "$dbVal",
                                lx,
                                ly,
                                textPaint
                            )
                        }
                    }

                    // 3. Draw Needle
                    // Angle calculation for measured level
                    val needleDb = animatedDb.coerceIn(0f, 120f)
                    val needleAngleDeg = 180f + (needleDb / 120f) * 180f
                    val needleAngleRad = Math.toRadians(needleAngleDeg.toDouble())

                    val needleLength = radius - 10.dp.toPx()
                    val needleTipX = (pivotX + needleLength * cos(needleAngleRad)).toFloat()
                    val needleTipY = (pivotY + needleLength * sin(needleAngleRad)).toFloat()

                    // Needle base width (perpendicular to angle)
                    val perpRad = needleAngleRad + Math.PI / 2.0
                    val baseHalfWidth = 4.dp.toPx()
                    val bx1 = (pivotX + baseHalfWidth * cos(perpRad)).toFloat()
                    val by1 = (pivotY + baseHalfWidth * sin(perpRad)).toFloat()
                    val bx2 = (pivotX - baseHalfWidth * cos(perpRad)).toFloat()
                    val by2 = (pivotY - baseHalfWidth * sin(perpRad)).toFloat()

                    // Tapered Needle Path
                    val needlePath = Path().apply {
                        moveTo(bx1, by1)
                        lineTo(needleTipX, needleTipY)
                        lineTo(bx2, by2)
                        close()
                    }

                    // Needle color reflects current threshold band
                    drawPath(
                        path = needlePath,
                        color = bandColor
                    )

                    // 4. Central Pivot Cap
                    drawCircle(
                        color = MutedSlateBorder,
                        radius = 11.dp.toPx(),
                        center = Offset(pivotX, pivotY)
                    )
                    drawCircle(
                        color = SlateSurface,
                        radius = 8.dp.toPx(),
                        center = Offset(pivotX, pivotY)
                    )
                    drawCircle(
                        color = bandColor,
                        radius = 3.5.dp.toPx(),
                        center = Offset(pivotX, pivotY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Prominent Digital Reading: "55.3 dB"
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                val formattedValue = if (isMeasuring) {
                    String.format(Locale.US, "%.1f", currentDb)
                } else {
                    "--.-"
                }

                Text(
                    text = formattedValue,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 42.sp,
                    color = bandColor,
                    lineHeight = 44.sp
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "dB",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = CoolGreyText,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Acoustic Context Description
            Text(
                text = acousticContext,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = if (isMeasuring) SoftWhiteText else SubtleMutedText
            )
        }
    }
}
