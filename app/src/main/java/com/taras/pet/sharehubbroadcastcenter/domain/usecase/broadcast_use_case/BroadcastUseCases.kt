package com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case

import javax.inject.Inject

data class BroadcastUseCases @Inject constructor(
    val registerReceiversUseCase: RegisterReceiversUseCase,
    val unregisterReceiversUseCase: UnregisterReceiversUseCase,
    val observeBroadcastEventsUseCase: ObserveBroadcastEventsUseCase,
    val sendCustomBroadcastUseCase: SendCustomBroadcastUseCase,
    val fetchInitialStatusUseCase: FetchInitialStatusUseCase,
)