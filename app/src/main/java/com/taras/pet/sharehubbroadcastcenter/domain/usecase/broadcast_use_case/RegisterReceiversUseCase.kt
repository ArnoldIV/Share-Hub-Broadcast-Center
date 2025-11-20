package com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import javax.inject.Inject

class RegisterReceiversUseCase @Inject constructor(
    private val broadcastRepository: BroadcastRepository
)  {
    operator fun invoke() = broadcastRepository.registerReceivers()
}