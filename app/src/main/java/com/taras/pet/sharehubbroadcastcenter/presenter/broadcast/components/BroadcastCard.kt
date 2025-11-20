package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryUnknown
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.PowerOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taras.pet.sharehubbroadcastcenter.domain.model.BroadcastEvent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BroadcastCard(
    event: BroadcastEvent,
    modifier: Modifier = Modifier
) {
    val (icon, color, title, description) = getEventDetails(event)
    val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class EventDetails(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val description: String
)

@Composable
private fun getEventDetails(event: BroadcastEvent): EventDetails {
    return when (event) {
        // Battery Events
        is BroadcastEvent.BatteryLevelChanged -> EventDetails(
            icon = getBatteryIcon(event.level),
            color = getBatteryColor(event.level, event.isCharging),
            title = "Battery Level: ${event.level}%",
            description = "Charging: ${if (event.isCharging) "Yes" else "No"} • Source: ${event.source}"
        )

        is BroadcastEvent.BatteryLow -> EventDetails(
            icon = Icons.Default.BatteryAlert,
            color = Color.Red,
            title = "Battery Low",
            description = "Device battery is running low"
        )

        is BroadcastEvent.BatteryOkay -> EventDetails(
            icon = Icons.Default.BatteryFull,
            color = Color.Green,
            title = "Battery Okay",
            description = "Battery level is now sufficient"
        )

        is BroadcastEvent.BatteryHealth -> EventDetails(
            icon = Icons.Default.BatteryFull,
            color = if (event.health.name == "GOOD") Color.Green else Color.hsl(33f, 1f, 0.5f),
            title = "Battery Health",
            description = "Status: ${event.health.name}"
        )

        is BroadcastEvent.BatteryTemperature -> EventDetails(
            icon = Icons.Default.BatteryFull,
            color = getTemperatureColor(event.temperatureC),
            title = "Battery Temperature",
            description = "${event.temperatureC}°C"
        )

        // Power Events
        is BroadcastEvent.PowerConnected -> EventDetails(
            icon = Icons.Default.Power,
            color = Color.Green,
            title = "Power Connected",
            description = "Source: ${event.source}"
        )

        is BroadcastEvent.PowerDisconnected -> EventDetails(
            icon = Icons.Default.PowerOff,
            color = Color.hsl(33f, 1f, 0.5f),
            title = "Power Disconnected",
            description = "Device is now running on battery"
        )

        // WiFi Events
        is BroadcastEvent.WifiEnabled -> EventDetails(
            icon = Icons.Default.Wifi,
            color = Color.Blue,
            title = "WiFi Enabled",
            description = "WiFi has been turned on"
        )

        is BroadcastEvent.WifiDisabled -> EventDetails(
            icon = Icons.Default.WifiOff,
            color = Color.Gray,
            title = "WiFi Disabled",
            description = "WiFi has been turned off"
        )

        is BroadcastEvent.WifiConnected -> EventDetails(
            icon = Icons.Default.Wifi,
            color = Color.Green,
            title = "WiFi Connected",
            description = "Network: ${event.ssid ?: "Hidden"} • IP: ${event.ipAddress ?: "Unknown"}"
        )

        is BroadcastEvent.WifiDisconnected -> EventDetails(
            icon = Icons.Default.WifiOff,
            color = Color.Red,
            title = "WiFi Disconnected",
            description = "Lost connection to WiFi network"
        )

        // Mobile Data Events
        is BroadcastEvent.MobileDataEnabled -> EventDetails(
            icon = Icons.Default.PhoneAndroid,
            color = Color.Blue,
            title = "Mobile Data Enabled",
            description = "Mobile data has been turned on"
        )

        is BroadcastEvent.MobileDataDisabled -> EventDetails(
            icon = Icons.Default.PhoneAndroid,
            color = Color.Gray,
            title = "Mobile Data Disabled",
            description = "Mobile data has been turned off"
        )

        is BroadcastEvent.MobileDataConnected -> EventDetails(
            icon = Icons.Default.PhoneAndroid,
            color = Color.Green,
            title = "Mobile Data Connected",
            description = "${event.networkType} • ${event.operator ?: "Unknown"} • Roaming: ${if (event.isRoaming) "Yes" else "No"}"
        )

        is BroadcastEvent.MobileDataDisconnected -> EventDetails(
            icon = Icons.Default.PhoneAndroid,
            color = Color.Red,
            title = "Mobile Data Disconnected",
            description = "Lost mobile data connection"
        )

        // Internet Events
        is BroadcastEvent.InternetAvailable -> EventDetails(
            icon = Icons.Default.Wifi,
            color = Color.Green,
            title = "Internet Available",
            description = "Connection type: ${event.type.name}"
        )

        is BroadcastEvent.InternetLost -> EventDetails(
            icon = Icons.Default.WifiOff,
            color = Color.Red,
            title = "Internet Lost",
            description = "No internet connection available"
        )

        // Airplane Mode Events
        is BroadcastEvent.AirplaneModeOn -> EventDetails(
            icon = Icons.Default.FlightTakeoff,
            color = Color.hsl(33f, 1f, 0.5f),
            title = "Airplane Mode On",
            description = "All wireless connections disabled"
        )

        is BroadcastEvent.AirplaneModeOff -> EventDetails(
            icon = Icons.Default.FlightTakeoff,
            color = Color.Green,
            title = "Airplane Mode Off",
            description = "Wireless connections enabled"
        )

        // Custom Events
        is BroadcastEvent.CustomEvent -> EventDetails(
            icon = Icons.Default.Power, // Generic icon for custom events
            color = Color.Magenta,
            title = "Custom Event",
            description = "Action: ${event.action}"
        )

        // Unknown Events
        is BroadcastEvent.Unknown -> EventDetails(
            icon = Icons.Default.BatteryUnknown,
            color = Color.Gray,
            title = "Unknown Event",
            description = "Action: ${event.rawAction ?: "N/A"}"
        )
    }
}

@Composable
private fun getBatteryIcon(level: Int): ImageVector {
    return when {
        level <= 10 -> Icons.Default.Battery0Bar
        level <= 20 -> Icons.Default.Battery1Bar
        level <= 30 -> Icons.Default.Battery2Bar
        level <= 50 -> Icons.Default.Battery3Bar
        level <= 60 -> Icons.Default.Battery4Bar
        level <= 80 -> Icons.Default.Battery5Bar
        level <= 90 -> Icons.Default.Battery6Bar
        else -> Icons.Default.BatteryFull
    }
}

@Composable
private fun getBatteryColor(level: Int, isCharging: Boolean): Color {
    return when {
        isCharging -> Color.Green
        level <= 15 -> Color.Red
        level <= 30 -> Color.hsl(33f, 1f, 0.5f)
        else -> Color.Green
    }
}

@Composable
private fun getTemperatureColor(tempC: Float): Color {
    return when {
        tempC > 45f -> Color.Red
        tempC > 35f -> Color.hsl(33f, 1f, 0.5f)
        tempC < 0f -> Color.Blue
        else -> Color.Green
    }
}