package com.chronos.core

import com.chronos.core.scheduling.ReconciliationPlanner
import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import com.chronos.core.scheduling.Schedule
import com.chronos.core.scheduling.ScheduleKind
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// The planner is the whole reliability decision, so it is tested as pure logic:
// fixed clock, fixed timezone, no Android.
class ReconciliationPlannerTest {

    private val planner = ReconciliationPlanner()
    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    private val now: LocalDateTime = LocalDateTime.of(2026, 11, 24, 7, 0)

    private fun daily(id: String, enabled: Boolean = true, hour: Int = 8): Schedule = Schedule(
        id = id,
        name = "daily " + id,
        kind = ScheduleKind.RECURRENCE,
        recurrence = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(hour, 0)),
        enabled = enabled,
    )

    private fun interval(id: String, minutes: Long): Schedule = Schedule(
        id = id,
        name = "every " + minutes + " minutes",
        kind = ScheduleKind.INTERVAL,
        recurrence = Recurrence(type = RecurrenceType.INTERVAL, intervalMinutes = minutes),
    )

    @Test
    fun `empty input produces an empty plan`() {
        val plan = planner.plan(emptyList(), now, canScheduleExact = true, zone = zone)
        assertTrue(plan.isEmpty)
    }

    @Test
    fun `daily schedule arms its next occurrence`() {
        val plan = planner.plan(listOf(daily("a")), now, canScheduleExact = true, zone = zone)

        assertEquals(1, plan.arm.size)
        val armed = plan.arm.single()
        assertEquals("a", armed.scheduleId)
        assertEquals(LocalDateTime.of(2026, 11, 24, 8, 0), armed.nextRun)
        assertEquals(
            LocalDateTime.of(2026, 11, 24, 8, 0).atZone(zone).toInstant().toEpochMilli(),
            armed.triggerAtMillis,
        )
        assertTrue(armed.exact)
        assertNull(armed.note)
        assertTrue(plan.degraded.isEmpty())
    }

    @Test
    fun `a schedule already past today rolls to tomorrow`() {
        val plan = planner.plan(listOf(daily("a", hour = 6)), now, canScheduleExact = true, zone = zone)
        assertEquals(LocalDateTime.of(2026, 11, 25, 6, 0), plan.arm.single().nextRun)
    }

    @Test
    fun `denied exact access degrades to an inexact alarm and says so`() {
        val plan = planner.plan(listOf(daily("a")), now, canScheduleExact = false, zone = zone)

        val armed = plan.arm.single()
        assertFalse(armed.exact)
        assertNotNull(armed.note)
        assertEquals(1, plan.degraded.size)
        assertEquals("a", plan.degraded.single().scheduleId)
    }

    @Test
    fun `disabled schedule is cancelled and skipped`() {
        val plan = planner.plan(listOf(daily("a", enabled = false)), now, canScheduleExact = true, zone = zone)

        assertTrue(plan.arm.isEmpty())
        assertEquals(listOf("a"), plan.cancel)
        assertEquals("disabled", plan.skip.single().reason)
    }

    @Test
    fun `fixed schedule in the past is cancelled rather than armed`() {
        val past = Schedule(
            id = "once",
            name = "missed",
            kind = ScheduleKind.FIXED,
            fixedDateTime = LocalDateTime.of(2026, 11, 24, 6, 0),
        )
        val plan = planner.plan(listOf(past), now, canScheduleExact = true, zone = zone)

        assertTrue(plan.arm.isEmpty())
        assertEquals(listOf("once"), plan.cancel)
        assertEquals("no future occurrence", plan.skip.single().reason)
    }

    @Test
    fun `fixed schedule in the future is armed once`() {
        val future = Schedule(
            id = "once",
            name = "later",
            kind = ScheduleKind.FIXED,
            fixedDateTime = LocalDateTime.of(2026, 12, 15, 8, 0),
        )
        val plan = planner.plan(listOf(future), now, canScheduleExact = true, zone = zone)

        assertEquals(LocalDateTime.of(2026, 12, 15, 8, 0), plan.arm.single().nextRun)
        assertTrue(plan.skip.isEmpty())
    }

    @Test
    fun `conditional schedule is skipped because there is no clock time to arm`() {
        val conditional = Schedule(
            id = "battery",
            name = "when battery is low",
            kind = ScheduleKind.CONDITIONAL,
        )
        val plan = planner.plan(listOf(conditional), now, canScheduleExact = true, zone = zone)

        assertTrue(plan.arm.isEmpty())
        assertEquals(listOf("battery"), plan.cancel)
        assertEquals("conditional schedules react to device events", plan.skip.single().reason)
    }

    @Test
    fun `short interval is armed as an exact alarm`() {
        val plan = planner.plan(listOf(interval("ten", 10)), now, canScheduleExact = true, zone = zone)

        val armed = plan.arm.single()
        assertTrue(armed.exact)
        assertNull(armed.note)
        assertEquals(LocalDateTime.of(2026, 11, 24, 7, 10), armed.nextRun)
    }

    @Test
    fun `long interval falls back to an inexact alarm and records why`() {
        val plan = planner.plan(listOf(interval("hourly", 60)), now, canScheduleExact = true, zone = zone)

        val armed = plan.arm.single()
        assertFalse(armed.exact)
        assertTrue(armed.note!!.contains("flexible work"))
        assertEquals(LocalDateTime.of(2026, 11, 24, 8, 0), armed.nextRun)
    }

    @Test
    fun `every armed schedule is cancelled first so a stale alarm cannot fire`() {
        val plan = planner.plan(listOf(daily("a"), daily("b")), now, canScheduleExact = true, zone = zone)

        assertEquals(listOf("a", "b"), plan.arm.map { it.scheduleId })
        assertEquals(setOf("a", "b"), plan.cancel.toSet())
    }

    @Test
    fun `weekly schedule skips to its next active weekday`() {
        val weekly = Schedule(
            id = "weekly",
            name = "mondays",
            kind = ScheduleKind.RECURRENCE,
            recurrence = Recurrence(
                type = RecurrenceType.WEEKLY,
                time = LocalTime.of(9, 0),
                weekdays = setOf(DayOfWeek.MONDAY),
            ),
        )
        // 2026-11-24 is a Tuesday, so the next Monday is 2026-11-30.
        val plan = planner.plan(listOf(weekly), now, canScheduleExact = true, zone = zone)

        val next = plan.arm.single().nextRun
        assertEquals(LocalDateTime.of(2026, 11, 30, 9, 0), next)
        assertEquals(DayOfWeek.MONDAY, next.dayOfWeek)
    }

    @Test
    fun `exclusion dates are respected when choosing the next occurrence`() {
        val excluded = Schedule(
            id = "excluded",
            name = "not on the 25th",
            kind = ScheduleKind.RECURRENCE,
            recurrence = Recurrence(
                type = RecurrenceType.DAILY,
                time = LocalTime.of(8, 0),
                exclusions = setOf(LocalDate.of(2026, 11, 24)),
            ),
        )
        val plan = planner.plan(listOf(excluded), now, canScheduleExact = true, zone = zone)

        assertEquals(LocalDateTime.of(2026, 11, 25, 8, 0), plan.arm.single().nextRun)
    }

    @Test
    fun `unreadable or unschedulable rows do not hide the good ones`() {
        val conditional = Schedule(id = "cond", name = "event", kind = ScheduleKind.CONDITIONAL)
        val plan = planner.plan(listOf(conditional, daily("a")), now, canScheduleExact = true, zone = zone)

        assertEquals(1, plan.arm.size)
        assertEquals("a", plan.arm.single().scheduleId)
        assertEquals(1, plan.skip.size)
    }
}
