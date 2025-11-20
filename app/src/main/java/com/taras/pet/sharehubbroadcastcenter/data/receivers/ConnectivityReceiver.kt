package com.taras.pet.sharehubbroadcastcenter.data.receivers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import com.taras.pet.sharehubbroadcastcenter.domain.model.ConnectionType
import java.util.Locale

/**
 * ConnectivityReceiver listens for network state changes (Wi-Fi, mobile data, offline)
 * and transmits them as a BroadcastEvent via the onEvent callback.
 */
class ConnectivityReceiver(
    private val onEvent: (BroadcastEvent) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                handleWifiStateChanged(context, intent)
            }

            WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                handleWifiNetworkStateChanged(context, intent)
            }

            ConnectivityManager.CONNECTIVITY_ACTION -> {
                handleConnectivityChanged(context)
            }

            else -> {
                onEvent(BroadcastEvent.Unknown(intent.action))
            }
        }
    }

    private fun handleWifiStateChanged(context: Context, intent: Intent) {
        val wifiState =
            intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)

        when (wifiState) {
            WifiManager.WIFI_STATE_ENABLED -> {
                onEvent(BroadcastEvent.WifiEnabled)
            }

            WifiManager.WIFI_STATE_DISABLED -> {
                onEvent(BroadcastEvent.WifiDisabled)
            }
        }
    }

    private fun handleWifiNetworkStateChanged(context: Context, intent: Intent) {
        val networkInfo =
            intent.getParcelableExtra<android.net.NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)

        when (networkInfo?.state) {
            android.net.NetworkInfo.State.CONNECTED -> {
                handleWifiConnected(context)
            }

            android.net.NetworkInfo.State.DISCONNECTED -> {
                onEvent(BroadcastEvent.WifiDisconnected)
            }

            else -> {
                // Handle other states like CONNECTING, DISCONNECTING, SUSPENDED, UNKNOWN, or null
                // We don't need to emit events for these intermediate states
            }
        }
    }

    private fun handleWifiConnected(context: Context) {
        try {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val hasLocationPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

            if (hasLocationPermission) {
                val wifiInfo = wifiManager.connectionInfo
                val ssid = wifiInfo.ssid?.trim('"')
                val ip = formatIpAddress(wifiInfo.ipAddress)
                val speed = wifiInfo.linkSpeed
                val frequency = wifiInfo.frequency
                val mac = wifiInfo.macAddress

                onEvent(
                    BroadcastEvent.WifiConnected(
                        ssid = ssid,
                        ipAddress = ip,
                        linkSpeed = speed,
                        frequencyMHz = frequency,
                        macAddress = mac
                    )
                )
            } else {
                // Still report connection but with limited info
                onEvent(
                    BroadcastEvent.WifiConnected(
                        ssid = "Hidden (No Permission)",
                        ipAddress = "Unknown",
                        linkSpeed = 0,
                        frequencyMHz = 0,
                        macAddress = null
                    )
                )
            }
        } catch (e: SecurityException) {
            onEvent(
                BroadcastEvent.WifiConnected(
                    ssid = "SecurityException",
                    ipAddress = "Unknown",
                    linkSpeed = 0,
                    frequencyMHz = 0,
                    macAddress = null
                )
            )
        }
    }

    private fun handleConnectivityChanged(context: Context) {
        try {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)

            when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                    onEvent(BroadcastEvent.InternetAvailable(ConnectionType.WIFI))
                }

                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                    onEvent(BroadcastEvent.InternetAvailable(ConnectionType.MOBILE))
                    handleMobileDataConnected(context)
                }

                else -> {
                    onEvent(BroadcastEvent.InternetLost)
                }
            }
        } catch (e: Exception) {
            onEvent(BroadcastEvent.Unknown("Connectivity check failed: ${e.message}"))
        }
    }

    private fun handleMobileDataConnected(context: Context) {
        try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val hasPhonePermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPhonePermission) {
                val operator = telephonyManager.networkOperatorName
                val networkType = when (telephonyManager.networkType) {
                    TelephonyManager.NETWORK_TYPE_LTE -> "4G"
                    TelephonyManager.NETWORK_TYPE_NR -> "5G"
                    TelephonyManager.NETWORK_TYPE_HSPA,
                    TelephonyManager.NETWORK_TYPE_HSPAP -> "3G"
                    TelephonyManager.NETWORK_TYPE_EDGE -> "2G"
                    else -> "Mobile"
                }
                val isRoaming = telephonyManager.isNetworkRoaming

                onEvent(
                    BroadcastEvent.MobileDataConnected(
                        networkType = networkType,
                        operator = operator,
                        isRoaming = isRoaming
                    )
                )
            } else {
                onEvent(
                    BroadcastEvent.MobileDataConnected(
                        networkType = "Mobile",
                        operator = "Unknown (No Permission)",
                        isRoaming = false
                    )
                )
            }
        } catch (e: SecurityException) {
            onEvent(
                BroadcastEvent.MobileDataConnected(
                    networkType = "Mobile",
                    operator = "SecurityException",
                    isRoaming = false
                )
            )
        }
    }

    private fun formatIpAddress(ip: Int): String {
        return String.format(
            Locale.ROOT,
            "%d.%d.%d.%d",
            (ip and 0xff),
            (ip shr 8 and 0xff),
            (ip shr 16 and 0xff),
            (ip shr 24 and 0xff)
        )
    }
}