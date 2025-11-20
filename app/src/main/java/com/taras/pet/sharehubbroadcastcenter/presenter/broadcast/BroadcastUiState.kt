package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast

import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent

data class BroadcastUiState(
    val events: List<BroadcastEvent> = emptyList(),
    val isListening: Boolean = false,
    val lastEvent: BroadcastEvent? = null,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean get() = events.isEmpty()
}