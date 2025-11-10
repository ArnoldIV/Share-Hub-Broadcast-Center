package com.taras.pet.sharehubbroadcastcenter.presenter.share

import androidx.lifecycle.ViewModel
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val shareRepository: ShareRepository
) : ViewModel() {
}