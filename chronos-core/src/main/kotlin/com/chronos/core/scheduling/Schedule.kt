package com.chronos.core.scheduling

import java.time.LocalDate
import java.time.LocalDateTime

// A persisted schedule definition. The executor routing decision (precise
// alarm vs flexible work) is derived from kind + priority via ExecutionRouting.
data class Schedule(
 val id: String,
 val name: String,
 val kind: ScheduleKind,
 val recurrence: Recurrence? = null,
 val fixedDateTime: LocalDateTime? = null,
 val enabled: Boolean = true,
 val nextRun: LocalDateTime? = null,
) {
 init {
 when (kind) {
 ScheduleKind.FIXED -> require(fixedDateTime != null) { "FIXED schedule requires fixedDateTime" }
 ScheduleKind.RECURRENCE, ScheduleKind.INTERVAL -> require(recurrence != null) {
 "schedule kind " + kind + " requires a recurrence"
 }
 else -> { }
 }
 }

 // Computes the next fire time strictly after `after`, using the recurrence
 // engine for recurring kinds or the fixed datetime for FIXED kind.
 fun computeNext(after: LocalDateTime, engine: RecurrenceEngine = RecurrenceEngine()): LocalDateTime? {
 if (!enabled) return null
 return when (kind) {
 ScheduleKind.FIXED -> fixedDateTime?.takeIf { it.isAfter(after) }
 ScheduleKind.RECURRENCE, ScheduleKind.INTERVAL -> recurrence?.let { engine.nextOccurrence(it, after) }
 else -> null
 }
 }

 companion object {
 fun daily(id: String, name: String, time: java.time.LocalTime): Schedule = Schedule(
 id = id,
 name = name,
 kind = ScheduleKind.RECURRENCE,
 recurrence = Recurrence(type = RecurrenceType.DAILY, time = time),
 )

 fun once(id: String, name: String, at: LocalDateTime): Schedule = Schedule(
 id = id,
 name = name,
 kind = ScheduleKind.FIXED,
 fixedDateTime = at,
 )
 }
}
