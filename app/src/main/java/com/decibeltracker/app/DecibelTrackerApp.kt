package com.decibeltracker.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.decibeltracker.app.data.db.AppDatabase
import com.decibeltracker.app.data.repository.SoundMeterRepository

class DecibelTrackerApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: SoundMeterRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        createNotificationChannels()
        database = AppDatabase.getInstance(this)
        repository = SoundMeterRepository(this, database)

        // Recover any incomplete sessions that were interrupted by process death
        repository.recoverIncompleteSessions()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_RECORDING_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_desc)
                setShowBadge(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_RECORDING_ID = "channel_decibel_recording"
        lateinit var instance: DecibelTrackerApp
            private set
    }
}
