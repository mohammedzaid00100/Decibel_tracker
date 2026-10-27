package com.decibeltracker.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CharcoalBg
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.LevelBlue
import com.decibeltracker.app.ui.theme.LevelGreen
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurface
import com.decibeltracker.app.ui.theme.SlateSurfaceElevated
import com.decibeltracker.app.ui.theme.SoftWhiteText
import com.decibeltracker.app.ui.theme.SubtleMutedText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onNavigateBack: () -> Unit,
    onSelectRecording: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val recordings by viewModel.recordings.collectAsState()

    var recordingToRename by remember { mutableStateOf<RecordingEntity?>(null) }
    var recordingToDelete by remember { mutableStateOf<RecordingEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Saved Recordings",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = SoftWhiteText
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SoftWhiteText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CharcoalBg
                )
            )
        },
        containerColor = CharcoalBg,
        modifier = modifier
    ) { innerPadding ->
        if (recordings.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = SubtleMutedText,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "No saved recordings yet",
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = SoftWhiteText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Press RECORD on the main screen to capture sound data.",
                        fontSize = 13.sp,
                        color = CoolGreyText
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = recordings,
                    key = { it.id }
                ) { rec ->
                    RecordingListItem(
                        recording = rec,
                        onClick = { onSelectRecording(rec.id) },
                        onRename = { recordingToRename = rec },
                        onDelete = { recordingToDelete = rec }
                    )
                }
            }
        }

        // Rename Dialog
        recordingToRename?.let { rec ->
            var newTitle by remember { mutableStateOf(rec.title) }
            AlertDialog(
                onDismissRequest = { recordingToRename = null },
                containerColor = SlateSurface,
                title = {
                    Text("Rename Recording", color = SoftWhiteText, fontWeight = FontWeight.Bold)
                },
                text = {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SoftWhiteText,
                            unfocusedTextColor = SoftWhiteText,
                            focusedBorderColor = BrandTurquoise,
                            unfocusedBorderColor = MutedSlateBorder
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.renameRecording(rec.id, newTitle)
                            recordingToRename = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandTurquoise,
                            contentColor = CharcoalBg
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recordingToRename = null }) {
                        Text("Cancel", color = CoolGreyText)
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        recordingToDelete?.let { rec ->
            AlertDialog(
                onDismissRequest = { recordingToDelete = null },
                containerColor = SlateSurface,
                title = {
                    Text("Delete Recording", color = SoftWhiteText, fontWeight = FontWeight.Bold)
                },
                text = {
                    Text(
                        "Are you sure you want to delete \"${rec.title}\"? This action cannot be undone.",
                        color = CoolGreyText,
                        fontSize = 13.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRecording(rec.id)
                            recordingToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LevelRed,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { recordingToDelete = null }) {
                        Text("Cancel", color = CoolGreyText)
                    }
                }
            )
        }
    }
}

@Composable
private fun RecordingListItem(
    recording: RecordingEntity,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault())
    val dateStr = dateFormat.format(Date(recording.startTimeEpochMillis))

    val totalSecs = recording.durationMillis / 1000
    val mins = totalSecs / 60
    val secs = totalSecs % 60
    val durationFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurface)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = recording.title,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = SoftWhiteText
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$dateStr · Duration: $durationFormatted",
                        fontSize = 12.sp,
                        color = CoolGreyText
                    )
                }

                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = CoolGreyText,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(SlateSurfaceElevated)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Rename", color = SoftWhiteText) },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = CoolGreyText, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onRename()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = LevelRed) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = LevelRed, modifier = Modifier.size(18.dp))
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mini stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                recording.minimumLevel?.let {
                    MiniStat("MIN", String.format(Locale.US, "%.1f dB", it), LevelGreen)
                }
                recording.averageLevel?.let {
                    MiniStat("AVG (Leq)", String.format(Locale.US, "%.1f dB", it), BrandTurquoise)
                }
                recording.maximumLevel?.let {
                    MiniStat("MAX", String.format(Locale.US, "%.1f dB", it), LevelRed)
                }
                MiniStat("SAMPLES", "${recording.sampleCount}", SubtleMutedText)
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label: ",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = SubtleMutedText
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = color
        )
    }
}
