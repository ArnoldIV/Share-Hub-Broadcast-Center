package com.taras.pet.sharehubbroadcastcenter.presenter.system

import androidx.lifecycle.ViewModel
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.system_events_use_case.SystemEventsUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SystemEventsViewModel @Inject constructor(
    private val useCases: SystemEventsUseCases
)  : ViewModel() {
}