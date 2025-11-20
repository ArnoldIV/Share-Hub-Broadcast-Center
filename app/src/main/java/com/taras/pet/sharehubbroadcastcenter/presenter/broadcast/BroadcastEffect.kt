package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast

import com.taras.pet.sharehubbroadcastcenter.util.AlertType

sealed class BroadcastEffect {
    data class ShowToast(val message: String) : BroadcastEffect()
    data class PlayAlertSound(val type: AlertType) : BroadcastEffect()
}