package com.taras.pet.sharehubbroadcastcenter.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent

class BatteryReceiver(
    private val onEvent: (BroadcastEvent) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

                val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL

                val source = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Unknown"
                }

                onEvent(
                    BroadcastEvent.BatteryLevelChanged(
                        level = level,
                        isCharging = isCharging,
                        source = source
                    )
                )
            }

            Intent.ACTION_BATTERY_LOW -> {
                onEvent(BroadcastEvent.BatteryLow)
            }

            Intent.ACTION_BATTERY_OKAY -> {
                onEvent(BroadcastEvent.BatteryOkay)
            }

            else -> {
                onEvent(BroadcastEvent.Unknown(intent.action))
            }
        }
    }
}
