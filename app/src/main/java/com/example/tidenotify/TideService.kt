package com.example.tidenotify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.*
import java.time.LocalTime

class TideService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        startForeground(NOTIF_ID, buildNotification(tideStringNow()))

        scope.launch {
            while (isActive) {
                notify(buildNotification(tideStringNow()))
                val now = System.currentTimeMillis()
                val delayMs = 60_000L - (now % 60_000L) // align to minute
                delay(delayMs)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun tideStringNow(): String {
        val s = LocalTime.now().toSecondOfDay()
        val block = s / 10_800
        val main = ((block + 7) % 8) + 1
        val within = s % 10_800
        val dec = within / 1_080
        val microIndex = (within % 1_080) / 360
        val symbol = when (microIndex) { 0 -> '-', 1 -> '*', else -> '+' }
        return "$main.$dec$symbol"
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID, "Tide", NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows tide code on the lock screen" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tide")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // show on lock screen
            .build()

    private fun notify(n: Notification) {
        NotificationManagerCompat.from(this).notify(NOTIF_ID, n)
    }

    companion object {
        private const val CHANNEL_ID = "tide_channel"
        private const val NOTIF_ID = 1
    }
}
