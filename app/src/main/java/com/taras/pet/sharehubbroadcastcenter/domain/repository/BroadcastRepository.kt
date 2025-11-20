package com.taras.pet.sharehubbroadcastcenter.domain.repository

import android.os.Bundle
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import kotlinx.coroutines.flow.Flow

interface BroadcastRepository {
    /** Stream of all incoming events (battery, wifi, airplane mode, custom and etc.) */
    val broadcastEvents: Flow<BroadcastEvent>

    /** Registers all necessary receivers */
    fun registerReceivers()

    /** Unsubscribes from all receivers */
    fun unregisterReceivers()

    /** Sends a custom broadcast to the system */
    fun sendCustomBroadcast(action: String, extras: Bundle? = null)
}