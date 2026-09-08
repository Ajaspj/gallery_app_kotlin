package com.gallery.data.model

import android.net.Uri

enum class MediaType {
    IMAGE, VIDEO
}

enum class MediaCategory {
    PEOPLE, PETS, DOCUMENTS, SCREENSHOTS, NONE
}

data class MediaItem(
    val id: Long,
    val uri: Uri,
    val name: String,
    val dateAdded: Long,
    val width: Int,
    val height: Int,
    val type: MediaType,
    val duration: Long = 0,
    val mimeType: String? = null,
    val category: MediaCategory = MediaCategory.NONE
)