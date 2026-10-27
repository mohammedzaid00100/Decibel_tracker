package com.decibeltracker.app.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.decibeltracker.app.ui.components.AnalogSpeedometerView
import com.decibeltracker.app.ui.components.CalibrationDialog
import com.decibeltracker.app.ui.components.InstrumentHeader
import com.decibeltracker.app.ui.components.LiveGraphView
import com.decibeltracker.app.ui.components.StatisticsPanel
import com.decibeltracker.app.ui.theme.BrandTurquoise
import com.decibeltracker.app.ui.theme.CharcoalBg
import com.decibeltracker.app.ui.theme.CoolGreyText
import com.decibeltracker.app.ui.theme.LevelRed
import com.decibeltracker.app.ui.theme.MutedSlateBorder
import com.decibeltracker.app.ui.theme.SlateSurfaceElevated
import com.decibeltracker.app.ui.theme.SoftWhiteText
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onOpenRecordingDetails: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
        if (granted) {
            viewModel.startMeasurementIfPermitted()
        }
    }

    val postNotificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Continue even if notifications denied
    }

    var showCalibrationDialog by remember { mutableStateOf(false) }

    val liveMeasurement by viewModel.liveMeasurement.collectAsState()
    val activeRecording by viewModel.activeRecordingState.collectAsState()
    val graphPoints by viewModel.liveGraphPoints.collectAsState()
    val lastSaved by viewModel.lastSavedRecording.collectAsState()

    // Lifecycle observer for starting/stopping measurement
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    if (hasMicPermission) {
                        viewModel.startMeasurementIfPermitted()
                    }
                }
                Lifecycle.Event.ON_PAUSE -> {
                    if (!activeRecording.isRecording) {
                        viewModel.stopMeasurement()
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Auto-prompt permission on first composition if missing
    LaunchedEffect(Unit) {
        if (!hasMicPermission) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                postNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Handle completed recording snackbar
    LaunchedEffect(lastSaved) {
        lastSaved?.let { rec ->
            coroutineScope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Recording saved: \"${rec.title}\"",
                    actionLabel = "View",
                    duration = androidx.compose.material3.SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onOpenRecordingDetails(rec.id)
                }
                viewModel.clearLastSavedRecording()
            }
        }
    }

    Scaffold(
        containerColor = CharcoalBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Header (starts safely below status bar and camera cutout)
            InstrumentHeader(
                isRecording = activeRecording.isRecording,
                onOpenHistory = onNavigateToHistory,
                onOpenCalibration = { showCalibrationDialog = true }
            )

            // 2. Permission banner if missing
            if (!hasMicPermission) {
                PermissionBanner(
                    onRequestPermission = {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                )
            }

            // 3. Analog Speedometer Gauge & Digital Readout
            AnalogSpeedometerView(
                currentDb = liveMeasurement.currentDb,
                isMeasuring = liveMeasurement.isMeasuring && hasMicPermission
            )

            // 4. Live Multi-Colored Line Graph
            LiveGraphView(
                points = graphPoints,
                windowDurationMillis = 30000L
            )

            // 5. Measurement Statistics (MIN, AVG Leq, MAX, TIME)
            StatisticsPanel(
                minDb = if (hasMicPermission) liveMeasurement.minDb else null,
                maxDb = if (hasMicPermission) liveMeasurement.maxDb else null,
                avgDb = if (hasMicPermission) liveMeasurement.avgDb else null,
                durationMillis = if (activeRecording.isRecording) activeRecording.elapsedDurationMillis else null
            )

            // 6. Recording Controls (RECORD / STOP and RESET)
            RecordingControlsSection(
                isRecording = activeRecording.isRecording,
                hasMicPermission = hasMicPermission,
                elapsedDurationMillis = activeRecording.elapsedDurationMillis,
                sampleCount = activeRecording.sampleCount,
                onStartRecording = {
                    if (hasMicPermission) {
                        viewModel.startRecording(context)
                    } else {
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                onStopRecording = {
                    viewModel.stopRecording(context)
                },
                onResetStats = {
                    viewModel.resetStats()
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        if (showCalibrationDialog) {
            CalibrationDialog(
                currentOffset = viewModel.getCalibrationOffset(),
                onSaveOffset = { newOffset ->
                    viewModel.setCalibrationOffset(newOffset)
                },
                onDismiss = { showCalibrationDialog = false }
            )
        }
    }
}

@Composable
private fun PermissionBanner(
    onRequestPermission: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(LevelRed.copy(alpha = 0.12f))
            .border(1.dp, LevelRed.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = LevelRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Microphone Permission Required",
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SoftWhiteText
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Microphone access is needed to compute live decibel levels. Decibel Tracker processes audio locally and never records raw audio.",
                fontSize = 12.sp,
                color = CoolGreyText,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = LevelRed,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Grant Microphone Access", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun RecordingControlsSection(
    isRecording: Boolean,
    hasMicPermission: Boolean,
    elapsedDurationMillis: Long,
    sampleCount: Long,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onResetStats: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main Record / Stop Button
            if (isRecording) {
                Button(
                    onClick = onStopRecording,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LevelRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop Recording",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "STOP RECORDING",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                Button(
                    onClick = onStartRecording,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandTurquoise,
                        contentColor = CharcoalBg
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Start Recording",
                        modifier = Modifier.size(18.dp),
                        tint = CharcoalBg
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECORD",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Reset Statistics Button
            OutlinedButton(
                onClick = onResetStats,
                modifier = Modifier.height(50.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CoolGreyText
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Stats",
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "RESET",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp
                )
            }
        }

        // Recording status banner when active
        AnimatedVisibility(
            visible = isRecording,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SlateSurfaceElevated)
                    .border(1.dp, MutedSlateBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 14.dp, vertical = 7.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val totalSecs = elapsedDurationMillis / 1000
                val mins = totalSecs / 60
                val secs = totalSecs % 60
                val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

                Text(
                    text = "Saving measurement samples to local database...",
                    fontSize = 11.sp,
                    color = CoolGreyText
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Duration: $formattedTime  |  Samples: $sampleCount",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = BrandTurquoise
                )
            }
        }
    }
}
