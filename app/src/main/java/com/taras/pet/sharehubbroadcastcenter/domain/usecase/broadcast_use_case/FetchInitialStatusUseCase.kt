package com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import javax.inject.Inject

class FetchInitialStatusUseCase @Inject constructor(
    private val broadcastRepository: BroadcastRepository
) {
    operator fun invoke(): List<BroadcastEvent> {
        // For now, return empty list as the initial status is handled
        // by the repository's fetchInitialSystemStatus() method
        // This could be extended to provide cached or computed initial events
        return emptyList()
    }
}