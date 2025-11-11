package com.taras.pet.sharehubbroadcastcenter.data.repository

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.taras.pet.sharehubbroadcastcenter.domain.model.SharedContent
import com.taras.pet.sharehubbroadcastcenter.domain.repository.ShareRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareRepositoryImpl @Inject constructor(
) : ShareRepository {

    override fun parseSharedIntent(intent: Intent): SharedContent {

        return when (intent.action) {
            null -> SharedContent.Unknown

            Intent.ACTION_SEND -> {
                handleActionSend(intent)
            }

            Intent.ACTION_SEND_MULTIPLE -> {
                handleActionSendMultiple(intent)
            }

            Intent.ACTION_VIEW -> {
                handleActionView(intent)
            }

            else -> SharedContent.Unknown
        }
    }
    private fun Intent.safeParcelableExtraUri(): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION")
            getParcelableExtra(Intent.EXTRA_STREAM)
        }
    }

    private fun handleActionSend(intent: Intent): SharedContent {
        val type = intent.type ?: return SharedContent.Unknown

        return when {
            // text and links
            type == "text/plain" -> {
                val data = intent.getStringExtra(Intent.EXTRA_TEXT) ?: "Unknown"
                if (data.startsWith("http://") || data.startsWith("https://")) {
                    SharedContent.Link(data.toUri())
                } else {
                    SharedContent.Text(data)
                }
            }

            // image
            type.startsWith("image/") -> {
                intent.safeParcelableExtraUri()?.let { SharedContent.Image(it) } ?: SharedContent.Unknown
            }

            // video
            type.startsWith("video/") -> {
                intent.safeParcelableExtraUri()?.let { SharedContent.Video(it) } ?: SharedContent.Unknown
            }

            // audio
            type.startsWith("audio/") -> {
                intent.safeParcelableExtraUri()?.let { SharedContent.Audio(it) } ?: SharedContent.Unknown
            }

            // pdf
            type == "application/pdf" -> {
                intent.safeParcelableExtraUri()?.let { SharedContent.Pdf(it) } ?: SharedContent.Unknown
            }

            else -> {
                val uri = intent.safeParcelableExtraUri()
                if (uri != null) SharedContent.File(uri, type) else SharedContent.Unknown
            }
        }
    }

    private fun handleActionView(intent: Intent): SharedContent {
        val uri = intent.data
        return if (uri?.scheme == "https" || uri?.scheme == "http") {
            SharedContent.Link(uri)
        } else SharedContent.Unknown
    }

    private fun handleActionSendMultiple(intent: Intent): SharedContent {
        val uris: ArrayList<Uri>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM)
            }
        return if (uris != null && uris.isNotEmpty()) {
            SharedContent.MultipleImages(uris)
        } else SharedContent.Unknown
    }

    override fun sendShareIntent(content: SharedContent): Intent? {
        val intent = when (content) {
            is SharedContent.Text ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content.text)
                }

            is SharedContent.Image ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.Video ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.Audio ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "audio/*"
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.Pdf ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.File ->
                Intent(Intent.ACTION_SEND).apply {
                    type = content.mimeType
                    putExtra(Intent.EXTRA_STREAM, content.uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.MultipleImages ->
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, content.uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

            is SharedContent.Link ->
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content.uri.toString())
                }

            SharedContent.Unknown -> null
        }

        return intent?.let {
            Intent.createChooser(it, "Share via")
        }
    }
}