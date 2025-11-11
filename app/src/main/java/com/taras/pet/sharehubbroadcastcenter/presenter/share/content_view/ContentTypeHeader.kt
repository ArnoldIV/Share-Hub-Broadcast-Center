package com.taras.pet.sharehubbroadcastcenter.presenter.share.content_view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TextSnippet
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent

@Composable
fun ContentTypeHeader(content: SharedContent) {
    val (icon, title, description) = when (content) {
        is SharedContent.Text -> Triple(Icons.AutoMirrored.Filled.TextSnippet, "Text", "Shared text content")
        is SharedContent.Image -> Triple(Icons.Default.Image, "Image", "Shared image file")
        is SharedContent.Video -> Triple(Icons.Default.PlayArrow, "Video", "Shared video file")
        is SharedContent.Audio -> Triple(Icons.Default.PlayArrow, "Audio", "Shared audio file")
        is SharedContent.Pdf -> Triple(Icons.Default.PictureAsPdf, "PDF", "Shared PDF document")
        is SharedContent.File -> Triple(Icons.Default.FilePresent, "File", "Shared file")
        is SharedContent.MultipleImages -> Triple(
            Icons.Default.Image,
            "Images",
            "${content.uris.size} shared images"
        )

        is SharedContent.Link -> Triple(Icons.Default.Link, "Link", "Shared web link")
        SharedContent.Unknown -> Triple(
            Icons.Default.Description,
            "Unknown",
            "Unknown content type"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}