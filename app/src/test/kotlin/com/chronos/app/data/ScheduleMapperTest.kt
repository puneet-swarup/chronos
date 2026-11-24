package com.chronos.app.data

import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import com.chronos.core.scheduling.Schedule
import com.chronos.core.scheduling.ScheduleKind
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleMapperTest {

    @Test
    fun `fixed schedule maps from the row`() {
        val entity = ScheduleEntity(
            id = "s1",
            name = "Standup",
            kind = ScheduleKind.FIXED.name,
            recurrenceJson = null,
            fixedDateTime = "2026-12-15T08:00",
            enabled = true,
            nextRun = null,
        )
        val domain = ScheduleMapper.toDomain(entity)

        assertEquals("s1", domain.id)
        assertEquals(ScheduleKind.FIXED, domain.kind)
        assertEquals(LocalDateTime.of(2026, 12, 15, 8, 0), domain.fixedDateTime)
    }

    @Test
    fun `recurring schedule decodes its recurrence JSON`() {
        val entity = ScheduleEntity(
            id = "s2",
            name = "Daily",
            kind = ScheduleKind.RECURRENCE.name,
            recurrenceJson = RecurrenceCodec.encode(
                Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(8, 0)),
            ),
            fixedDateTime = null,
            enabled = true,
            nextRun = "2026-11-25T08:00",
        )
        val domain = ScheduleMapper.toDomain(entity)

        assertEquals(ScheduleKind.RECURRENCE, domain.kind)
        assertEquals(RecurrenceType.DAILY, domain.recurrence!!.type)
        assertEquals(LocalDateTime.of(2026, 11, 25, 8, 0), domain.nextRun)
    }

    @Test
    fun `domain round-trips through the row`() {
        val original = Schedule(
            id = "s3",
            name = "Weekdays",
            kind = ScheduleKind.RECURRENCE,
            recurrence = Recurrence(type = RecurrenceType.DAILY, time = LocalTime.of(7, 45)),
            enabled = false,
            nextRun = LocalDateTime.of(2026, 11, 26, 7, 45),
        )
        val roundTripped = ScheduleMapper.toDomain(ScheduleMapper.toEntity(original))

        assertEquals(original.id, roundTripped.id)
        assertEquals(original.name, roundTripped.name)
        assertEquals(original.kind, roundTripped.kind)
        assertEquals(original.recurrence, roundTripped.recurrence)
        assertEquals(original.enabled, roundTripped.enabled)
        assertEquals(original.nextRun, roundTripped.nextRun)
    }

    @Test
    fun `an unknown kind fails loudly instead of defaulting`() {
        val entity = ScheduleEntity(
            id = "bad",
            name = "Broken",
            kind = "WHENEVER",
            recurrenceJson = null,
            fixedDateTime = null,
            enabled = true,
            nextRun = null,
        )
        val error = runCatching { ScheduleMapper.toDomain(entity) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(error!!.message!!.contains("WHENEVER"))
    }

    @Test
    fun `unreadable recurrence JSON is reported, not silently dropped`() {
        val entity = ScheduleEntity(
            id = "bad",
            name = "Broken",
            kind = ScheduleKind.RECURRENCE.name,
            recurrenceJson = "{oops",
            fixedDateTime = null,
            enabled = true,
            nextRun = null,
        )
        val error = runCatching { ScheduleMapper.toDomain(entity) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
    }
}
