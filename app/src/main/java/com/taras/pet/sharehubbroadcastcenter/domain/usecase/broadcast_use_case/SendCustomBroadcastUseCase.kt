package com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case

import android.os.Bundle
import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import javax.inject.Inject

class SendCustomBroadcastUseCase @Inject constructor(
    private val broadcastRepository: BroadcastRepository
)  {
    operator fun invoke(action: String, extras: Bundle? = null) = broadcastRepository.sendCustomBroadcast(action,extras)
}