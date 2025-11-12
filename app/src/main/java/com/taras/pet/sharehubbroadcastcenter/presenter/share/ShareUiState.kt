package com.taras.pet.sharehubbroadcastcenter.presenter.share

import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent

data class ShareUiState(
    val sharedContent: SharedContent? = null,
    val isEmpty: Boolean = true,
    val isSharing: Boolean = false,
    val errorMessage: String? = null,
)