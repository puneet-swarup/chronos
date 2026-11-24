package com.chronos.app.data

import com.chronos.core.scheduling.Recurrence
import com.chronos.core.scheduling.RecurrenceType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// The on-disk shape of a Recurrence. Deliberately a separate DTO rather than
// annotations on the domain class: the core module stays free of serialization
// concerns, and this format can evolve without touching scheduling logic.
//
// Everything is a string or primitive on purpose - java.time types do not
// serialize themselves, and ISO-8601 text is readable in a database browser.
@Serializable
internal data class RecurrenceDto(
    val type: String,
    val time: String? = null,
    val weekdays: List<String> = emptyList(),
    val dayOfMonth: Int? = null,
    val lastDayOfMonth: Boolean = false,
    val firstWorkingDay: Boolean = false,
    val intervalMinutes: Long? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val exclusions: List<String> = emptyList(),
)

object RecurrenceCodec {

    // ignoreUnknownKeys keeps rows written by a newer build readable by an older
    // one during a staged upgrade; encodeDefaults keeps the stored text explicit
    // rather than depending on Kotlin defaults.
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun encode(recurrence: Recurrence): String {
        val dto = RecurrenceDto(
            type = recurrence.type.name,
            time = recurrence.time?.toString(),
            weekdays = recurrence.weekdays.sortedBy { it.value }.map { it.name },
            dayOfMonth = recurrence.dayOfMonth,
            lastDayOfMonth = recurrence.lastDayOfMonth,
            firstWorkingDay = recurrence.firstWorkingDay,
            intervalMinutes = recurrence.intervalMinutes,
            startDate = recurrence.startDate?.toString(),
            endDate = recurrence.endDate?.toString(),
            exclusions = recurrence.exclusions.sorted().map { it.toString() },
        )
        return json.encodeToString(RecurrenceDto.serializer(), dto)
    }

    // Throws IllegalArgumentException on anything unreadable. Callers decide
    // whether to skip the row or fail loudly; nothing is guessed.
    fun decode(value: String): Recurrence {
        val dto = try {
            json.decodeFromString(RecurrenceDto.serializer(), value)
        } catch (e: Exception) {
            throw IllegalArgumentException("Malformed recurrence JSON: " + e.message, e)
        }
        return try {
            Recurrence(
                type = RecurrenceType.valueOf(dto.type),
                time = dto.time?.let { LocalTime.parse(it) },
                weekdays = dto.weekdays.map { DayOfWeek.valueOf(it) }.toSet(),
                dayOfMonth = dto.dayOfMonth,
                lastDayOfMonth = dto.lastDayOfMonth,
                firstWorkingDay = dto.firstWorkingDay,
                intervalMinutes = dto.intervalMinutes,
                startDate = dto.startDate?.let { LocalDate.parse(it) },
                endDate = dto.endDate?.let { LocalDate.parse(it) },
                exclusions = dto.exclusions.map { LocalDate.parse(it) }.toSet(),
            )
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid recurrence data: " + e.message, e)
        }
    }
}
