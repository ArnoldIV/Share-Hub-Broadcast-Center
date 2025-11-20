package com.taras.pet.sharehubbroadcastcenter.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent

class PowerReceiver(
    private val onEvent: (BroadcastEvent) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_POWER_CONNECTED -> {
                // Try to get the plugged source from the current battery status
                val batteryIntent = context?.registerReceiver(
                    null,
                    android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED)
                )
                val plugged = batteryIntent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1

                val source = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Unknown"
                }

                onEvent(BroadcastEvent.PowerConnected(source))
            }

            Intent.ACTION_POWER_DISCONNECTED -> {
                onEvent(BroadcastEvent.PowerDisconnected)
            }

            else -> {
                onEvent(BroadcastEvent.Unknown(intent.action))
            }
        }
    }
}