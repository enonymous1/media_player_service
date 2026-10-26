package com.enonymous1.mediaplayer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PlaylistTest {

    private fun track(id: Long, title: String) =
        Track(id, "content://media/external/audio/media/$id", title, "Artist", 180_000L)

    private val threeTracks = listOf(track(1, "One"), track(2, "Two"), track(3, "Three"))

    @Test
    fun emptyPlaylistHasNoSelectionOrNeighbours() {
        val playlist = Playlist(emptyList())
        assertEquals(-1, playlist.currentIndex)
        assertNull(playlist.currentTrack())
        assertNull(playlist.nextIndex())
        assertNull(playlist.previousIndex())
        assertNull(playlist.select(0))
    }

    @Test
    fun selectSetsCurrentTrackAndRejectsOutOfRange() {
        val playlist = Playlist(threeTracks)
        assertEquals("Two", playlist.select(1)?.title)
        assertEquals(1, playlist.currentIndex)
        assertNull(playlist.select(3))
        assertNull(playlist.select(-1))
        // A failed selection leaves the previous selection untouched.
        assertEquals(1, playlist.currentIndex)
    }

    @Test
    fun nextAndPreviousDoNotWrap() {
        val playlist = Playlist(threeTracks)
        playlist.select(0)
        assertEquals(1, playlist.nextIndex())
        assertNull(playlist.previousIndex())

        playlist.select(2)
        assertNull(playlist.nextIndex())
        assertEquals(1, playlist.previousIndex())
    }

    @Test
    fun nextIsNullBeforeAnySelection() {
        val playlist = Playlist(threeTracks)
        assertNull(playlist.nextIndex())
        assertNull(playlist.previousIndex())
    }

    @Test
    fun trackOrderIsPreserved() {
        // Note: Track.toMediaItem() is deliberately not unit-tested here: it
        // calls Uri.parse, an Android framework method that throws in plain
        // JVM tests. It is exercised on-device through MainActivity instead.
        val playlist = Playlist(threeTracks)
        assertEquals(listOf("One", "Two", "Three"), (0..2).map { playlist.trackAt(it)?.title })
        assertEquals(listOf(1L, 2L, 3L), (0..2).map { playlist.trackAt(it)?.id })
    }
}
