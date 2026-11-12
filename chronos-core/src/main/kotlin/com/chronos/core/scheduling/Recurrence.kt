package com.chronos.core.scheduling

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth

// A calendar recurrence rule. The recurrence engine expands this into concrete
// occurrences; it deliberately does not depend on Android so it is unit-testable
// on any JVM. Exactly one type is active at a time. startDate/endDate bound
// the overall validity window (both inclusive; null means unbounded).
// exclusions is a set of dates (e.g. holiday calendar) that are skipped.
data class Recurrence(
 val type: RecurrenceType,
 // Time of day for calendar recurrences (may be null for interval rules).
 val time: LocalTime? = null,
 // Weekly: the set of active weekdays.
 val weekdays: Set<DayOfWeek> = emptySet(),
 // Monthly: a specific day-of-month (1..31).
 val dayOfMonth: Int? = null,
 // Monthly: run on the last day of the month.
 val lastDayOfMonth: Boolean = false,
 // Monthly: run on the first working day (Mon-Fri) of the month.
 val firstWorkingDay: Boolean = false,
 // Interval: the period between occurrences. Only for RecurrenceType.INTERVAL.
 val intervalMinutes: Long? = null,
 val startDate: LocalDate? = null,
 val endDate: LocalDate? = null,
 val exclusions: Set<LocalDate> = emptySet(),
) {
 init {
 when (type) {
 RecurrenceType.DAILY, RecurrenceType.WEEKLY,
 RecurrenceType.MONTHLY -> require(time != null) {
 "Calendar recurrence $type requires a time of day"
 }
 RecurrenceType.INTERVAL -> require(intervalMinutes != null && intervalMinutes > 0) {
 "Interval recurrence requires a positive intervalMinutes"
 }
 }
 if (type == RecurrenceType.WEEKLY) {
 require(weekdays.isNotEmpty()) { "Weekly recurrence requires at least one weekday" }
 }
 if (dayOfMonth != null) {
 require(dayOfMonth in 1..31) { "dayOfMonth must be in 1..31" }
 }
 require(intervalMinutes == null || intervalMinutes > 0) {
 "intervalMinutes must be positive when set"
 }
 if (startDate != null && endDate != null) {
 require(!endDate.isBefore(startDate)) { "endDate must not be before startDate" }
 }
 }

 fun isActiveOn(date: LocalDate): Boolean {
 if (startDate != null && date.isBefore(startDate)) return false
 if (endDate != null && date.isAfter(endDate)) return false
 if (date in exclusions) return false
 return when (type) {
 RecurrenceType.DAILY -> true
 RecurrenceType.WEEKLY -> date.dayOfWeek in weekdays
 RecurrenceType.MONTHLY -> matchesMonthly(date)
 RecurrenceType.INTERVAL -> true
 }
 }

 private fun matchesMonthly(date: LocalDate): Boolean {
 val ym = YearMonth.from(date)
 if (lastDayOfMonth && date == ym.atEndOfMonth()) return true
 if (firstWorkingDay) {
 var d = ym.atDay(1)
 while (d.dayOfWeek == DayOfWeek.SATURDAY || d.dayOfWeek == DayOfWeek.SUNDAY) {
 d = d.plusDays(1)
 }
 if (date == d) return true
 }
 if (dayOfMonth != null) {
 val target = if (dayOfMonth <= ym.lengthOfMonth()) {
 ym.atDay(dayOfMonth)
 } else {
 ym.atEndOfMonth()
 }
 if (date == target) return true
 }
 return false
 }
}
