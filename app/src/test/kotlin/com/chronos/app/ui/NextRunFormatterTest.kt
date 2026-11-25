package com.chronos.app.ui

import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NextRunFormatterTest {

    private val now = LocalDateTime.of(2026, 11, 25, 9, 0)

    @Test
    fun `nothing scheduled formats to null`() {
        assertNull(NextRunFormatter.format(null, now))
    }

    @Test
    fun `a run later today reads as Today`() {
        assertEquals("Today 18:30", NextRunFormatter.format(LocalDateTime.of(2026, 11, 25, 18, 30), now))
    }

    @Test
    fun `tomorrow reads as Tomorrow`() {
        assertEquals("Tomorrow 07:00", NextRunFormatter.format(LocalDateTime.of(2026, 11, 26, 7, 0), now))
    }

    @Test
    fun `later in the week shows the weekday and date`() {
        val label = NextRunFormatter.format(LocalDateTime.of(2026, 11, 30, 8, 0), now)
        assertEquals("Mon 30 Nov 08:00", label)
    }

    @Test
    fun `a past timestamp is called overdue rather than next`() {
        val label = NextRunFormatter.format(LocalDateTime.of(2026, 11, 20, 8, 0), now)
        assertEquals(true, label!!.startsWith("Overdue"))
    }

    @Test
    fun `soonest ignores past runs and picks the earliest future one`() {
        val runs = listOf(
            LocalDateTime.of(2026, 11, 20, 8, 0),
            LocalDateTime.of(2026, 11, 27, 8, 0),
            LocalDateTime.of(2026, 11, 26, 6, 0),
            null,
        )
        assertEquals(LocalDateTime.of(2026, 11, 26, 6, 0), NextRunFormatter.soonest(runs, now))
    }

    @Test
    fun `soonest is null when everything is in the past`() {
        assertNull(NextRunFormatter.soonest(listOf(LocalDateTime.of(2026, 11, 20, 8, 0)), now))
    }

    @Test
    fun `a run due right now still counts`() {
        assertEquals(now, NextRunFormatter.soonest(listOf(now), now))
    }
}
