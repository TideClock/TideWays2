package com.example.tidenotify

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        } else {
            startTideService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startTideService()
        }
    }

    private fun startTideService() {
        val intent = Intent(this, TideService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun tideStringNow(): String {
        val t = LocalTime.now()
        val s = t.toSecondOfDay()                 // 0..86399
        val block = s / 10_800                    // 3h chunks
        val main = ((block + 7) % 8) + 1          // 8,1,2,3,4,5,6,7
        val within = s % 10_800
        val dec = within / 1_080                  // 18 min steps
        val microIndex = (within % 1_080) / 360   // 6 min steps
        val symbol = charArrayOf('-', '*', '+')[microIndex]
        return "$main.$dec$symbol"
    }
}
