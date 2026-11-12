package com.chronos.core

import com.chronos.core.scheduling.ExecutionMechanism
import com.chronos.core.scheduling.ExecutionRouter
import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import com.chronos.core.scheduling.Schedule
import com.chronos.core.scheduling.ScheduleKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime
import java.time.LocalTime

class ScheduleAndRoutingTest {

 @Test
 fun onceComputesNextOnlyIfInFuture() {
 val at = LocalDateTime.of(2026, 1, 1, 8, 0)
 val s = Schedule.once("s1", "wake", at)
 assertEquals(at, s.computeNext(LocalDateTime.of(2026, 1, 1, 7, 0)))
 assertNull(s.computeNext(LocalDateTime.of(2026, 1, 1, 9, 0)))
 }

 @Test
 fun dailyComputesNextOccurrence() {
 val s = Schedule.daily("s2", "meds", LocalTime.of(9, 0))
 val next = s.computeNext(LocalDateTime.of(2026, 1, 1, 10, 0))
 assertEquals(LocalDateTime.of(2026, 1, 2, 9, 0), next)
 }

 @Test
 fun disabledScheduleHasNoNext() {
 val s = Schedule.daily("s3", "x", LocalTime.NOON).copy(enabled = false)
 assertNull(s.computeNext(LocalDateTime.of(2026, 1, 1, 0, 0)))
 }

 @Test
 fun fixedRoutesToExactAlarm() {
 val s = Schedule.once("s", "n", LocalDateTime.of(2026, 1, 1, 8, 0))
 assertEquals(ExecutionMechanism.EXACT_ALARM, ExecutionRouter.route(s).mechanism)
 }

 @Test
 fun fixedRoutesToFlexibleWhenNotExact() {
 val s = Schedule.once("s", "n", LocalDateTime.of(2026, 1, 1, 8, 0))
 assertEquals(ExecutionMechanism.FLEXIBLE_WORK, ExecutionRouter.route(s, requiresExact = false).mechanism)
 }

 @Test
 fun shortIntervalRoutesToExact() {
 val s = Schedule("i1", "poll", ScheduleKind.INTERVAL, Recurrence(RecurrenceType.INTERVAL, intervalMinutes = 5))
 assertEquals(ExecutionMechanism.EXACT_ALARM, ExecutionRouter.route(s).mechanism)
 }

 @Test
 fun longIntervalRoutesToFlexible() {
 val s = Schedule("i2", "backup", ScheduleKind.INTERVAL, Recurrence(RecurrenceType.INTERVAL, intervalMinutes = 120))
 assertEquals(ExecutionMechanism.FLEXIBLE_WORK, ExecutionRouter.route(s).mechanism)
 }

 @Test
 fun conditionalRoutesToEventDriven() {
 val s = Schedule("c1", "batt", ScheduleKind.CONDITIONAL)
 assertEquals(ExecutionMechanism.EVENT_DRIVEN, ExecutionRouter.route(s).mechanism)
 }

 @Test
 fun disabledRouteReportsEventDriven() {
 val s = Schedule("d1", "x", ScheduleKind.CONDITIONAL, enabled = false)
 assertTrue(ExecutionRouter.route(s).reason.contains("disabled"))
 }
}
