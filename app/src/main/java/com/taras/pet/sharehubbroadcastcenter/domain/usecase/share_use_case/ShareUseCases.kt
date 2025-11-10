package com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case

import javax.inject.Inject

data class ShareUseCases @Inject constructor(
    val shareDataUseCase: ShareDataUseCase
)