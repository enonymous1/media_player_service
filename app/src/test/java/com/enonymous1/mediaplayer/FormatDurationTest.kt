package com.enonymous1.mediaplayer

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatDurationTest {

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("0:00", formatDuration(0L))
        assertEquals("0:07", formatDuration(7_000L))
        assertEquals("1:05", formatDuration(65_000L))
        assertEquals("3:07", formatDuration(187_000L))
        assertEquals("60:00", formatDuration(3_600_000L))
    }

    @Test
    fun truncatesSubSecondRemainder() {
        assertEquals("1:05", formatDuration(65_999L))
    }

    @Test
    fun negativeDurationsRenderAsZero() {
        assertEquals("0:00", formatDuration(-1L))
    }
}
