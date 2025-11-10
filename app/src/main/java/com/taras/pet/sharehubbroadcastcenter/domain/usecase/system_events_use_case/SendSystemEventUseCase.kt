package com.taras.pet.sharehubbroadcastcenter.domain.usecase.system_events_use_case

import com.taras.pet.sharehubbroadcastcenter.domain.repository.SystemEventsRepository
import javax.inject.Inject

class SendSystemEventUseCase @Inject constructor(
    private val systemEventsRepository: SystemEventsRepository
) {
}