package com.taras.pet.sharehubbroadcastcenter.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent

/**
 * CustomBroadcastReceiver — listens to internal custom broadcasts,
 * created by the application via sendCustomBroadcast().
 */

class CustomBroadcastReceiver (
    private val onEvent: (BroadcastEvent) -> Unit
): BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null) return

        val action = intent.action
        val extras = intent.extras

        if (action != null) {
            onEvent(BroadcastEvent.CustomEvent(action, extras))
        } else {
            onEvent(BroadcastEvent.Unknown("CustomBroadcast with no action"))
        }
    }
}