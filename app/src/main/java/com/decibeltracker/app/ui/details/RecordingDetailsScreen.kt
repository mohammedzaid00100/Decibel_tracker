package com.decibeltracker.app.ui.details

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.decibeltracker.app.data.db.RecordingEntity
import com.decibeltracker.app.ui.components.SavedGraphView
import com.decibeltracker.app.ui.components.StatisticsPanel
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CharcoalBg
import com.decibeltracker.app.ui.theme.CoolGreyText
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
fun RecordingDetailsScreen(
    viewModel: RecordingDetailsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    val recording by viewModel.recording.collectAsState()
    val samples by viewModel.samples.collectAsState()
    val exportState by viewModel.exportState.collectAsState()

    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Storage Access Framework launcher for PDF creation
    val createPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            viewModel.exportPdfToUri(context, uri)
        }
    }

    LaunchedEffect(exportState) {
        when (exportState) {
            is ExportState.Success -> {
                snackbarHostState.showSnackbar((exportState as ExportState.Success).message)
                viewModel.clearExportState()
            }
            is ExportState.Error -> {
                snackbarHostState.showSnackbar((exportState as ExportState.Error).error)
                viewModel.clearExportState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Recording Details",
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
                actions = {
                    IconButton(onClick = { showRenameDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Rename",
                            tint = CoolGreyText
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = LevelRed
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
        val rec = recording
        if (rec == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = BrandTurquoise)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header info
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateSurface)
                        .border(1.dp, MutedSlateBorder, RoundedCornerShape(10.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rec.title,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = SoftWhiteText,
                                modifier = Modifier.weight(1f)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(BrandTurquoise.copy(alpha = 0.15f))
                                    .border(1.dp, BrandTurquoise.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = rec.status.name,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandTurquoise
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        val dateFormat = SimpleDateFormat("yyyy-MM-dd · HH:mm:ss", Locale.getDefault())
                        val startDateStr = dateFormat.format(Date(rec.startTimeEpochMillis))
                        Text(
                            text = "Recorded: $startDateStr",
                            fontSize = 12.sp,
                            color = CoolGreyText
                        )
                    }
                }

                // Reconstructed Multi-Colored Saved Graph
                SavedGraphView(
                    recording = rec,
                    samples = samples
                )

                // Statistics Panel
                StatisticsPanel(
                    minDb = rec.minimumLevel,
                    maxDb = rec.maximumLevel,
                    avgDb = rec.averageLevel,
                    durationMillis = rec.durationMillis
                )

                // Detailed Metadata Grid
                MetadataSummaryBox(recording = rec)

                // Export PDF Button
                Button(
                    onClick = {
                        val defaultFileName = viewModel.generateDefaultPdfFileName()
                        createPdfLauncher.launch(defaultFileName)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTurquoise,
                        contentColor = CharcoalBg
                    ),
                    shape = RoundedCornerShape(10.dp),
                    enabled = exportState !is ExportState.Exporting
                ) {
                    if (exportState is ExportState.Exporting) {
                        CircularProgressIndicator(
                            color = CharcoalBg,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("GENERATING PDF REPORT...", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Export PDF",
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "EXPORT PDF REPORT",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        SnackbarHost(hostState = snackbarHostState)

        // Rename Dialog
        if (showRenameDialog && recording != null) {
            var newTitle by remember { mutableStateOf(recording!!.title) }
            AlertDialog(
                onDismissRequest = { showRenameDialog = false },
                containerColor = SlateSurface,
                title = { Text("Rename Recording", color = SoftWhiteText, fontWeight = FontWeight.Bold) },
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
                            viewModel.renameRecording(newTitle)
                            showRenameDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandTurquoise, contentColor = CharcoalBg)
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = false }) {
                        Text("Cancel", color = CoolGreyText)
                    }
                }
            )
        }

        // Delete Dialog
        if (showDeleteDialog && recording != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                containerColor = SlateSurface,
                title = { Text("Delete Recording", color = SoftWhiteText, fontWeight = FontWeight.Bold) },
                text = {
                    Text("Are you sure you want to delete this recording? All stored samples and graph data will be removed.", color = CoolGreyText, fontSize = 13.sp)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteRecording {
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LevelRed, contentColor = Color.White)
                    ) {
                        Text("Delete", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel", color = CoolGreyText)
                    }
                }
            )
        }
    }
}

@Composable
private fun MetadataSummaryBox(recording: RecordingEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SlateSurfaceElevated)
            .border(1.dp, MutedSlateBorder, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "SESSION SPECIFICATIONS",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = SubtleMutedText
            )

            MetaRow("Stored Samples", "${recording.sampleCount}")
            MetaRow("Measurement Scale", recording.measurementUnit)
            MetaRow("Calibration Offset", String.format(Locale.US, "%+.1f dB", recording.calibrationOffset))
            val range = if (recording.maximumLevel != null && recording.minimumLevel != null) {
                recording.maximumLevel - recording.minimumLevel
            } else 0f
            MetaRow("Dynamic Range", String.format(Locale.US, "%.1f dB", range))
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = CoolGreyText)
        Text(text = value, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = SoftWhiteText)
    }
}
