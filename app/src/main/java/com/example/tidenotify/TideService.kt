package com.example.tidenotify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import kotlinx.coroutines.*
import java.time.LocalTime

class TideService : Service() {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    override fun onBind(intent: android.content.Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()

        val notif = buildNotification(tideStringNow())

        // Start foreground immediately
        if (Build.VERSION.SDK_INT >= 34) {
            ServiceCompat.startForeground(
                this,
                NOTIF_ID,
                notif,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(NOTIF_ID, notif)
        }

        scope.launch {
            while (isActive) {
                notify(buildNotification(tideStringNow()))
                val now = System.currentTimeMillis()
                val delayMs = 60_000L - (now % 60_000L) // align to minute boundary
                delay(delayMs)
            }
        }
    }

    override fun onStartCommand(intent: android.content.Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun tideStringNow(): String {
        val s = LocalTime.now().toSecondOfDay()   // 0..86399
        val block = s / 10_800                    // 3h chunks
        val main = ((block + 7) % 8) + 1          // maps 0..7 -> 8,1..7
        val within = s % 10_800
        val dec = within / 1_080                  // 18-min tenths
        val microIndex = (within % 1_080) / 360   // 6-min substeps (0..2)
        val symbol = charArrayOf('-', '*', '+')[microIndex]
        return "$main.$dec$symbol"
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Tide (Live)",
                NotificationManager.IMPORTANCE_DEFAULT
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
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

    private fun notify(n: Notification) {
        NotificationManagerCompat.from(this).notify(NOTIF_ID, n)
    }

    companion object {
        private const val CHANNEL_ID = "tide_channel_v2"
        private const val NOTIF_ID = 1
    }
}
