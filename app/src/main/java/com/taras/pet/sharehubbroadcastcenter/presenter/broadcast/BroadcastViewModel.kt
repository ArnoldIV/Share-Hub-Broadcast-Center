package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import com.taras.pet.sharehubbroadcastcenter.domain.usecase.broadcast_use_case.BroadcastUseCases
import com.taras.pet.sharehubbroadcastcenter.presenter.broadcast.BroadcastEffect.*
import com.taras.pet.sharehubbroadcastcenter.util.AlertType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BroadcastViewModel @Inject constructor(
    private val useCases: BroadcastUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(BroadcastUiState())
    val state: StateFlow<BroadcastUiState> = _uiState

    private val _effect = MutableSharedFlow<BroadcastEffect>()
    val effect: SharedFlow<BroadcastEffect> = _effect

    private var observeEventsJob: Job? = null

    init {
        startListening()
    }

    private fun observeBroadcastEvents() {
        observeEventsJob?.cancel()
        Log.d("myString", "Starting to observe broadcast events...")
        observeEventsJob = viewModelScope.launch {
            Log.d("myString", "Inside observeBroadcastEvents coroutine, about to collect...")
            useCases.observeBroadcastEventsUseCase()
                .catch { throwable ->
                    Log.d("myString", "Error in observeBroadcastEvents: ${throwable.message}")
                    handleError("Failed to observe broadcast events: ${throwable.message}")
                }
                .collect { event ->
                    Log.d("myString","calling observeBroadcastEvents() and event is $event")
                    handleEvent(event)
                }
        }
    }

    fun startListening() {
        if (_uiState.value.isListening) return
        Log.d("myString","calling startListening()")
        viewModelScope.launch {
            try {
                // Start observing BEFORE registering receivers
                observeBroadcastEvents()

                // Then register receivers and fetch status
                useCases.registerReceiversUseCase()
                reduce { copy(isListening = true, errorMessage = null) }
                fetchInitialStatus()

                emitEffect(ShowToast("Started listening for broadcast events"))
            } catch (e: Exception) {
                handleError("Failed to start listening: ${e.message}")
            }
        }
    }

    fun stopListening() {
        if (!_uiState.value.isListening) return

        viewModelScope.launch {
            try {
                observeEventsJob?.cancel()
                useCases.unregisterReceiversUseCase()
                reduce { copy(isListening = false, errorMessage = null) }
                emitEffect(ShowToast("Stopped listening for broadcast events"))
            } catch (e: Exception) {
                handleError("Failed to stop listening: ${e.message}")
            }
        }
    }

    private fun handleEvent(event: BroadcastEvent) {

        fun updateState() {
            val old = state.value.events
            val new = old + event

            reduce {
                copy(
                    events = new,
                    lastEvent = event,
                    errorMessage = null
                )
            }
        }

        when (event) {

            // ------------------------------------------------------
            // BATTERY
            // ------------------------------------------------------

            is BroadcastEvent.BatteryLow -> {
                updateState()
                emitEffect(ShowToast("Battery is low"))
                emitEffect(PlayAlertSound(AlertType.BATTERY_LOW))
            }

            is BroadcastEvent.BatteryOkay -> {
                updateState()
                emitEffect(ShowToast("Battery is okay"))
            }

            is BroadcastEvent.BatteryLevelChanged -> {
                updateState()
                emitEffect(
                    ShowToast(
                        "Battery: ${event.level}% (charging=${event.isCharging}, via ${event.source})"
                    )
                )
            }

            is BroadcastEvent.BatteryHealth -> {
                updateState()
                emitEffect(ShowToast("Battery health: ${event.health}"))
            }

            is BroadcastEvent.BatteryTemperature -> {
                updateState()
                emitEffect(ShowToast("Battery temperature: ${event.temperatureC}°C"))
            }

            // ------------------------------------------------------
            // POWER
            // ------------------------------------------------------

            is BroadcastEvent.PowerConnected -> {
                updateState()
                emitEffect(ShowToast("Power connected (${event.source})"))
            }

            BroadcastEvent.PowerDisconnected -> {
                updateState()
                emitEffect(ShowToast("Power disconnected"))
            }

            // ------------------------------------------------------
            // WIFI
            // ------------------------------------------------------

            BroadcastEvent.WifiEnabled -> {
                updateState()
                emitEffect(ShowToast("Wi-Fi enabled"))
            }

            BroadcastEvent.WifiDisabled -> {
                updateState()
                emitEffect(ShowToast("Wi-Fi disabled"))
            }

            is BroadcastEvent.WifiConnected -> {
                updateState()
                emitEffect(
                    ShowToast(
                        "Wi-Fi connected: ${event.ssid ?: "Unknown SSID"} (${event.ipAddress})"
                    )
                )
                emitEffect(PlayAlertSound(AlertType.WIFI_CONNECTED))
            }

            BroadcastEvent.WifiDisconnected -> {
                updateState()
                emitEffect(ShowToast("Wi-Fi disconnected"))
            }

            // ------------------------------------------------------
            // MOBILE DATA
            // ------------------------------------------------------

            BroadcastEvent.MobileDataEnabled -> {
                updateState()
                emitEffect(ShowToast("Mobile data enabled"))
            }

            BroadcastEvent.MobileDataDisabled -> {
                updateState()
                emitEffect(ShowToast("Mobile data disabled"))
            }

            is BroadcastEvent.MobileDataConnected -> {
                updateState()
                emitEffect(
                    ShowToast(
                        "Mobile data: ${event.networkType}, operator=${event.operator}, roaming=${event.isRoaming}"
                    )
                )
            }

            BroadcastEvent.MobileDataDisconnected -> {
                updateState()
                emitEffect(ShowToast("Mobile data lost"))
            }

            // ------------------------------------------------------
            // INTERNET
            // ------------------------------------------------------

            is BroadcastEvent.InternetAvailable -> {
                updateState()
                emitEffect(ShowToast("Internet available (${event.type})"))
            }

            BroadcastEvent.InternetLost -> {
                updateState()
                emitEffect(ShowToast("Internet lost"))
            }

            // ------------------------------------------------------
            // AIRPLANE MODE
            // ------------------------------------------------------

            BroadcastEvent.AirplaneModeOn -> {
                updateState()
                emitEffect(ShowToast("Airplane mode ON"))
                emitEffect(PlayAlertSound(AlertType.AIRPLANE_MODE_ON))
            }

            BroadcastEvent.AirplaneModeOff -> {
                updateState()
                emitEffect(ShowToast("Airplane mode OFF"))
            }

            // ------------------------------------------------------
            // CUSTOM
            // ------------------------------------------------------

            is BroadcastEvent.CustomEvent -> {
                updateState()
                emitEffect(ShowToast("Custom event: ${event.action}"))
                emitEffect(PlayAlertSound(AlertType.CUSTOM_EVENT))
            }

            // ------------------------------------------------------
            // UNKNOWN
            // ------------------------------------------------------

            is BroadcastEvent.Unknown -> {
                updateState()
                emitEffect(ShowToast("Unknown event: ${event.rawAction}"))
            }
        }
    }

    fun sendCustomBroadcast(action: String, extras: Bundle? = null) {
        viewModelScope.launch {
            try {
                useCases.sendCustomBroadcastUseCase(action, extras)
                emitEffect(ShowToast("Custom broadcast sent: $action"))
            } catch (e: Exception) {
                handleError("Failed to send custom broadcast: ${e.message}")
            }
        }
    }

    fun clearEvents() {
        reduce { copy(events = emptyList(), lastEvent = null) }
        emitEffect(ShowToast("Events cleared"))
    }

    fun toggleListening() {
        if (_uiState.value.isListening) {
            stopListening()
        } else {
            startListening()
        }
    }

    private fun handleError(message: String) {
        reduce { copy(errorMessage = message) }
        emitEffect(ShowToast("Error: $message"))
    }

    // ----------- REDUCER -----------

    private fun reduce(block: BroadcastUiState.() -> BroadcastUiState) {
        _uiState.update(block)
    }

    // ----------- INTENTS -----------

    fun onIntent(intent: BroadcastIntent) {
        when (intent) {
            is BroadcastIntent.StartListening -> startListening()
            is BroadcastIntent.StopListening -> stopListening()
            is BroadcastIntent.ToggleListening -> toggleListening()
            is BroadcastIntent.SendCustomBroadcast -> sendCustomBroadcast(
                intent.action,
                intent.extras
            )
            is BroadcastIntent.ClearError -> cleanError()
            is BroadcastIntent.ClearEvents -> clearEvents()
        }
    }

    fun cleanError() {
        reduce { copy(errorMessage = null) }
    }

    private fun emitEffect(effect: BroadcastEffect) {
        viewModelScope.launch {
            _effect.emit(effect)
        }
    }

    private fun fetchInitialStatus() {
        viewModelScope.launch {
            try {
                Log.d("myString","calling fetchInitialStatus()")
                val initialEvents = useCases.fetchInitialStatusUseCase()
                Log.d("myString", "Initial events: $initialEvents")
                if (initialEvents.isNotEmpty()) {
                    reduce { copy(events = initialEvents) }
                    emitEffect(ShowToast("Loaded ${initialEvents.size} initial status events"))
                }
            } catch (e: Exception) {
                // Don't fail the whole operation if initial status can't be fetched
                emitEffect(ShowToast("Could not fetch initial status: ${e.message}"))
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Clean up resources when ViewModel is destroyed
        observeEventsJob?.cancel()
        if (_uiState.value.isListening) {
            viewModelScope.launch {
                try {
                    useCases.unregisterReceiversUseCase()
                } catch (e: Exception) {
                    Log.d("myStringError","error onCleared() message:$e")
                    // Log error but don't crash during cleanup
                }
            }
        }
    }
}