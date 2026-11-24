package com.chronos.app.scheduling

import com.chronos.app.data.ExecutionEntity
import com.chronos.app.data.ScheduleRepository
import com.chronos.app.notifications.ExecutionNotifier
import java.time.Clock

// Runs one schedule after its alarm fires: records the attempt, tells the user,
// then re-arms the next occurrence.
//
// Every outcome is written to the executions table, including the failures. An
// automation that fails silently is worse than one that does not run.
class ScheduleExecutor(
    private val repository: ScheduleRepository,
    private val notifier: ExecutionNotifier,
    private val reconciler: ScheduleReconciler,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    suspend fun execute(scheduleId: String) {
        val firedAt = clock.millis()
        val entity = repository.get(scheduleId)

        if (entity == null) {
            repository.recordExecution(
                ExecutionEntity(
                    scheduleId = scheduleId,
                    state = STATE_SKIPPED,
                    scheduledAt = firedAt,
                    startedAt = firedAt,
                    finishedAt = firedAt,
                    errorCode = "SCHEDULE_MISSING",
                    errorMessage = "No schedule with id " + scheduleId,
                ),
            )
            return
        }

        try {
            notifier.notifyScheduleFired(entity)
            repository.recordExecution(
                ExecutionEntity(
                    scheduleId = scheduleId,
                    state = STATE_FIRED,
                    scheduledAt = firedAt,
                    startedAt = firedAt,
                    finishedAt = clock.millis(),
                ),
            )
        } catch (t: Throwable) {
            // The usual cause is POST_NOTIFICATIONS being denied on Android 13+.
            repository.recordExecution(
                ExecutionEntity(
                    scheduleId = scheduleId,
                    state = STATE_FAILED,
                    scheduledAt = firedAt,
                    startedAt = firedAt,
                    finishedAt = clock.millis(),
                    errorCode = errorCodeFor(t),
                    errorMessage = t.message ?: t::class.java.simpleName,
                ),
            )
        } finally {
            // Re-arm regardless: the next occurrence must not depend on whether
            // this one could be announced.
            reconciler.reconcile()
        }
    }

    private fun errorCodeFor(t: Throwable): String = when (t) {
        is SecurityException -> "NOTIFICATION_PERMISSION"
        else -> "EXECUTION_FAILED"
    }

    companion object {
        const val STATE_FIRED = "FIRED"
        const val STATE_FAILED = "FAILED"
        const val STATE_SKIPPED = "SKIPPED"
    }
}
