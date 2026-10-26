package com.enonymous1.mediaplayer

/**
 * The ordered track list and the current position in it.
 *
 * Kept deliberately free of Android and Media3 types so the navigation rules
 * (no wrapping, no out-of-bounds index) are unit-testable on the JVM.
 * The player itself owns the actual playback queue; this class is the UI's
 * source of truth for "which track is selected, and what comes next".
 */
class Playlist(private val tracks: List<Track>) {

    /** Index of the currently selected track, or -1 when nothing is selected. */
    var currentIndex: Int = -1
        private set

    val size: Int get() = tracks.size

    val isEmpty: Boolean get() = tracks.isEmpty()

    fun trackAt(index: Int): Track? = tracks.getOrNull(index)

    fun currentTrack(): Track? = trackAt(currentIndex)

    /** Selects [index]; returns the track, or null when [index] is out of range. */
    fun select(index: Int): Track? {
        val track = trackAt(index) ?: return null
        currentIndex = index
        return track
    }

    /** Index of the next track, or null when at (or past) the end. No wrapping. */
    fun nextIndex(): Int? {
        if (currentIndex < 0 || currentIndex + 1 >= tracks.size) return null
        return currentIndex + 1
    }

    /** Index of the previous track, or null when at (or before) the start. */
    fun previousIndex(): Int? {
        if (currentIndex <= 0) return null
        return currentIndex - 1
    }

    /** All tracks as Media3 items, in display order, for the player queue. */
    fun toMediaItems() = tracks.map { it.toMediaItem() }
}
