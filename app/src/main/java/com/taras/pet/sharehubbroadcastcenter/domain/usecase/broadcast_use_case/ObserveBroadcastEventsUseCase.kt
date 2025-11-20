package com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBroadcastEventsUseCase @Inject constructor(
    private val broadcastRepository: BroadcastRepository
)  {
    operator fun invoke(): Flow<BroadcastEvent> = broadcastRepository.broadcastEvents
}