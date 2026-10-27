package com.decibeltracker.app.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.decibeltracker.app.DecibelTrackerApp
import com.decibeltracker.app.R
import com.decibeltracker.app.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Locale

class SoundMeterService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var updateJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_RECORDING -> {
                serviceScope.launch {
                    DecibelTrackerApp.instance.repository.stopRecording()
                    stopSelf()
                }
                return START_NOT_STICKY
            }
            ACTION_START_RECORDING -> {
                startForegroundServiceWithNotification()
            }
            else -> {
                startForegroundServiceWithNotification()
            }
        }

        observeRecordingAndMeasurement()
        return START_STICKY
    }

    private fun startForegroundServiceWithNotification() {
        val notification = buildNotification(0f, 0L)
        val foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        } else {
            0
        }

        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                foregroundType
            )
        } catch (e: Exception) {
            // Foreground service start error handling
        }
    }

    private fun observeRecordingAndMeasurement() {
        updateJob?.cancel()
        val repository = DecibelTrackerApp.instance.repository

        updateJob = serviceScope.launch {
            repository.activeRecordingState.combine(repository.liveMeasurement) { recState, liveMeas ->
                Pair(recState, liveMeas)
            }.collect { (recState, liveMeas) ->
                if (!recState.isRecording) {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    val notification = buildNotification(liveMeas.currentDb, recState.elapsedDurationMillis)
                    val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun buildNotification(currentDb: Float, elapsedDurationMillis: Long): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, SoundMeterService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val totalSecs = elapsedDurationMillis / 1000
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

        val contentText = getString(R.string.notification_recording_text, currentDb, formattedTime)

        return NotificationCompat.Builder(this, DecibelTrackerApp.CHANNEL_RECORDING_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.notification_recording_title))
            .setContentText(contentText)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                getString(R.string.notification_stop_action),
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateJob?.cancel()
        serviceScope.cancel()
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val ACTION_START_RECORDING = "com.decibeltracker.app.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.decibeltracker.app.action.STOP_RECORDING"

        fun start(context: Context) {
            val intent = Intent(context, SoundMeterService::class.java).apply {
                action = ACTION_START_RECORDING
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, SoundMeterService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            context.startService(intent)
        }
    }
}
