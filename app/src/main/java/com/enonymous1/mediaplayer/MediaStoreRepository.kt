package com.enonymous1.mediaplayer

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore

/**
 * Reads the device's local audio library from MediaStore.
 *
 * Only files flagged as music (`IS_MUSIC != 0`) are listed, and only files
 * with a positive duration, so zero-length or corrupt entries never reach
 * the player queue. Results are sorted by title.
 *
 * Callers must hold READ_MEDIA_AUDIO (Android 13+) or READ_EXTERNAL_STORAGE
 * (Android 12 and below) before calling this; without the permission the
 * query simply returns no rows.
 */
object MediaStoreRepository {

    fun loadTracks(context: Context): List<Track> {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        val tracks = mutableListOf<Track>()
        context.contentResolver.query(collection, projection, selection, null, sortOrder)?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (cursor.moveToNext()) {
                val durationMs = cursor.getLong(durationColumn)
                if (durationMs <= 0L) continue
                tracks += Track(
                    id = cursor.getLong(idColumn),
                    uri = ContentUris.withAppendedId(collection, cursor.getLong(idColumn)).toString(),
                    title = cursor.getString(titleColumn) ?: "Unknown track",
                    artist = cursor.getString(artistColumn) ?: "Unknown artist",
                    durationMs = durationMs,
                )
            }
        }
        return tracks
    }
}
