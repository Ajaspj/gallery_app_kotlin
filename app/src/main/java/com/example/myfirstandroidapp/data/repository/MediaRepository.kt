package com.gallery.data.repository

import android.content.ContentResolver
import android.net.Uri
import android.provider.MediaStore
import com.gallery.data.model.MediaCategory
import com.gallery.data.model.MediaItem
import com.gallery.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MediaRepository(
    private val contentResolver: ContentResolver
) {

    suspend fun getMedia(): List<MediaItem> =
        withContext(Dispatchers.IO) {
            val mediaItems = mutableListOf<MediaItem>()

            mediaItems.addAll(fetchFromUri(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                MediaType.IMAGE
            ))

            mediaItems.addAll(fetchFromUri(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                MediaType.VIDEO
            ))

            mediaItems.sortedByDescending { it.dateAdded }
        }

    private fun fetchFromUri(
        collection: Uri,
        type: MediaType
    ): List<MediaItem> {
        val items = mutableListOf<MediaItem>()

        val projection = mutableListOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.DATE_ADDED,
            MediaStore.MediaColumns.WIDTH,
            MediaStore.MediaColumns.HEIGHT,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATA
        )

        if (type == MediaType.VIDEO) {
            projection.add(MediaStore.Video.VideoColumns.DURATION)
        }

        contentResolver.query(
            collection,
            projection.toTypedArray(),
            null,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
            val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)
            val widthCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
            val heightCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
            val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
            val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            val durationCol = if (type == MediaType.VIDEO) {
                cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION)
            } else -1

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(collection, id.toString())
                val path = cursor.getString(dataCol)
                val mimeType = cursor.getString(mimeCol)
                val name = cursor.getString(nameCol)

                val category = when {
                    path?.contains("Screenshots", ignoreCase = true) == true -> MediaCategory.SCREENSHOTS
                    mimeType?.contains("pdf", ignoreCase = true) == true || 
                            mimeType?.contains("document", ignoreCase = true) == true -> MediaCategory.DOCUMENTS
                    // Mock stubs for People/Pets based on random ID for demo purposes
                    id % 15 == 0L -> MediaCategory.PEOPLE
                    id % 20 == 0L -> MediaCategory.PETS
                    else -> MediaCategory.NONE
                }

                items.add(
                    MediaItem(
                        id = id,
                        uri = uri,
                        name = name,
                        dateAdded = cursor.getLong(dateCol),
                        width = cursor.getInt(widthCol),
                        height = cursor.getInt(heightCol),
                        type = type,
                        duration = if (durationCol != -1) cursor.getLong(durationCol) else 0L,
                        mimeType = mimeType,
                        category = category
                    )
                )
            }
        }
        return items
    }
}