package com.taras.pet.sharehubbroadcastcenter.presenter.share

import android.content.Intent
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent

sealed class ShareIntent {
    data class ParseIncomingIntent(val androidIntent: Intent) : ShareIntent()
    data class ShareAgainClicked(val content: SharedContent) : ShareIntent()
    object ClearError : ShareIntent()
}