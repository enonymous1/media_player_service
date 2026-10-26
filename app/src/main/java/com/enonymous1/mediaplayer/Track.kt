package com.enonymous1.mediaplayer

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.util.Locale

/**
 * One local audio file, as read from MediaStore.
 *
 * The [uri] is a MediaStore content URI string, not a raw file path: that is
 * what keeps playback working under scoped storage, where the app has no
 * direct access to other apps' files. Kept as a string (rather than an
 * android.net.Uri) so this class stays usable in plain JVM unit tests.
 */
data class Track(
    val id: Long,
    val uri: String,
    val title: String,
    val artist: String,
    /** Duration in milliseconds, as reported by MediaStore. */
    val durationMs: Long,
) {
    /** The Media3 item handed to the player for this track. */
    fun toMediaItem(): MediaItem =
        MediaItem.Builder()
            .setUri(Uri.parse(uri))
            .setMediaId(id.toString())
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .build(),
            )
            .build()
}

/** Formats a millisecond duration as `m:ss`, e.g. 187_000 -> "3:07". */
fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0L) return "0:00"
    val totalSeconds = durationMs / 1000
    return String.format(Locale.US, "%d:%02d", totalSeconds / 60, totalSeconds % 60)
}
