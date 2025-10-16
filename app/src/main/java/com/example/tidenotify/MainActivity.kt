package com.example.tidenotify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import java.time.LocalTime

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this).apply {
            textSize = 32f
            text = "Tide: " + tideStringNow()
            setPadding(48, 96, 48, 96)
        }
        setContentView(tv)

        ensureChannel()

        // Always start the foreground service (permission affects *posting*, not starting)
        startTideService()

        // If we have POST_NOTIFICATIONS permission, show an immediate test notification.
        // If not, request it, then show after grant.
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        } else {
            postTestNotification("Starting… ${tideStringNow()}")
        }

        // Tap the text to open this app's notification settings quickly
        tv.setOnClickListener { openAppNotificationSettings() }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            postTestNotification("Permission granted. ${tideStringNow()}")
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(NotificationManager::class.java)
            val ch = NotificationChannel(
                CHANNEL_ID, "Tide (Live)", NotificationManager.IMPORTANCE_DEFAULT
            )
            mgr.createNotificationChannel(ch)
        }
    }

    private fun postTestNotification(text: String) {
        val n = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Tide")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
        NotificationManagerCompat.from(this).notify(999, n)
    }

    private fun startTideService() {
        val intent = Intent(this, TideService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun tideStringNow(): String {
        val s = LocalTime.now().toSecondOfDay()
        val block = s / 10_800
        val main = ((block + 7) % 8) + 1
        val within = s % 10_800
        val dec = within / 1_080
        val microIndex = (within % 1_080) / 360
        val symbol = charArrayOf('-', '*', '+')[microIndex]
        return "$main.$dec$symbol"
    }

    private fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        startActivity(intent)
    }

    companion object {
        private const val CHANNEL_ID = "tide_channel_v3" // must match service
    }
}
