package com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import com.taras.pet.sharehubbroadcastcenter.domain.repository.SystemEventsRepository
import javax.inject.Inject

class ShareDataUseCase @Inject constructor(
    private val systemEventsRepository: ShareRepository
) {
}