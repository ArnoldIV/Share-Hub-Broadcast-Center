package com.taras.pet.sharehubbroadcastcenter.data.repository

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.content.ContextCompat
import com.taras.pet.sharehubbroadcastcenter.data.receivers.AirplaneModeReceiver
import com.taras.pet.sharehubbroadcastcenter.data.receivers.BatteryReceiver
import com.taras.pet.sharehubbroadcastcenter.data.receivers.ConnectivityReceiver
import com.taras.pet.sharehubbroadcastcenter.data.receivers.CustomBroadcastReceiver
import com.taras.pet.sharehubbroadcastcenter.data.receivers.PowerReceiver
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.BroadcastRepository
import com.taras.pet.sharehubbroadcastcenter.util.BroadcastUtils.Companion.ACTION_CUSTOM_EVENT
import com.taras.pet.sharehubbroadcastcenter.util.BroadcastUtils.Companion.ACTION_REFRESH
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BroadcastRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BroadcastRepository {

    private val _events = MutableSharedFlow<BroadcastEvent>(
        replay = 10, // Keep last 10 events for late subscribers
        extraBufferCapacity = 64
    )

    override val broadcastEvents: Flow<BroadcastEvent> = _events

    private val receivers = mutableListOf<BroadcastReceiver>()
    private var isRegistered = false

    // Deduplication: Keep track of recent events to avoid duplicates
    private val recentEvents = mutableMapOf<String, Long>()
    private val eventDeduplicationWindow = 2000L // 2 seconds

    private fun tryEmitEvent(event: BroadcastEvent) {
        val eventKey = when (event) {
            is BroadcastEvent.BatteryLevelChanged -> "battery_${event.level}_${event.isCharging}"
            is BroadcastEvent.WifiEnabled -> "wifi_enabled"
            is BroadcastEvent.WifiDisabled -> "wifi_disabled"
            is BroadcastEvent.AirplaneModeOn -> "airplane_on"
            is BroadcastEvent.AirplaneModeOff -> "airplane_off"
            is BroadcastEvent.InternetAvailable -> "internet_${event.type}"
            is BroadcastEvent.InternetLost -> "internet_lost"
            is BroadcastEvent.WifiConnected -> "wifi_connected_${event.ssid}"
            is BroadcastEvent.WifiDisconnected -> "wifi_disconnected"
            is BroadcastEvent.MobileDataConnected -> "mobile_${event.networkType}_${event.operator}"
            is BroadcastEvent.MobileDataDisconnected -> "mobile_disconnected"
            else -> event.toString() // For other events, use string representation
        }

        val now = System.currentTimeMillis()
        val lastEmitted = recentEvents[eventKey] ?: 0

        if (now - lastEmitted > eventDeduplicationWindow) {
            recentEvents[eventKey] = now
            val result = _events.tryEmit(event)
            Log.d("myString", "Emitted unique event: $event (result: $result)")
        } else {
            Log.d("myString", "Skipped duplicate event: $event (within ${now - lastEmitted}ms)")
        }
    }

    override fun registerReceivers() {
        if (isRegistered) return
        isRegistered = true

        Log.d("myString", "registerReceivers: Starting receiver registration")

        // ✅ Battery Receiver - Register for battery changes
        val batteryReceiver = BatteryReceiver { event ->
            Log.d("myString", "BatteryReceiver callback triggered with event: $event")
            tryEmitEvent(event)
        }
        val batteryFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_BATTERY_OKAY)
        }
        context.registerReceiver(batteryReceiver, batteryFilter)
        receivers.add(batteryReceiver)

        // ✅ Power Connection Receiver
        val powerReceiver = PowerReceiver { event ->
            Log.d("myString", "PowerReceiver callback triggered with event: $event")
            tryEmitEvent(event)
        }
        val powerFilter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
        }
        context.registerReceiver(powerReceiver, powerFilter)
        receivers.add(powerReceiver)

        // ✅ Connectivity Receiver
        val connectivityReceiver = ConnectivityReceiver { event ->
            Log.d("myString", "ConnectivityReceiver callback triggered with event: $event")
            tryEmitEvent(event)
        }
        val connectivityFilter = IntentFilter().apply {
            addAction(ConnectivityManager.CONNECTIVITY_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(connectivityReceiver, connectivityFilter)
        receivers.add(connectivityReceiver)

        // ✅ Airplane Mode Receiver
        val airplaneModeReceiver = AirplaneModeReceiver { event ->
            Log.d("myString", "AirplaneModeReceiver callback triggered with event: $event")
            tryEmitEvent(event)
        }
        val airplaneModeFilter = IntentFilter().apply {
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        context.registerReceiver(airplaneModeReceiver, airplaneModeFilter)
        receivers.add(airplaneModeReceiver)

        // ✅ Custom Receiver
        val customReceiver = CustomBroadcastReceiver { event ->
            Log.d("myString", "CustomBroadcastReceiver callback triggered with event: $event")
            tryEmitEvent(event)
        }
        val customFilter = IntentFilter().apply {
            addAction(ACTION_CUSTOM_EVENT)
            addAction(ACTION_REFRESH)
        }
        ContextCompat.registerReceiver(
            context,
            customReceiver,
            customFilter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receivers.add(customReceiver)

        // Fetch and emit initial status after all receivers are registered
        fetchCurrentBatteryStatus()
        fetchInitialSystemStatus()
    }

    override fun unregisterReceivers() {
        receivers.forEach { receiver ->
            try {
                context.unregisterReceiver(receiver)
            } catch (_: IllegalArgumentException) { }
        }
        receivers.clear()
        isRegistered = false
    }

    override fun sendCustomBroadcast(action: String, extras: Bundle?) {
        try {
            val intent = Intent(action).apply {
                extras?.let { putExtras(it) }
            }
            context.sendBroadcast(intent, null)
        } catch (e: SecurityException) {
            tryEmitEvent(BroadcastEvent.Unknown("SecurityException: ${e.message}"))
        }
    }

    private fun fetchCurrentBatteryStatus() {
        try {
            Log.d("myString", "fetchCurrentBatteryStatus: Starting battery status fetch")
            val batteryIntent =
                context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            batteryIntent?.let { intent ->
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)

                Log.d(
                    "myString",
                    "Battery raw data: level=$level, scale=$scale, status=$status, plugged=$plugged"
                )

                if (level >= 0 && scale > 0) {
                    val batteryLevel = (level * 100) / scale
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL

                    val source = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
                        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                        else -> "Battery"
                    }

                    val event = BroadcastEvent.BatteryLevelChanged(
                        level = batteryLevel,
                        isCharging = isCharging,
                        source = source
                    )

                    Log.d("myString", "Emitting battery event: $event")
                    tryEmitEvent(event)
                }
            }
        } catch (e: Exception) {
            Log.d("myString", "fetchCurrentBatteryStatus error: ${e.message}")
            tryEmitEvent(BroadcastEvent.Unknown("Failed to get battery status: ${e.message}"))
        }
    }

    private fun fetchInitialSystemStatus() {
        try {
            Log.d("myString","calling fetchInitialSystemStatus()")
            // Check airplane mode
            val isAirplaneModeOn = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON, 0
            ) != 0

            Log.d("myString", "Airplane mode status: $isAirplaneModeOn")

            val airplaneModeEvent = if (isAirplaneModeOn) {
                BroadcastEvent.AirplaneModeOn
            } else {
                BroadcastEvent.AirplaneModeOff
            }

            Log.d("myString", "Emitting airplane mode event: $airplaneModeEvent")
            tryEmitEvent(airplaneModeEvent)

            // Check WiFi state
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiEnabled = wifiManager.isWifiEnabled
            Log.d("myString", "WiFi enabled status: $wifiEnabled")

            val wifiEvent = if (wifiEnabled) {
                BroadcastEvent.WifiEnabled
            } else {
                BroadcastEvent.WifiDisabled
            }

            Log.d("myString", "Emitting WiFi event: $wifiEvent")
            tryEmitEvent(wifiEvent)

            // Check connectivity
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            Log.d(
                "myString",
                "Active network: $activeNetwork, connected: ${activeNetwork?.isConnected}"
            )

            if (activeNetwork?.isConnected == true) {
                val internetEvent = when (activeNetwork.type) {
                    ConnectivityManager.TYPE_WIFI -> {
                        BroadcastEvent.InternetAvailable(com.taras.pet.sharehubbroadcastcenter.domain.model.ConnectionType.WIFI)
                    }

                    ConnectivityManager.TYPE_MOBILE -> {
                        BroadcastEvent.InternetAvailable(com.taras.pet.sharehubbroadcastcenter.domain.model.ConnectionType.MOBILE)
                    }

                    else -> {
                        BroadcastEvent.InternetLost
                    }
                }

                Log.d("myString", "Emitting internet event: $internetEvent")
                tryEmitEvent(internetEvent)
            } else {
                val event = BroadcastEvent.InternetLost
                Log.d("myString", "Emitting internet lost event: $event")
                tryEmitEvent(event)
            }

        } catch (e: Exception) {
            Log.d("myString", "fetchInitialSystemStatus error: ${e.message}")
            tryEmitEvent(BroadcastEvent.Unknown("Failed to get initial status: ${e.message}"))
        }
    }
}