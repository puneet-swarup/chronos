package com.chronos.core.scheduling

/**
 * The five scheduling patterns defined in the Chronos PRD (Module 1).
 *
 * [FIXED] - one-off date/time ("15 Dec 2026 at 8 AM").
 * [RECURRENCE] - calendar recurrence ("every Monday at 7 AM").
 * [INTERVAL] - elapsed-time interval ("every 45 minutes").
 * [RELATIVE] - relative to another event ("30 min before a meeting").
 * [CONDITIONAL] - driven by a device condition ("when battery < 20%").
 */
enum class ScheduleKind {
 FIXED,
 RECURRENCE,
 INTERVAL,
 RELATIVE,
 CONDITIONAL;
}
