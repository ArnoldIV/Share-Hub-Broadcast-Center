package com.taras.pet.sharehubbroadcastcenter.presenter.share

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.share_use_case.ShareUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val useCases: ShareUseCases
) : ViewModel() {
    private val _uiState = MutableStateFlow(ShareUiState())
    val state: StateFlow<ShareUiState> = _uiState

    private val _effect = MutableSharedFlow<ShareEffect>()
    val effect: SharedFlow<ShareEffect> = _effect

    fun parseIntent(intent: Intent) {
        viewModelScope.launch {
            val content = useCases.parseIntentUseCase(intent)
            if (content == SharedContent.Unknown) {
                emitEffect(ShareEffect.ShowToast("Unknown content"))
            } else {
                cleanError()
                reduce { copy(sharedContent = content, isEmpty = false) }
            }
        }
    }

    fun shareContent(content: SharedContent) {
        viewModelScope.launch {
            val chooser = useCases.sendContentUseCase(content)
            if (chooser != null) {
                cleanError()
                emitEffect(ShareEffect.OpenChooser(intent = chooser))
            } else {
                emitEffect(ShareEffect.ShowToast("Chooser is null"))
            }
        }
    }

    // ----------- REDUCER -----------

    private fun reduce(block: ShareUiState.() -> ShareUiState) {
        _uiState.update(block)
    }

    // ----------- INTENTS -----------

    fun onIntent(intent: ShareIntent) {
        when (intent) {
            is ShareIntent.ParseIncomingIntent -> parseIntent(intent.androidIntent)
            is ShareIntent.ShareAgainClicked -> shareContent(intent.content)
            is ShareIntent.ClearError -> cleanError()
        }
    }

    fun cleanError() {
        reduce { copy(errorMessage = null) }
    }

    private fun emitEffect(effect: ShareEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }
}