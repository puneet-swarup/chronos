# Chronos Architecture

## Overview

Chronos is a modular monolith split into two Gradle modules:

| Module | Type | Responsibility |
|--------|------|----------------|
| chronos-core | pure Kotlin/JVM (no Android) | Domain logic: recurrence, schedules, workflow model, action registry, validator, rules-based NL parser |
| app | Android (Compose) | UI, platform adapters (AlarmManager/WorkManager), receivers, dependency wiring |

All framework-independent logic lives in chronos-core so it is testable on any JVM
and reusable by the Android layer. The app module depends on chronos-core.

## Layering

```
app/            Compose UI, Navigation, Room, receivers, platform adapters
                        |
                        | depends on
                        v
chronos-core/   domain: scheduling, workflow, actions, AI seam
```


## The unified action contract (key design decision)

Every schedulable operation implements one interface:

```kotlin
interface ChronosAction {
    val type: String
    fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult
    suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult
    fun requiredCapabilities(): Set<Capability>
    fun executionPolicy(): ExecutionPolicy
}
```


Actions are registered in an ActionRegistry and looked up by type. The scheduler
never references concrete actions, so new capabilities (HTTP, Termux, smart-home)
are added by registering a new implementation - no changes to the engine.

Built-in actions (DefaultActions.registry()):

- notification - show a notification
- open_url - open an http(s) URL
- launch_app - launch an installed app by package name
- delay - wait a fixed duration

## Scheduling

- Recurrence + RecurrenceEngine expand calendar/interval rules into concrete
 occurrences. Clock-time rules ("every day at 08:00") are kept distinct from
 elapsed-time intervals ("every 24h").
- Schedule binds a recurrence (or fixed datetime) to an identity and enabled flag.
- ExecutionRouter decides whether a schedule should use an exact alarm or
 flexible work based on its kind and precision needs.

## Scheduling pipeline (M5)

The scheduler is split so that every decision is testable without a device, and
the Android layer only performs requests it is handed:

```
Room (schedules)  ->  ScheduleMapper  ->  ReconciliationPlanner  ->  ReconciliationPlan
                                                                          |
                                     AlarmScheduler.cancel/arm  <---------+
                                                                          |
                                                     AlarmManager -> AlarmReceiver
                                                                          |
                                     ScheduleExecutor: execution row + notification
                                                                          |
                                                     ScheduleReconciler (re-arm next)
```

- `ReconciliationPlanner` (in `chronos-core`, pure) decides, for every stored
  schedule, whether to cancel it, arm it, or skip it — and with which precision.
  It is the only place that reasons about "is this schedule still due?".
- `AlarmScheduler` is the only class that touches `AlarmManager`. It makes no
  decisions: it arms exactly when told to and exactness is available, otherwise it
  uses `setAndAllowWhileIdle` and the planner has already recorded the degradation.
- `ScheduleReconciler` re-plans the whole set and runs on boot, after a package
  replace, on clock or timezone change, on app start, and after every firing. It is
  idempotent: each pass cancels and re-arms the same set.
- `ScheduleExecutor` writes one `executions` row per attempt — including failures,
  with an error code — then re-arms. Nothing fails silently.
- `RecurrenceCodec` stores recurrences as ISO-8601 text in `recurrenceJson`, with a
  separate DTO so the domain model carries no serialization annotations.

## Workflow model and validation

- Versioned Workflow with a sealed WorkflowNode hierarchy (Action, Condition,
 Delay, Approval, Stop).
- WorkflowValidator produces structured errors (never throws to the UI) for schema
 problems, unknown action types, duplicate node ids and missing capabilities.
- Natural-language input is parsed into a ParsedIntent, compiled to a draft
 Workflow (IntentCompiler), then validated before it can run. AI/model output
 is never executed directly.

## On-device AI seam

LocalModelProvider is a replaceable interface. NoOpModelProvider represents the
"no model downloaded" state; the deterministic RuleBasedParser handles simple
commands (English / Hindi / Hinglish keywords) without any model. A LiteRT-LM /
FunctionGemma provider can be added later without touching the scheduler.

## Reliability principles

1. Schedules are stored durably and re-armed after boot/upgrade (ScheduleReconciler).
2. Exact-alarm access is checked at runtime and reconciled.
3. Failures are recorded and surfaced, never silently swallowed.

## UI & theming (backend-agnostic)

The Compose UI is deliberately decoupled from data sources and from concrete
colors:

- Design tokens live in ui/theme/Color.kt (ChronosColors) and ui/theme/Type.kt
 (ChronosType). ui/theme/Theme.kt exposes a single ChronosTheme wrapper with
 dark/light schemes. Screens read only MaterialTheme tokens, so the whole app
 can be re-themed or redesigned without touching business logic.
- Screens consume immutable UI state (e.g. HomeUiState) produced by ViewModels
 (e.g. HomeViewModel) that are backed by the repository. Switching the data
 source (Room today, remote later) or reworking the UI does not affect each other.
- Dependencies are provided by a small ServiceLocator (manual DI) because Hilt's
 Gradle plugin does not yet support AGP 9.2.0.
