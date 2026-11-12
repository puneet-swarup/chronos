package com.chronos.core.scheduling

// Decides how a schedule should be executed on Android (PRD §2.2):
// - precise, time-critical schedules -> AlarmManager exact
// - flexible/background-friendly schedules -> WorkManager
// This pure function is unit-testable without Android.
enum class ExecutionMechanism { EXACT_ALARM, FLEXIBLE_WORK, EVENT_DRIVEN }

data class RoutingDecision(
 val mechanism: ExecutionMechanism,
 val reason: String,
)

object ExecutionRouter {

 // A schedule is "precise" when it must fire at an exact clock time and a
 // small delay is unacceptable (alarms and time-critical reminders).
 fun route(schedule: Schedule, requiresExact: Boolean = true): RoutingDecision {
 if (!schedule.enabled) {
 return RoutingDecision(ExecutionMechanism.EVENT_DRIVEN, "schedule disabled")
 }
 return when (schedule.kind) {
 ScheduleKind.CONDITIONAL -> RoutingDecision(
 ExecutionMechanism.EVENT_DRIVEN,
 "conditional schedules react to device events",
 )
 ScheduleKind.FIXED, ScheduleKind.RECURRENCE -> {
 if (requiresExact) {
 RoutingDecision(ExecutionMechanism.EXACT_ALARM, "exact clock-time schedule")
 } else {
 RoutingDecision(ExecutionMechanism.FLEXIBLE_WORK, "flexible background work requested")
 }
 }
 ScheduleKind.INTERVAL -> {
 // Long intervals tolerate batching; short ones need precision.
 val minutes = schedule.recurrence?.intervalMinutes ?: 60
 if (minutes <= 15) {
 RoutingDecision(ExecutionMechanism.EXACT_ALARM, "short interval needs precision")
 } else {
 RoutingDecision(ExecutionMechanism.FLEXIBLE_WORK, "long interval can be batched")
 }
 }
 ScheduleKind.RELATIVE -> RoutingDecision(
 ExecutionMechanism.EVENT_DRIVEN,
 "relative schedules are anchored to another event",
 )
 }
 }
}
