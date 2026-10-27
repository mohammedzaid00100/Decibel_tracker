package com.decibeltracker.app.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.ui.theme.AccentPurple
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.LevelGreen
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SubtleMutedText
import java.util.Locale

@Composable
fun StatisticsPanel(
    minDb: Float?,
    maxDb: Float?,
    avgDb: Float?,
    durationMillis: Long? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatItem(
            title = "MIN",
            value = minDb?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
            unit = "dB",
            accentColor = LevelGreen,
            modifier = Modifier.weight(1f)
        )
        StatItem(
            title = "AVG (Leq)",
            value = avgDb?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
            unit = "dB",
            accentColor = BrandTurquoise,
            modifier = Modifier.weight(1.1f)
        )
        StatItem(
            title = "MAX",
            value = maxDb?.let { String.format(Locale.US, "%.1f", it) } ?: "--.-",
            unit = "dB",
            accentColor = LevelRed,
            modifier = Modifier.weight(1f)
        )
        durationMillis?.let { duration ->
            val totalSecs = duration / 1000
            val mins = totalSecs / 60
            val secs = totalSecs % 60
            StatItem(
                title = "TIME",
                value = String.format(Locale.US, "%02d:%02d", mins, secs),
                unit = "",
                accentColor = AccentPurple,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    unit: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurface)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = SubtleMutedText,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = value,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = accentColor
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = " $unit",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = CoolGreyText,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
            }
        }
    }
}
