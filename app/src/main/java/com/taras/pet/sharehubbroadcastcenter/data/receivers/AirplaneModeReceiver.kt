package com.taras.pet.sharehubbroadcastcenter.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent

class AirplaneModeReceiver(
    private val onEvent: (BroadcastEvent) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                try {
                    val isAirplaneModeOn = Settings.Global.getInt(
                        context.contentResolver,
                        Settings.Global.AIRPLANE_MODE_ON, 0
                    ) != 0

                    if (isAirplaneModeOn) {
                        onEvent(BroadcastEvent.AirplaneModeOn)
                    } else {
                        onEvent(BroadcastEvent.AirplaneModeOff)
                    }
                } catch (e: Exception) {
                    onEvent(BroadcastEvent.Unknown("Failed to check airplane mode: ${e.message}"))
                }
            }

            else -> {
                onEvent(BroadcastEvent.Unknown(intent.action))
            }
        }
    }
}