package com.chronos.core.scheduling

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit

// Expands a Recurrence into concrete occurrences. Clock-time recurrences
// (DAILY/WEEKLY/MONTHLY) are distinguished from elapsed-time INTERVAL
// recurrences, per the PRD: "every day at 8 AM" is not "every 24 hours".
class RecurrenceEngine {

 // First occurrence strictly after after, or null if none remain.
 fun nextOccurrence(recurrence: Recurrence, after: LocalDateTime): LocalDateTime? {
 return when (recurrence.type) {
 RecurrenceType.INTERVAL -> nextInterval(recurrence, after)
 else -> nextClockTime(recurrence, after)
 }
 }

 // All occurrences in the half-open range [from, to), capped at maxOccurrences.
 fun occurrencesBetween(
 recurrence: Recurrence,
 from: LocalDateTime,
 to: LocalDateTime,
 maxOccurrences: Int = 1000,
 ): List<LocalDateTime> {
 val out = ArrayList<LocalDateTime>()
 var cursor = from.minusNanos(1)
 while (out.size < maxOccurrences) {
 val next = nextOccurrence(recurrence, cursor) ?: break
 if (!next.isBefore(to)) break
 if (!next.isBefore(from)) out.add(next)
 cursor = next
 }
 return out
 }

 private fun nextInterval(recurrence: Recurrence, after: LocalDateTime): LocalDateTime? {
 val step = recurrence.intervalMinutes ?: return null
 val start = recurrence.startDate?.atStartOfDay() ?: after
 var candidate = start
 if (!candidate.isAfter(after)) {
 val minutesSinceStart = ChronoUnit.MINUTES.between(start, after)
 val steps = if (minutesSinceStart < 0) 0 else minutesSinceStart / step + 1
 candidate = start.plusMinutes(steps * step)
 }
 val endBound = recurrence.endDate?.atTime(LocalTime.MAX)
 if (endBound != null && candidate.isAfter(endBound)) return null
 return candidate
 }

 private fun nextClockTime(recurrence: Recurrence, after: LocalDateTime): LocalDateTime? {
 val time = recurrence.time ?: return null
 var date: LocalDate = maxOf(after.toLocalDate(), recurrence.startDate ?: after.toLocalDate())
 var guard = 0
 while (guard++ < 4000) {
 val endDate = recurrence.endDate
 if (endDate != null && date.isAfter(endDate)) return null
 if (recurrence.isActiveOn(date)) {
 val candidate = date.atTime(time)
 if (candidate.isAfter(after)) return candidate
 }
 date = date.plusDays(1)
 }
 return null
 }
}
