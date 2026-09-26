package com.choirscorepractice.core.score

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class NoteEventTest {
    @Test fun `preserves fractional beats for playback timing`() {
        val note = NoteEvent(60, 1.5, 0.5)
        assertEquals(1.5, note.startBeat, 0.0)
        assertEquals(0.5, note.durationBeats, 0.0)
    }

    @Test fun `rejects invalid pitches and timing before playback`() {
        for (pitch in listOf(-1, 128)) {
            assertThrows(IllegalArgumentException::class.java) { NoteEvent(pitch, 0.0, 1.0) }
        }
        for (start in listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { NoteEvent(60, start, 1.0) }
        }
        for (duration in listOf(0.0, -1.0, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(IllegalArgumentException::class.java) { NoteEvent(60, 0.0, duration) }
        }
    }
}
