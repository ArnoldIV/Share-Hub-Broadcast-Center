package com.taras.pet.sharehubbroadcastcenter.domain.model

import android.net.Uri

sealed class SharedContent {
    data class Text(val text: String) : SharedContent()
    data class Image(val uri: Uri) : SharedContent()
    data class Video(val uri: Uri) : SharedContent()
    data class Audio(val uri: Uri) : SharedContent()
    data class Pdf(val uri: Uri) : SharedContent()
    data class MultipleImages(val uris: ArrayList<Uri>) : SharedContent()
    data class File(val uri: Uri, val mimeType: String) : SharedContent()
    data class Link(val uri: Uri) : SharedContent()
    object Unknown : SharedContent()
}