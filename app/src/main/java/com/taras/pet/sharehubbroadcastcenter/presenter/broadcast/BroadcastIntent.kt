package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast

import android.os.Bundle

sealed class BroadcastIntent {
    object StartListening : BroadcastIntent()
    object StopListening : BroadcastIntent()
    object ToggleListening : BroadcastIntent()
    data class SendCustomBroadcast(val action: String, val extras: Bundle? = null) :
        BroadcastIntent()
    object ClearError : BroadcastIntent()
    object ClearEvents : BroadcastIntent()
}