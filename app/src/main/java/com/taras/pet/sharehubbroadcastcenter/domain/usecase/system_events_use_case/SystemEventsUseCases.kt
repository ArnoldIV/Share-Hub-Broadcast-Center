package com.taras.pet.sharehubbroadcastcenter.domain.usecase.system_events_use_case

import javax.inject.Inject

data class SystemEventsUseCases @Inject constructor(
    val sendSystemEventUseCase: SendSystemEventUseCase
)