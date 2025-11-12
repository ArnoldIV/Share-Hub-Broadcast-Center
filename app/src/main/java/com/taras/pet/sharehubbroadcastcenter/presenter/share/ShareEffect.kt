package com.taras.pet.sharehubbroadcastcenter.presenter.share

import android.content.Intent

sealed class ShareEffect {
    data class ShowToast(val message: String) : ShareEffect()
    data class OpenChooser(val intent: Intent) : ShareEffect()
}