package com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import javax.inject.Inject

class SendShareContentUseCase @Inject constructor(
    private val shareRepository: ShareRepository
)  {
   operator fun invoke(content: SharedContent) = shareRepository.sendShareIntent(content)
}