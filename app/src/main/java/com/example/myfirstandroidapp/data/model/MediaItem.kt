package com.gallery.data.model

import android.net.Uri
import com.gallery.data.local.database.MediaItemEntity

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

fun MediaItemEntity.toMediaItem(): MediaItem {
    return MediaItem(
        id = id,
        uri = Uri.parse(uri),
        name = name,
        dateAdded = dateAdded,
        width = width,
        height = height,
        type = if (type == "IMAGE") MediaType.IMAGE else MediaType.VIDEO,
        mimeType = mimeType,
        category = try { MediaCategory.valueOf(category) } catch(e: Exception) { MediaCategory.NONE }
    )
}
