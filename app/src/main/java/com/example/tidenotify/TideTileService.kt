package com.example.tidenotify

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import java.time.LocalTime

class TideTileService : TileService() {
    override fun onStartListening() {
        val tile = qsTile ?: return
        tile.state = Tile.STATE_ACTIVE
        tile.label = tideStringNow()
        tile.updateTile()
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
}
