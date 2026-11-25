package com.chronos.app.ui

import com.chronos.app.data.AuditEntity
import com.chronos.app.data.ExecutionEntity
import java.time.LocalDateTime
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ActivityFormatterTest {

    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")
    private val now: LocalDateTime = LocalDateTime.of(2026, 11, 25, 9, 0)

    private fun millisOf(dateTime: LocalDateTime): Long =
        dateTime.atZone(zone).toInstant().toEpochMilli()

    @Test
    fun `today, yesterday and older timestamps read differently`() {
        assertEquals(
            "Today 07:30",
            ActivityFormatter.formatTimestamp(millisOf(LocalDateTime.of(2026, 11, 25, 7, 30)), now, zone),
        )
        assertEquals(
            "Yesterday 22:15",
            ActivityFormatter.formatTimestamp(millisOf(LocalDateTime.of(2026, 11, 24, 22, 15)), now, zone),
        )
        assertEquals(
            "20 Nov 06:00",
            ActivityFormatter.formatTimestamp(millisOf(LocalDateTime.of(2026, 11, 20, 6, 0)), now, zone),
        )
    }

    @Test
    fun `machine states become readable labels`() {
        assertEquals("Fired", ActivityFormatter.labelFor("FIRED"))
        assertEquals("Failed", ActivityFormatter.labelFor("FAILED"))
        assertEquals("Skipped", ActivityFormatter.labelFor("SKIPPED"))
        assertEquals("Something else", ActivityFormatter.labelFor("something else"))
    }

    @Test
    fun `failures and skips are flagged`() {
        assertTrue(ActivityFormatter.isFailure("FAILED"))
        assertTrue(ActivityFormatter.isFailure("skipped"))
        assertFalse(ActivityFormatter.isFailure("FIRED"))
    }

    @Test
    fun `detail combines the code and the message`() {
        val execution = ExecutionEntity(
            scheduleId = "s1",
            state = "FAILED",
            scheduledAt = 0L,
            errorCode = "NOTIFICATION_PERMISSION",
            errorMessage = "denied",
        )
        assertEquals("NOTIFICATION_PERMISSION - denied", ActivityFormatter.detailFor(execution))
    }

    @Test
    fun `a clean run has no detail line`() {
        val execution = ExecutionEntity(scheduleId = "s1", state = "FIRED", scheduledAt = 0L)
        assertNull(ActivityFormatter.detailFor(execution))
    }

    @Test
    fun `rows carry the schedule name and count the failures`() {
        val executions = listOf(
            ExecutionEntity(scheduleId = "s1", state = "FIRED", scheduledAt = millisOf(now.minusHours(2))),
            ExecutionEntity(
                scheduleId = "s2",
                state = "FAILED",
                scheduledAt = millisOf(now.minusHours(1)),
                errorCode = "SCHEDULE_MISSING",
            ),
        )
        val state = ActivityUiState.from(
            executions = executions,
            audit = emptyList(),
            scheduleNames = mapOf("s1" to "Morning run"),
            now = now,
            zone = zone,
        )

        assertEquals(2, state.rows.size)
        assertEquals("Morning run", state.rows[0].title)
        assertEquals("s2", state.rows[1].title)
        assertEquals(1, state.failedCount)
        assertEquals("Today 07:00", state.rows[0].whenLabel)
    }

    @Test
    fun `the last scheduler pass is surfaced from the audit log`() {
        val audit = listOf(
            AuditEntity(category = "scheduler", message = "reconciled: armed=2 cancelled=2"),
            AuditEntity(category = "schedule", message = "upsert s1"),
        )
        val state = ActivityUiState.from(emptyList(), audit, emptyMap(), now, zone)
        assertEquals("reconciled: armed=2 cancelled=2", state.schedulerSummary)
    }

    @Test
    fun `nothing recorded is an empty state, not an error`() {
        val state = ActivityUiState.from(emptyList(), emptyList(), emptyMap(), now, zone)
        assertTrue(state.isEmpty)
        assertEquals(0, state.failedCount)
        assertNull(state.schedulerSummary)
    }
}
