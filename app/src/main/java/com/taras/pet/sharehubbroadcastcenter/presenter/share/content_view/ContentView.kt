package com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent

@Composable
fun ContentView(
    content: SharedContent,
    onShareAgain: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Content Type Header
        ContentTypeHeader(content = content)

        Spacer(modifier = Modifier.height(24.dp))

        // Content Display
        when (content) {
            is SharedContent.Text -> {
                TextContentView(content)
            }

            is SharedContent.Image -> {
                ImageContentView(content)
            }

            is SharedContent.Video -> {
                VideoContentView(content)
            }

            is SharedContent.Audio ->{
                AudioContentView(content)
            }

            is SharedContent.Pdf -> {
                PdfContentView(content)
            }

            is SharedContent.File -> {
                FileContentView(content)
            }

            is SharedContent.MultipleImages -> {
                MultipleImagesContentView(content)
            }

            is SharedContent.Link -> {
                LinkContentView(content)
            }

            SharedContent.Unknown -> {
                UnknownContentView()
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Share Again Button
        Button(
            onClick = onShareAgain,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share Again",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}