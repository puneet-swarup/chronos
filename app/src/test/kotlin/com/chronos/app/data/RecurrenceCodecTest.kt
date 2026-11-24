package com.chronos.app.data

import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// The codec is what stands between the database and the scheduler: if it is
// wrong, schedules silently stop firing. Round-trips are asserted on the domain
// object, not on the JSON text, so the format stays free to change.
class RecurrenceCodecTest {

    @Test
    fun `daily recurrence round-trips`() {
        val original = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 30))
        assertEquals(original, RecurrenceCodec.decode(RecurrenceCodec.encode(original)))
    }

    @Test
    fun `weekly recurrence keeps its weekday set`() {
        val original = Recurrence(
            type = RecurrenceType.WEEKLY,
            time = LocalTime.of(7, 15),
            weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY),
        )
        val decoded = RecurrenceCodec.decode(RecurrenceCodec.encode(original))
        assertEquals(original, decoded)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), decoded.weekdays)
    }

    @Test
    fun `monthly recurrence keeps last-day, first-working-day and day-of-month`() {
        val lastDay = Recurrence(
            type = RecurrenceType.MONTHLY,
            time = LocalTime.of(9, 0),
            lastDayOfMonth = true,
        )
        val firstWorking = Recurrence(
            type = RecurrenceType.MONTHLY,
            time = LocalTime.of(9, 0),
            firstWorkingDay = true,
        )
        val day31 = Recurrence(
            type = RecurrenceType.MONTHLY,
            time = LocalTime.of(9, 0),
            dayOfMonth = 31,
        )
        listOf(lastDay, firstWorking, day31).forEach {
            assertEquals(it, RecurrenceCodec.decode(RecurrenceCodec.encode(it)))
        }
    }

    @Test
    fun `interval recurrence keeps its period`() {
        val original = Recurrence(type = RecurrenceType.INTERVAL, intervalMinutes = 45)
        val decoded = RecurrenceCodec.decode(RecurrenceCodec.encode(original))
        assertEquals(original, decoded)
        assertEquals(45L, decoded.intervalMinutes)
    }

    @Test
    fun `bounds and exclusions survive the round trip`() {
        val original = Recurrence(
            type = RecurrenceType.DAILY,
            time = LocalTime.of(6, 0),
            startDate = LocalDate.of(2026, 11, 1),
            endDate = LocalDate.of(2027, 3, 31),
            exclusions = setOf(LocalDate.of(2026, 12, 25), LocalDate.of(2027, 1, 1)),
        )
        assertEquals(original, RecurrenceCodec.decode(RecurrenceCodec.encode(original)))
    }

    @Test
    fun `encoded text is readable ISO-8601, not opaque`() {
        val json = RecurrenceCodec.encode(
            Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 0)),
        )
        assertTrue(json.contains("\"type\":\"DAILY\""))
        assertTrue(json.contains("\"time\":\"08:00\""))
    }

    @Test
    fun `weekdays are written in calendar order regardless of set order`() {
        val json = RecurrenceCodec.encode(
            Recurrence(
                type = RecurrenceType.WEEKLY,
                time = LocalTime.of(8, 0),
                weekdays = setOf(DayOfWeek.FRIDAY, DayOfWeek.MONDAY),
            ),
        )
        assertTrue(json.indexOf("MONDAY") < json.indexOf("FRIDAY"))
    }

    @Test
    fun `an unknown recurrence type is rejected, not guessed`() {
        val bad = "{\"type\":\"HOURLY\"}"
        val error = runCatching { RecurrenceCodec.decode(bad) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }

    @Test
    fun `malformed JSON is rejected with a message`() {
        val error = runCatching { RecurrenceCodec.decode("not json at all") }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("Malformed recurrence JSON"))
    }

    @Test
    fun `a valid type with impossible data is rejected by the domain invariants`() {
        // Weekly without weekdays violates Recurrence's init block.
        val bad = "{\"type\":\"WEEKLY\",\"time\":\"08:00\",\"weekdays\":[]}"
        val error = runCatching { RecurrenceCodec.decode(bad) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
