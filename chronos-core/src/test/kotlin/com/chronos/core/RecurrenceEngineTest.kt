package com.chronos.core

import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceEngine
import com.chronos.core.scheduling.RecurrenceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class RecurrenceEngineTest {

 private val engine = RecurrenceEngine()

 @Test
 fun dailyNextIsFollowingDayAtTime() {
 val r = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 0))
 val after = LocalDateTime.of(2026, 1, 1, 9, 0)
 assertEquals(LocalDateTime.of(2026, 1, 2, 8, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun dailySameDayStillAheadReturnsToday() {
 val r = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 0))
 val after = LocalDateTime.of(2026, 1, 1, 7, 0)
 assertEquals(LocalDateTime.of(2026, 1, 1, 8, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun weeklyRespectsWeekdays() {
 val r = Recurrence(
 type = RecurrenceType.WEEKLY,
 time = LocalTime.of(7, 0),
 weekdays = setOf(DayOfWeek.MONDAY),
 )
 // 2026-01-01 is a Thursday; next Monday is 2026-01-05.
 val after = LocalDateTime.of(2026, 1, 1, 0, 0)
 assertEquals(LocalDateTime.of(2026, 1, 5, 7, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun monthlyLastDayHandlesShortMonths() {
 val r = Recurrence(type = RecurrenceType.MONTHLY, time = LocalTime.of(0, 0), lastDayOfMonth = true)
 val after = LocalDateTime.of(2026, 1, 31, 1, 0)
 assertEquals(LocalDateTime.of(2026, 2, 28, 0, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun monthlyDay31ClampsToLastDay() {
 val r = Recurrence(type = RecurrenceType.MONTHLY, time = LocalTime.of(0, 0), dayOfMonth = 31)
 val after = LocalDateTime.of(2026, 2, 1, 0, 0)
 assertEquals(LocalDateTime.of(2026, 2, 28, 0, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun exclusionsAreSkipped() {
 val r = Recurrence(
 type = RecurrenceType.DAILY,
 time = LocalTime.of(9, 0),
 exclusions = setOf(LocalDate.of(2026, 1, 2)),
 )
 val after = LocalDateTime.of(2026, 1, 1, 10, 0)
 assertEquals(LocalDateTime.of(2026, 1, 3, 9, 0), engine.nextOccurrence(r, after))
 }

 @Test
 fun intervalIsElapsedTimeNotClockTime() {
 val r = Recurrence(type = RecurrenceType.INTERVAL, intervalMinutes = 45)
 val after = LocalDateTime.of(2026, 1, 1, 0, 0)
 assertEquals(LocalDateTime.of(2026, 1, 1, 0, 45), engine.nextOccurrence(r, after))
 }

 @Test
 fun occurrencesBetweenReturnsOrderedList() {
 val r = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 0))
 val from = LocalDateTime.of(2026, 1, 1, 0, 0)
 val to = LocalDateTime.of(2026, 1, 4, 0, 0)
 val list = engine.occurrencesBetween(r, from, to)
 assertEquals(3, list.size)
 assertTrue(list[0].isBefore(list[1]))
 }

 @Test
 fun endDateStopsRecurrence() {
 val r = Recurrence(
 type = RecurrenceType.DAILY,
 time = LocalTime.of(8, 0),
 endDate = LocalDate.of(2026, 1, 2),
 )
 val after = LocalDateTime.of(2026, 1, 2, 9, 0)
 assertEquals(null, engine.nextOccurrence(r, after))
 }
}
