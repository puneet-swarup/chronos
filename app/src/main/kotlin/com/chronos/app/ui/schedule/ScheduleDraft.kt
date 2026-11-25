package com.chronos.app.ui.schedule

import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import com.chronos.core.scheduling.Schedule
import com.chronos.core.scheduling.ScheduleKind
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

// What the user is filling in. Kept as plain data with no Android types, so the
// translation into a domain Schedule is unit-tested rather than trusted.
enum class RepeatOption(val label: String, val description: String) {
    ONCE("Once", "A single alarm, at the next time this clock reaches"),
    DAILY("Every day", "The same time, every day"),
    WEEKDAYS("Weekdays", "Monday to Friday"),
    WEEKLY("Weekly", "Once a week, on today's weekday"),
}

data class ScheduleDraft(
    val name: String = "",
    val hour: Int = 7,
    val minute: Int = 0,
    val repeat: RepeatOption = RepeatOption.DAILY,
) {
    val timeLabel: String get() = String.format("%02d:%02d", hour, minute)

    fun validate(): List<String> {
        val errors = mutableListOf<String>()
        if (name.isBlank()) errors.add("Give the schedule a name")
        if (hour !in 0..23) errors.add("Hour must be between 0 and 23")
        if (minute !in 0..59) errors.add("Minute must be between 0 and 59")
        return errors
    }

    fun withHourStep(delta: Int): ScheduleDraft = copy(hour = Math.floorMod(hour + delta, 24))

    fun withMinuteStep(delta: Int): ScheduleDraft = copy(minute = Math.floorMod(minute + delta, 60))

    // Turns the draft into something the scheduler can arm. `now` decides which
    // day a one-off lands on and which weekday a weekly repeat belongs to, so it is
    // a parameter rather than a call to the clock inside.
    fun toSchedule(id: String, now: LocalDateTime): Schedule {
        val time = LocalTime.of(hour, minute)
        val title = name.trim()
        return when (repeat) {
            RepeatOption.ONCE -> {
                var at = LocalDateTime.of(now.toLocalDate(), time)
                if (!at.isAfter(now)) at = at.plusDays(1)
                Schedule(
                    id = id,
                    name = title,
                    kind = ScheduleKind.FIXED,
                    fixedDateTime = at,
                )
            }

            RepeatOption.DAILY -> Schedule(
                id = id,
                name = title,
                kind = ScheduleKind.RECURRENCE,
                recurrence = Recurrence(type = RecurrenceType.DAILY, time = time),
            )

            RepeatOption.WEEKDAYS -> Schedule(
                id = id,
                name = title,
                kind = ScheduleKind.RECURRENCE,
                recurrence = Recurrence(
                    type = RecurrenceType.WEEKLY,
                    time = time,
                    weekdays = setOf(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                    ),
                ),
            )

            RepeatOption.WEEKLY -> Schedule(
                id = id,
                name = title,
                kind = ScheduleKind.RECURRENCE,
                recurrence = Recurrence(
                    type = RecurrenceType.WEEKLY,
                    time = time,
                    weekdays = setOf(now.dayOfWeek),
                ),
            )
        }
    }
}

// One row in the schedule list.
data class ScheduleRow(
    val id: String,
    val name: String,
    val whenLabel: String,
    val repeatLabel: String,
    val enabled: Boolean,
)

data class ScheduleUiState(
    val rows: List<ScheduleRow> = emptyList(),
    // Non-null while the editor is open.
    val draft: ScheduleDraft? = null,
    val errors: List<String> = emptyList(),
    val saving: Boolean = false,
) {
    val isEditing: Boolean get() = draft != null
    val isEmpty: Boolean get() = rows.isEmpty()
}
