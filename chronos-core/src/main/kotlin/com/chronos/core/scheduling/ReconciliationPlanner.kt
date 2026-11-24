package com.chronos.core.scheduling

import java.time.LocalDateTime
import java.time.ZoneId

// A single alarm the platform layer should arm. `exact` is what the device
// actually allows, which is not always what the schedule wants; when they
// differ, `note` records why so the UI can be honest about it.
data class ArmRequest(
    val scheduleId: String,
    val triggerAtMillis: Long,
    val nextRun: LocalDateTime,
    val exact: Boolean,
    val note: String? = null,
)

// A schedule that was deliberately not armed, and why.
data class SkipRequest(
    val scheduleId: String,
    val reason: String,
)

// The complete set of platform operations for one reconciliation pass. Pure data,
// so the whole decision is unit-testable without Android.
data class ReconciliationPlan(
    val cancel: List<String> = emptyList(),
    val arm: List<ArmRequest> = emptyList(),
    val skip: List<SkipRequest> = emptyList(),
) {
    val isEmpty: Boolean get() = cancel.isEmpty() && arm.isEmpty() && skip.isEmpty()

    // Armed, but not in the way the schedule asked for.
    val degraded: List<ArmRequest> get() = arm.filter { it.note != null }
}

// Turns persisted schedules into alarm operations. This is the whole of the
// "reliability" decision: which schedules to arm, at which instant, and with
// which precision. Everything below it is a thin platform adapter.
//
// Every schedule that is armed is also listed for cancellation first. Cancelling
// a PendingIntent is idempotent, and doing it before arming means a stale alarm
// left over from a previous pass cannot fire between planning and arming.
class ReconciliationPlanner(private val engine: RecurrenceEngine = RecurrenceEngine()) {

    fun plan(
        schedules: List<Schedule>,
        now: LocalDateTime,
        canScheduleExact: Boolean = true,
        zone: ZoneId = ZoneId.systemDefault(),
    ): ReconciliationPlan {
        val cancel = ArrayList<String>(schedules.size)
        val arm = ArrayList<ArmRequest>()
        val skip = ArrayList<SkipRequest>()

        for (schedule in schedules) {
            if (!schedule.enabled) {
                cancel.add(schedule.id)
                skip.add(SkipRequest(schedule.id, "disabled"))
                continue
            }

            // Route first: an event-driven schedule has no clock time at all, so
            // asking for its next occurrence would report the misleading
            // "no future occurrence" instead of "this reacts to a device event".
            val decision = ExecutionRouter.route(schedule, requiresExact = true)
            if (decision.mechanism == ExecutionMechanism.EVENT_DRIVEN) {
                cancel.add(schedule.id)
                skip.add(SkipRequest(schedule.id, decision.reason))
                continue
            }

            val next = schedule.computeNext(now, engine)
            if (next == null) {
                cancel.add(schedule.id)
                skip.add(SkipRequest(schedule.id, "no future occurrence"))
                continue
            }

            when (decision.mechanism) {
                ExecutionMechanism.EVENT_DRIVEN -> Unit // handled above

                ExecutionMechanism.EXACT_ALARM -> {
                    cancel.add(schedule.id)
                    arm.add(
                        ArmRequest(
                            scheduleId = schedule.id,
                            triggerAtMillis = next.atZone(zone).toInstant().toEpochMilli(),
                            nextRun = next,
                            exact = canScheduleExact,
                            note = if (canScheduleExact) {
                                null
                            } else {
                                "exact alarm access not granted; armed as an inexact alarm"
                            },
                        ),
                    )
                }

                ExecutionMechanism.FLEXIBLE_WORK -> {
                    cancel.add(schedule.id)
                    arm.add(
                        ArmRequest(
                            scheduleId = schedule.id,
                            triggerAtMillis = next.atZone(zone).toInstant().toEpochMilli(),
                            nextRun = next,
                            exact = false,
                            note = "flexible work is not implemented yet; armed as an inexact alarm",
                        ),
                    )
                }
            }
        }

        return ReconciliationPlan(cancel = cancel, arm = arm, skip = skip)
    }
}
