package com.chronos.app.scheduling

import com.chronos.app.data.ScheduleMapper
import com.chronos.app.data.ScheduleRepository
import com.chronos.core.scheduling.ReconciliationPlanner
import com.chronos.core.scheduling.Schedule
import java.time.Clock
import java.time.LocalDateTime

// What one reconciliation pass did. Surfaced to the audit log now, and to the
// Activity screen when that screen is wired up.
data class ReconciliationResult(
    val armed: Int = 0,
    val cancelled: Int = 0,
    val skipped: List<String> = emptyList(),
    val degraded: List<String> = emptyList(),
    val unreadable: List<String> = emptyList(),
) {
    fun summary(): String =
        "reconciled: armed=" + armed + " cancelled=" + cancelled +
            " skipped=" + skipped.size + " degraded=" + degraded.size +
            " unreadable=" + unreadable.size
}

// Re-arms every enabled schedule from the database. Safe to call as often as
// needed: it is idempotent, because each pass cancels and re-arms the same set.
//
// Runs after boot, after a package replace, on timezone or clock change, on app
// start, and after a schedule fires.
class ScheduleReconciler(
    private val repository: ScheduleRepository,
    private val scheduler: AlarmScheduler,
    private val workScheduler: WorkScheduler,
    private val planner: ReconciliationPlanner = ReconciliationPlanner(),
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    suspend fun reconcile(): ReconciliationResult {
        val entities = repository.allSchedules()
        val domain = ArrayList<Schedule>(entities.size)
        val unreadable = ArrayList<String>()

        for (entity in entities) {
            try {
                domain.add(ScheduleMapper.toDomain(entity))
            } catch (e: IllegalArgumentException) {
                // A row that cannot be read must not stop the other schedules
                // from being armed. It is reported instead of silently dropped.
                unreadable.add(entity.id + ": " + (e.message ?: "unreadable"))
            }
        }

        val now = LocalDateTime.now(clock)
        val plan = planner.plan(
            schedules = domain,
            now = now,
            canScheduleExact = scheduler.canScheduleExactAlarms(),
            zone = clock.zone,
        )

        for (id in plan.cancel) {
            // Both mechanisms are cancelled: the planner may have moved a schedule
            // from one to the other, and a leftover of either kind would double-fire.
            scheduler.cancel(id)
            workScheduler.cancel(id)
        }
        for (request in plan.arm) {
            when (request.mechanism) {
                com.chronos.core.scheduling.ExecutionMechanism.FLEXIBLE_WORK ->
                    workScheduler.schedule(request.scheduleId, request.intervalMinutes)

                else -> scheduler.arm(request)
            }
            repository.updateNextRun(request.scheduleId, request.nextRun.toString())
        }
        for (skipped in plan.skip) {
            repository.updateNextRun(skipped.scheduleId, null)
        }

        val result = ReconciliationResult(
            armed = plan.arm.size,
            cancelled = plan.cancel.size,
            skipped = plan.skip.map { it.scheduleId + ": " + it.reason },
            degraded = plan.degraded.map { it.scheduleId + ": " + (it.note ?: "degraded") },
            unreadable = unreadable,
        )
        repository.recordAudit("scheduler", result.summary())
        return result
    }
}
