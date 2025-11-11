package com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case

import android.content.Intent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import javax.inject.Inject

class ParseShareIntentUseCase @Inject constructor(
    private val shareRepository: ShareRepository
)  {
    operator fun invoke(intent: Intent) = shareRepository.parseSharedIntent(intent)
}