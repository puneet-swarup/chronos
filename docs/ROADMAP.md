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
| M5 | Android scheduling adapters: AlarmManager, WorkManager, reboot and clock-change recovery | Not started (stubs only) |
| M6 | Integrations: app discovery, device controls, event triggers, capability manager | Not started |
| M7 | Local AI: LiteRT-LM provider, model manager, tiered model selection | Seam only — no model |
| M8 | Advanced mode (Shizuku/Termux/Tasker), import/export, security audit, release | Not started |

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

Pending: wire the Schedule, Automations, AI, Activity and Settings screens to real
data; add an app icon and adaptive icon resources; let Settings reopen the guide
(`OnboardingViewModel.reopen()` exists but has no UI entry point yet).

## M5 — Android scheduling adapters (next up)

This is the milestone that makes Chronos an automation app rather than a model of
one. Needed, in order:

1. `AlarmScheduler` — `AlarmManager.setExactAndAllowWhileIdle`, with a runtime check
   for exact-alarm access and a fallback to inexact alarms when it is denied.
2. `WorkScheduler` — `WorkManager` for flexible and interval work, with constraints
   and retry policy.
3. `ScheduleReconciler` — read schedules from Room, cancel stale alarms, re-arm
   current ones. Called from boot, package-replace, timezone change and app start.
4. `AlarmReceiver` / `WorkReceiver` — dispatch into the workflow executor, record an
   `Execution` row, and de-duplicate so a trigger cannot run twice.
5. Permission-change and exact-alarm-revoked receivers, surfaced as honest warnings
   rather than silent failures.

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

Optional Shizuku / Termux / Tasker bridges, workflow import and export, a security
audit with Android Keystore credential storage, the audit log, and a device
compatibility matrix across flagship, mid-range and aggressive-OEM phones.

## Working agreement

Every milestone's definition of done: unit tests written alongside the code,
`./gradlew test` green, CI green, and the relevant acceptance rows satisfied.
