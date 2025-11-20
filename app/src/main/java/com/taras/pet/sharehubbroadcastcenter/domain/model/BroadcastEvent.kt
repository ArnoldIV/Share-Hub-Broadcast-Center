package com.taras.pet.sharehubbroadcastcenter.domain.model

import android.os.Bundle

sealed class BroadcastEvent {

// ------------------------------------------------------
    // BATTERY EVENTS
    // ------------------------------------------------------

    data class BatteryLevelChanged(
        val level: Int,                // %, 0–100
        val isCharging: Boolean,       // charging or not
        val source: String             // AC / USB / WIRELESS / UNKNOWN
    ) : BroadcastEvent()

    object BatteryLow : BroadcastEvent()
    object BatteryOkay : BroadcastEvent()

    // Battery health
    data class BatteryHealth(
        val health: BatteryHealthType  // GOOD, OVERHEAT, DEAD, COLD, etc.
    ) : BroadcastEvent()

    // Battery temperature (°C)
    data class BatteryTemperature(
        val temperatureC: Float
    ) : BroadcastEvent()

    // ------------------------------------------------------
    // POWER EVENTS
    // ------------------------------------------------------

    data class PowerConnected(
        val source: String             // AC / USB / WIRELESS
    ) : BroadcastEvent()

    object PowerDisconnected : BroadcastEvent()

    // ------------------------------------------------------
    // WIFI EVENTS
    // ------------------------------------------------------

    // Base wifi state
    object WifiEnabled : BroadcastEvent()
    object WifiDisabled : BroadcastEvent()

    data class WifiConnected(
        val ssid: String?,
        val ipAddress: String?,
        val linkSpeed: Int,            // Mbps
        val frequencyMHz: Int,
        val macAddress: String?
    ) : BroadcastEvent()

    object WifiDisconnected : BroadcastEvent()

    // ------------------------------------------------------
    // MOBILE DATA EVENTS
    // ------------------------------------------------------

    object MobileDataEnabled : BroadcastEvent()
    object MobileDataDisabled : BroadcastEvent()

    data class MobileDataConnected(
        val networkType: String,       // LTE, 5G, 3G, etc.
        val operator: String?,
        val isRoaming: Boolean
    ) : BroadcastEvent()

    object MobileDataDisconnected : BroadcastEvent()

    // ------------------------------------------------------
    // INTERNET STATE
    // ------------------------------------------------------

    data class InternetAvailable(
        val type: ConnectionType       // WIFI / MOBILE
    ) : BroadcastEvent()

    object InternetLost : BroadcastEvent()

    // ------------------------------------------------------
    // AIRPLANE MODE
    // ------------------------------------------------------

    object AirplaneModeOn : BroadcastEvent()
    object AirplaneModeOff : BroadcastEvent()

    // ------------------------------------------------------
    // CUSTOM BROADCASTS
    // ------------------------------------------------------

    data class CustomEvent(
        val action: String,
        val extras: Bundle?
    ) : BroadcastEvent()

    // ------------------------------------------------------
    // UNKNOWN
    // ------------------------------------------------------

    data class Unknown(
        val rawAction: String?
    ) : BroadcastEvent()
}

enum class BatteryHealthType {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    COLD,
    UNKNOWN
}

enum class ConnectionType {
    WIFI,
    MOBILE
}