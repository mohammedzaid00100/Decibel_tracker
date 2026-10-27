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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.audio.SoundLevelCalculator
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CharcoalBg
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SoftWhiteText
import com.decibeltracker.app.ui.theme.SubtleMutedText
import java.util.Locale

@Composable
fun CalibrationDialog(
    currentOffset: Float,
    onSaveOffset: (Float) -> Unit,
    onDismiss: () -> Unit
) {
    var offset by remember { mutableFloatStateOf(currentOffset) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SlateSurface,
        title = {
            Text(
                text = "Microphone Calibration",
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = SoftWhiteText
            )
        },
        text = {
            Column {
                Text(
                    text = "A smartphone microphone provides digital PCM samples (dBFS). An offset is applied to estimate Sound Pressure Level (dB SPL).",
                    fontSize = 12.sp,
                    color = CoolGreyText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(CharcoalBg)
                        .border(1.dp, MutedSlateBorder, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "CALIBRATION OFFSET",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = SubtleMutedText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = String.format(Locale.US, "%+.1f dB", offset),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = BrandTurquoise
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Slider(
                    value = offset,
                    onValueChange = { offset = it },
                    valueRange = 60f..120f,
                    steps = 59,
                    colors = SliderDefaults.colors(
                        thumbColor = BrandTurquoise,
                        activeTrackColor = BrandTurquoise,
                        inactiveTrackColor = MutedSlateBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("60 dB", fontSize = 10.sp, color = SubtleMutedText)
                    Text("Default: 90 dB", fontSize = 10.sp, color = SubtleMutedText)
                    Text("120 dB", fontSize = 10.sp, color = SubtleMutedText)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { offset = (offset - 1f).coerceIn(60f, 120f) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftWhiteText),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("-1 dB", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { offset = SoundLevelCalculator.DEFAULT_CALIBRATION_OFFSET },
                        modifier = Modifier.weight(1.2f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CoolGreyText),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Reset", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { offset = (offset + 1f).coerceIn(60f, 120f) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = SoftWhiteText),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("+1 dB", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveOffset(offset)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandTurquoise,
                    contentColor = CharcoalBg
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Apply", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = CoolGreyText)
            ) {
                Text("Cancel")
            }
        }
    )
}
