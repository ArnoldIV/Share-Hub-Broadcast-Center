package com.taras.pet.sharehubbroadcastcenter.presenter.share

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view.ContentView
import com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view.content_states_view.EmptyView
import com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view.content_states_view.ErrorView
import com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view.content_states_view.LoadingView
import kotlinx.coroutines.flow.collectLatest

@Composable
fun ShareScreen(
    viewModel: ShareViewModel = hiltViewModel(),
    intent: Intent? = null
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle incoming intent when screen is first composed
    LaunchedEffect(intent) {
        val targetIntent = intent ?: (context as? androidx.activity.ComponentActivity)?.intent
        targetIntent?.let { intentToProcess ->
            if (isShareIntent(intentToProcess)) {
                viewModel.onIntent(ShareIntent.ParseIncomingIntent(intentToProcess))
            }
        }
    }

    // Handle side effects
    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ShareEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }

                is ShareEffect.OpenChooser -> {
                    try {
                        context.startActivity(effect.intent)
                    } catch (_: Exception) {
                        Toast.makeText(
                            context,
                            "No app available to handle this content",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isSharing -> {
                LoadingView()
            }

            state.errorMessage != null -> {
                ErrorView(
                    message = state.errorMessage!!,
                    onRetry = { viewModel.cleanError() }
                )
            }

            state.isEmpty -> {
                EmptyView()
            }

            state.sharedContent != null -> {
                ContentView(
                    content = state.sharedContent!!,
                    onShareAgain = { viewModel.onIntent(
                        ShareIntent.ShareAgainClicked(state.sharedContent!!))
                    }
                )
            }
        }
    }
}

/**
 * Check if the intent is a share intent
 */
private fun isShareIntent(intent: Intent): Boolean {
    return when (intent.action) {
        Intent.ACTION_SEND,
        Intent.ACTION_SEND_MULTIPLE,
        Intent.ACTION_VIEW -> true

        else -> false
    }
}

@Preview(showBackground = true)
@Composable
fun UnknownContentTypePreview() {
    EmptyView()
}