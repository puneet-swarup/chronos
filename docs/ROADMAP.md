# Roadmap

The ordered backlog is defined in [`FORGE_PLAN.md`](https://github.com/puneet-swarup/chronos/blob/main/FORGE_PLAN.md)
(M0–M8). This page tracks where the code actually is, as opposed to where the plan
intended it to be.

## Status at a glance

| Milestone | Scope | Status |
|-----------|-------|--------|
| M0 | Toolchain, Gradle skeleton, two modules | Done |
| M1 | Universal scheduler: recurrence engine, schedule model, execution routing | Done, except DST/timezone wrapper |
| M2 | Workflow model, deterministic validator, intent compiler | Done, except execution state machine |
| M3 | Action contract, registry, four built-in actions | Done |
| M4 | Android app shell: Compose, navigation, Room, DI | Mostly done — Home screen is wired, five sections are static |
| M5 | Android scheduling adapters: AlarmManager, WorkManager, reboot and clock-change recovery | Done — both mechanisms arm, fire and re-arm; device verification outstanding |
| M6 | Integrations: app discovery, device controls, event triggers, capability manager | Not started |
| M7 | Local AI: LiteRT-LM provider, model manager, tiered model selection | Seam only — no model |
| M8 | Advanced mode (Shizuku/Termux/Tasker), import/export, security audit, release | Partial — signed test builds and self-updates work; advanced mode and the audit are not started |

## M1 — Universal scheduler

Implemented: `Recurrence`, `RecurrenceEngine`, `Schedule`, `ScheduleKind`,
`ExecutionRouter`. Daily, weekly, monthly and custom-interval rules, month-end and
first-working-day handling, day-31 clamping, exclusion dates, and inclusive
start/end bounds.

Pending: a `ZonedDateTime` wrapper so occurrences resolve against the device
timezone and DST transitions rather than local wall-clock arithmetic.

## M2 — Workflow model and validation

Implemented: versioned `Workflow` with a sealed `WorkflowNode` hierarchy (Action,
Condition, Delay, Approval, Stop), `WorkflowValidator` producing structured errors,
and `IntentCompiler` turning a parsed intent into a validated draft.

Pending: cycle detection for `Condition` branches, entry-node reachability checks,
and the runtime execution state machine with checkpoint/idempotency metadata.

## M3 — Action registry

Implemented: `ChronosAction` contract, `ActionRegistry` with capability
introspection, and the notification, open-URL, launch-app and delay actions.

## M4 — Android app shell

Implemented: `ChronosApplication`, `MainActivity`, Compose navigation across six
sections, design tokens and a single `ChronosTheme` wrapper, `HomeUiState` +
`HomeViewModel` backed by the repository, and the Room layer (`Entities`, `Daos`,
`ChronosDatabase`, `ScheduleRepository`). Hilt is replaced by a manual
`ServiceLocator` because its Gradle plugin does not support AGP 9.2.0.

Implemented: a first-run guide (`onboarding/`) that walks a new user through what the
app does and through the four permissions Android requires from them, with the
consequence of refusing each one stated up front.

Pending: wire the Automations and AI screens to real data. Schedule, Activity and
Settings are live; the app icon and the guide replay entry point are done.

## M5 — Android scheduling adapters

Done:

1. `ReconciliationPlanner` — pure decision layer: cancel, arm or skip every stored
   schedule, with the exactness the device actually allows.
2. `AlarmScheduler` — `setExactAndAllowWhileIdle` when exact access is granted,
   `setAndAllowWhileIdle` otherwise, with `SecurityException` handled if access is
   revoked between the check and the call.
3. `ScheduleReconciler` — re-plans the whole set on boot, package replace, clock or
   timezone change, app start, and after every firing. Idempotent by design.
4. `AlarmReceiver` + `ScheduleExecutor` — one `executions` row per attempt including
   failures with an error code, a notification, then a re-arm.
5. `RecurrenceCodec` + `ScheduleMapper` — the persistence bridge that was missing;
   `recurrenceJson` was previously a column nothing wrote or read.
6. `TimeChangeReceiver` — clock and timezone changes re-plan every schedule.
7. Launcher and notification icons, so the app no longer ships the default robot.

8. `WorkScheduler` + `ScheduleWorker` — WorkManager owns flexible and interval work,
   with unique work per schedule so re-planning replaces rather than duplicates.
   Intervals below WorkManager's 15 minute floor are reported as degraded instead of
   being silently rounded.
9. `Activity` screen — executions, failures and the last scheduler pass, read from
   Room and the audit log.

Still missing:

- Constraints and retry policy on the WorkManager requests (currently none).
- Exact-alarm-revoked and permission-change receivers surfaced in the UI.
- Duplicate-execution prevention beyond unique work and one PendingIntent per id.

Acceptance: a schedule survives reboot, a timezone change and a force-stop on a
real device, and a failed arm is visible in the Activity screen.

## M6 — Integrations

Installed-app discovery via package visibility queries, device controls (volume,
DND, flashlight, brightness) behind capability gates, event triggers (battery,
network, Bluetooth, location, calendar), `CapabilityManager` for a live inventory,
and `AutomationSupervisor` for broken schedules and permission drift.

## M7 — Local AI

`LocalModelProvider` is implemented as a seam with a simulated provider, and the
rules-based parser covers common phrasing without any model. Remaining: a LiteRT-LM
provider, a Wi-Fi-only model manager with checksum and resume, tiered model
selection by device profile, and language detection for English / Hindi / Hinglish.

## M8 — Advanced mode and release

Done:

- **Self-updates.** Every push to `main` builds a signed APK, publishes it as a
  GitHub Release, and the app offers it from Settings. It checks on start (throttled
  to every six hours), verifies the published SHA-256, and hands the APK to the
  system installer. Android still asks for one confirmation; a normal app cannot
  install silently.
- **Signing** with a committed test key, documented in `keystore/README.md`.

Still open:

- Replace the public test key before any real distribution.
- Optional Shizuku / Termux / Tasker bridges.
- Workflow import and export.
- A security audit with Android Keystore credential storage, and the audit log UI.
- A device compatibility matrix across flagship, mid-range and aggressive-OEM
  phones.

## Working agreement

Every milestone's definition of done: unit tests written alongside the code,
`./gradlew test` green, CI green, and the relevant acceptance rows satisfied.
