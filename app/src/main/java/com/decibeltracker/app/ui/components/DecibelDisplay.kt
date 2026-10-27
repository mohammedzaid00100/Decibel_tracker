package com.decibeltracker.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.ui.theme.AlertRed
import com.decibeltracker.app.ui.theme.InstrumentBorder
import com.decibeltracker.app.ui.theme.InstrumentSurface
import com.decibeltracker.app.ui.theme.NeonGreen
import com.decibeltracker.app.ui.theme.TextMuted
import com.decibeltracker.app.ui.theme.TextPrimary
import com.decibeltracker.app.ui.theme.TextSecondary
import com.decibeltracker.app.ui.theme.WarningAmber
import java.util.Locale

@Composable
fun DecibelDisplay(
    currentDb: Float,
    isMeasuring: Boolean,
    modifier: Modifier = Modifier
) {
    val levelColor by animateColorAsState(
        targetValue = when {
            !isMeasuring -> TextMuted
            currentDb < 65f -> NeonGreen
            currentDb < 85f -> WarningAmber
            else -> AlertRed
        },
        animationSpec = tween(durationMillis = 150),
        label = "levelColor"
    )

    val referenceLabel = when {
        !isMeasuring -> "Standby"
        currentDb < 35f -> "Very Quiet (Whisper/Library)"
        currentDb < 50f -> "Quiet (Quiet Home / Office)"
        currentDb < 65f -> "Moderate (Conversation)"
        currentDb < 75f -> "Loud (Busy Street / TV)"
        currentDb < 85f -> "Very Loud (Alarm / Vacuum)"
        currentDb < 100f -> "Hearing Risk (Traffic / Power Tools)"
        else -> "Dangerous (>100 dB Hearing Hazard)"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InstrumentSurface)
            .border(1.dp, InstrumentBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Numeric reading
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
                    fontSize = 58.sp,
                    color = levelColor,
                    lineHeight = 62.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "dB",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Level bar / progress indicator
            val progress = ((currentDb - 20f) / 100f).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { if (isMeasuring) progress else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = levelColor,
                trackColor = InstrumentBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reference context string
            Text(
                text = referenceLabel,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isMeasuring) TextSecondary else TextMuted
            )
        }
    }
}
