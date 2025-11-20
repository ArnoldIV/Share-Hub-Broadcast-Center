package com.taras.pet.sharehubbroadcastcenter.presenter.broadcast

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taras.pet.sharehubbroadcastcenter.presenter.broadcast.components.BroadcastCard
import com.taras.pet.sharehubbroadcastcenter.presenter.broadcast.components.BroadcastToolbar
import com.taras.pet.sharehubbroadcastcenter.util.AlertType
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID


@Composable
fun BroadcastScreen(
    viewModel: BroadcastViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val listState = rememberLazyListState()

    var showCustomBroadcastDialog by remember { mutableStateOf(false) }

    // Handle effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is BroadcastEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is BroadcastEffect.PlayAlertSound -> {
                    playAlertSound(context, effect.type)
                }
            }
        }
    }

    // Auto-scroll to latest event
    LaunchedEffect(state.events.size) {
        if (state.events.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCustomBroadcastDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Send Custom Broadcast"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Toolbar
            BroadcastToolbar(
                isListening = state.isListening,
                eventCount = state.events.size,
                onToggleListening = {
                    viewModel.onIntent(BroadcastIntent.ToggleListening)
                },
                onClearEvents = {
                    viewModel.onIntent(BroadcastIntent.ClearEvents)
                }
            )

            // Error Message
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )

                        TextButton(
                            onClick = {
                                viewModel.onIntent(BroadcastIntent.ClearError)
                            }
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            // Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    state.isEmpty -> {
                        // Empty state
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (state.isListening)
                                    "Listening for broadcast events..."
                                else
                                    "Tap Start to begin listening for system events",
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "System events like battery changes, WiFi connections, and more will appear here.",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    else -> {
                        // Events list
                        LazyColumn(
                            state = listState,
                            reverseLayout = true, // Show newest events at top
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item { Spacer(modifier = Modifier.height(8.dp)) }

                            items(
                                items = state.events.reversed(),
                                key = { UUID.randomUUID().toString() }
                            ) { event ->
                                BroadcastCard(
                                    event = event,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }

    // Custom Broadcast Dialog
    if (showCustomBroadcastDialog) {
        CustomBroadcastDialog(
            onDismiss = { showCustomBroadcastDialog = false },
            onSend = { action ->
                viewModel.onIntent(BroadcastIntent.SendCustomBroadcast(action))
                showCustomBroadcastDialog = false
            }
        )
    }
}

@Composable
private fun CustomBroadcastDialog(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit
) {
    var customAction by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Send Custom Broadcast",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Enter the action name for the custom broadcast:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = customAction,
                    onValueChange = { customAction = it },
                    label = { Text("Action") },
                    placeholder = { Text("com.example.MY_CUSTOM_ACTION") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSend(customAction) },
                enabled = customAction.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

private fun playAlertSound(context: android.content.Context, alertType: AlertType) {
    try {
        // You can customize sound resources based on alert type
        val soundResource = when (alertType) {
            AlertType.BATTERY_LOW -> android.media.RingtoneManager.TYPE_NOTIFICATION
            AlertType.AIRPLANE_MODE_ON -> android.media.RingtoneManager.TYPE_NOTIFICATION
            AlertType.WIFI_CONNECTED -> android.media.RingtoneManager.TYPE_NOTIFICATION
            AlertType.CUSTOM_EVENT -> android.media.RingtoneManager.TYPE_NOTIFICATION
        }

        val ringtone = android.media.RingtoneManager.getRingtone(
            context,
            android.media.RingtoneManager.getDefaultUri(soundResource)
        )
        ringtone?.play()
    } catch (e: Exception) {
        // Ignore sound errors
    }
}