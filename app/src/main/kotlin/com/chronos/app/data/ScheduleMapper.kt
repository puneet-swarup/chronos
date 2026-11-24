package com.chronos.app.data

import com.chronos.core.scheduling.Schedule
import com.chronos.core.scheduling.ScheduleKind
import java.time.LocalDateTime

// Maps between the Room row and the domain model. The domain model is the one
// that knows how to compute occurrences, so persistence never reasons about time.
object ScheduleMapper {

    fun toDomain(entity: ScheduleEntity): Schedule {
        val kind = try {
            ScheduleKind.valueOf(entity.kind)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException(
                "Unknown schedule kind '" + entity.kind + "' for schedule " + entity.id,
                e,
            )
        }
        return Schedule(
            id = entity.id,
            name = entity.name,
            kind = kind,
            recurrence = entity.recurrenceJson?.let { RecurrenceCodec.decode(it) },
            fixedDateTime = entity.fixedDateTime?.let { LocalDateTime.parse(it) },
            enabled = entity.enabled,
            nextRun = entity.nextRun?.let { LocalDateTime.parse(it) },
        )
    }

    fun toEntity(schedule: Schedule): ScheduleEntity = ScheduleEntity(
        id = schedule.id,
        name = schedule.name,
        kind = schedule.kind.name,
        recurrenceJson = schedule.recurrence?.let { RecurrenceCodec.encode(it) },
        fixedDateTime = schedule.fixedDateTime?.toString(),
        enabled = schedule.enabled,
        nextRun = schedule.nextRun?.toString(),
    )
}
