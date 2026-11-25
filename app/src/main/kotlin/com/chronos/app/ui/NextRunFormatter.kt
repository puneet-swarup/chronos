package com.chronos.app.ui

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Turns the stored next-run timestamp into something a person reads at a glance:
// "Today 07:30", "Tomorrow 07:30", "Fri 27 Nov 07:30". Pure, so it is tested.
object NextRunFormatter {

    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("EEE d MMM")

    fun format(next: LocalDateTime?, now: LocalDateTime): String? {
        if (next == null) return null
        val time = next.format(timeFormat)
        return when {
            next.toLocalDate() == now.toLocalDate() -> "Today " + time
            next.toLocalDate() == now.toLocalDate().plusDays(1) -> "Tomorrow " + time
            next.toLocalDate().isBefore(now.toLocalDate()) -> "Overdue (" + next.format(dateFormat) + " " + time + ")"
            else -> next.format(dateFormat) + " " + time
        }
    }

    // Picks the soonest future run out of everything stored. A past timestamp is
    // ignored rather than shown as the next alarm.
    fun soonest(runs: List<LocalDateTime?>, now: LocalDateTime): LocalDateTime? =
        runs.filterNotNull()
            .filter { it.toLocalDate() >= LocalDate.from(now.toLocalDate()) && it.isAfter(now.minusMinutes(1)) }
            .minOrNull()
}
