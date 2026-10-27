package com.decibeltracker.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.R
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurfaceElevated
import com.decibeltracker.app.ui.theme.SoftWhiteText

@Composable
fun InstrumentHeader(
    isRecording: Boolean,
    onOpenHistory: () -> Unit,
    onOpenCalibration: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFD9D9D9))
                    .border(1.dp, MutedSlateBorder, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_logo_small),
                    contentDescription = "Decibel Tracker Logo",
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Decibel Tracker",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SoftWhiteText
            )

            if (isRecording) {
                Spacer(modifier = Modifier.width(10.dp))
                RecordingBadge()
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenCalibration,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceElevated)
                    .border(1.dp, MutedSlateBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Calibration Settings",
                    tint = CoolGreyText,
                    modifier = Modifier.size(17.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onOpenHistory,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceElevated)
                    .border(1.dp, MutedSlateBorder, RoundedCornerShape(8.dp))
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Saved Recordings History",
                    tint = CoolGreyText,
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun RecordingBadge() {
    val infiniteTransition = rememberInfiniteTransition(label = "recPulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recAlpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(LevelRed.copy(alpha = 0.15f))
            .border(1.dp, LevelRed.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(LevelRed.copy(alpha = alpha))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "REC",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = LevelRed
            )
        }
    }
}
