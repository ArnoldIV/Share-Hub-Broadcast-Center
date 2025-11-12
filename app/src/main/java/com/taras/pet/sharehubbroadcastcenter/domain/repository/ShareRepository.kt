package com.taras.pet.sharehubbroadcastcenter.domain.repository

import android.content.Intent
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent

interface ShareRepository {
    fun parseSharedIntent(intent: Intent): SharedContent

    fun sendShareIntent(content: SharedContent): Intent?

}