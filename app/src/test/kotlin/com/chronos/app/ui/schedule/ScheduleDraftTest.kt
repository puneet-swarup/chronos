package com.chronos.app.ui.schedule

import com.chronos.core.scheduling.RecurrenceType
import com.chronos.core.scheduling.ScheduleKind
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleDraftTest {

    private val now = LocalDateTime.of(2026, 11, 25, 9, 30) // a Wednesday

    @Test
    fun `a blank name is rejected before anything is armed`() {
        val errors = ScheduleDraft(name = "   ").validate()
        assertEquals(listOf("Give the schedule a name"), errors)
    }

    @Test
    fun `a complete draft has no errors`() {
        assertTrue(ScheduleDraft(name = "Morning run").validate().isEmpty())
    }

    @Test
    fun `a daily draft becomes a daily recurrence at the chosen time`() {
        val schedule = ScheduleDraft(name = "Morning run", hour = 6, minute = 15, repeat = RepeatOption.DAILY)
            .toSchedule("id-1", now)

        assertEquals(ScheduleKind.RECURRENCE, schedule.kind)
        val recurrence = schedule.recurrence!!
        assertEquals(RecurrenceType.DAILY, recurrence.type)
        assertEquals(LocalTime.of(6, 15), recurrence.time)
        assertEquals("Morning run", schedule.name)
        assertEquals("id-1", schedule.id)
    }

    @Test
    fun `weekdays covers monday to friday`() {
        val schedule = ScheduleDraft(name = "Standup", repeat = RepeatOption.WEEKDAYS).toSchedule("id-2", now)
        val weekdays = schedule.recurrence!!.weekdays
        assertEquals(
            setOf(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY,
            ),
            weekdays,
        )
    }

    @Test
    fun `weekly uses the weekday the draft was created on`() {
        val schedule = ScheduleDraft(name = "Review", repeat = RepeatOption.WEEKLY).toSchedule("id-3", now)
        val weekdays = schedule.recurrence!!.weekdays
        assertEquals(setOf(DayOfWeek.WEDNESDAY), weekdays)
    }

    @Test
    fun `a one-off later today stays today`() {
        val schedule = ScheduleDraft(name = "Call", hour = 18, minute = 0, repeat = RepeatOption.ONCE)
            .toSchedule("id-4", now)
        assertEquals(ScheduleKind.FIXED, schedule.kind)
        assertEquals(LocalDateTime.of(2026, 11, 25, 18, 0), schedule.fixedDateTime)
        assertNull(schedule.recurrence)
    }

    @Test
    fun `a one-off whose time has passed moves to tomorrow`() {
        val schedule = ScheduleDraft(name = "Call", hour = 7, minute = 0, repeat = RepeatOption.ONCE)
            .toSchedule("id-5", now)
        assertEquals(LocalDateTime.of(2026, 11, 26, 7, 0), schedule.fixedDateTime)
    }

    @Test
    fun `stepping wraps instead of leaving the valid range`() {
        val draft = ScheduleDraft(hour = 23, minute = 55)
        assertEquals(0, draft.withHourStep(1).hour)
        assertEquals(0, draft.withMinuteStep(5).minute)
    }

    @Test
    fun `the time label is zero padded`() {
        assertEquals("07:05", ScheduleDraft(hour = 7, minute = 5).timeLabel)
    }
}
