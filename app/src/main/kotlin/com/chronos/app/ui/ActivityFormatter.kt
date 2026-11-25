package com.chronos.app.ui

import com.chronos.app.data.AuditEntity
import com.chronos.app.data.ExecutionEntity
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// Pure formatting for the Activity screen, so the labels are tested rather than
// eyeballed on a device.
object ActivityFormatter {

    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormat = DateTimeFormatter.ofPattern("d MMM")

    fun formatTimestamp(epochMillis: Long, now: LocalDateTime, zone: ZoneId = ZoneId.systemDefault()): String {
        val at = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), zone)
        val time = at.format(timeFormat)
        val date = at.toLocalDate()
        val today: LocalDate = now.toLocalDate()
        return when {
            date == today -> "Today " + time
            date == today.minusDays(1) -> "Yesterday " + time
            date == today.plusDays(1) -> "Tomorrow " + time
            else -> at.format(dateFormat) + " " + time
        }
    }

    // Executions store a machine state; the screen shows a phrase.
    fun labelFor(state: String): String = when (state.uppercase()) {
        "FIRED" -> "Fired"
        "FAILED" -> "Failed"
        "SKIPPED" -> "Skipped"
        "STARTED" -> "Started"
        else -> state.lowercase().replaceFirstChar { it.uppercase() }
    }

    fun isFailure(state: String): Boolean = state.equals("FAILED", ignoreCase = true) ||
        state.equals("SKIPPED", ignoreCase = true)

    // One line explaining what went wrong, or null when nothing did.
    fun detailFor(execution: ExecutionEntity): String? {
        val code = execution.errorCode
        val message = execution.errorMessage
        return when {
            code == null && message == null -> null
            code == null -> message
            message == null -> code
            else -> code + " - " + message
        }
    }
}

// One row as the screen renders it.
data class ActivityRow(
    val title: String,
    val whenLabel: String,
    val stateLabel: String,
    val failed: Boolean,
    val detail: String?,
)

data class ActivityUiState(
    val rows: List<ActivityRow> = emptyList(),
    val failedCount: Int = 0,
    // Last reconciliation, straight from the audit log. This is what makes a
    // scheduler that could not arm something visible instead of silent.
    val schedulerSummary: String? = null,
) {
    val isEmpty: Boolean get() = rows.isEmpty()

    companion object {
        fun from(
            executions: List<ExecutionEntity>,
            audit: List<AuditEntity>,
            scheduleNames: Map<String, String> = emptyMap(),
            now: LocalDateTime? = null,
            zone: ZoneId = ZoneId.systemDefault(),
        ): ActivityUiState {
            val rows = executions.map { execution ->
                ActivityRow(
                    title = execution.scheduleId?.let { id -> scheduleNames[id] ?: id } ?: "Workflow run",
                    whenLabel = if (now == null) {
                        ""
                    } else {
                        ActivityFormatter.formatTimestamp(execution.scheduledAt, now, zone)
                    },
                    stateLabel = ActivityFormatter.labelFor(execution.state),
                    failed = ActivityFormatter.isFailure(execution.state),
                    detail = ActivityFormatter.detailFor(execution),
                )
            }
            val summary = audit
                .firstOrNull { it.category == "scheduler" }
                ?.message
            return ActivityUiState(
                rows = rows,
                failedCount = rows.count { it.failed },
                schedulerSummary = summary,
            )
        }
    }
}
