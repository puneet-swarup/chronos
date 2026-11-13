# Chronos — Implementation Plan (forge_plan.md)

> Derived from FORGE_TODO.md (Chronos PRD v1.1). This plan turns the product
> specification into an ordered engineering backlog with milestones.
> Locked decisions (from the PRD): Android, Kotlin, Jetpack Compose, Room,
> AlarmManager + WorkManager, local-first, on-device AI, plugin action registry.

---

## 0. Guiding principles (non-negotiable)

1. Reliability first — a scheduled task must survive app restart and reboot
 wherever Android permits it. Build and prove the scheduler before AI.
2. Transparency — the user must always know what will run and which
 permissions/access it needs.
3. Extensibility — every action is an independent module behind one
 ChronosAction contract; new actions are registered, not hard-coded.
4. Privacy / local-first — schedules, workflows and history stay on device;
 cloud AI is never required.
5. Honest failure recovery — record and surface failures, never pretend
 success. AI only proposes; a deterministic compiler validates.

---

## 1. Repository layout (target)


chronos/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat / gradle/wrapper/
├── chronos-core/ # pure Kotlin/JVM domain (no Android) — unit-tested
│ └── src/main|test/kotlin/com/chronos/core/...
├── app/ # Android application (Jetpack Compose)
│ └── src/main/...
└── forge_plan.md


Rationale: all scheduling math, the workflow model, the action contract and the
validator live in :chronos-core so they are testable on any JVM and reusable by
the Android layer. The app module adds Compose UI, Room, AlarmManager/WorkManager
adapters and platform receivers.

---

## 2. Core domain model (:chronos-core)

### 2.1 Scheduling model
- ScheduleKind: ONCE, RECURRENCE, INTERVAL, RELATIVE_EVENT, CONDITIONAL.
- Recurrence supports: daily / weekly (custom weekday set) / monthly
 (day-of-month, last-day, first-working-day) / custom interval.
- Start/end dates, exclusions/holiday calendars, timezone awareness.
- Distinguish clock-time events (every day at 08:00) from elapsed-time
 intervals (every 24h) — they are not interchangeable (PRD §Module 1).

### 2.2 Execution routing
| Schedule type | Mechanism |
|---|---|
| Precise alarm / reminder | AlarmManager exact (subject to access) |
| Flexible background task | WorkManager (persistent, constrained) |
| Location / event monitor | Android event APIs |

### 2.3 Workflow model (versioned JSON schema)
- Node types: Trigger, Action, Condition, Delay, EventWait, Loop, Parallel,
 Variable, Notification, Approval, Retry, Stop.
- Safety limits: max duration, max loop count, checkpoints, cancellation,
 idempotency, explicit reboot behavior, user-visible failure state.
- Execution state machine: SCHEDULED → TRIGGERED → RUNNING → COMPLETED,
 with WAITING (approval), FAILED, CANCELLED.

### 2.4 Action contract (the key extensibility decision)
kotlin
interface ChronosAction {
 val type: String
 fun validate(params: ActionParameters, ctx: CapabilityContext): ValidationResult
 suspend fun execute(ctx: ExecutionContext, params: ActionParameters): ActionResult
 fun requiredCapabilities(): Set<Capability>
 fun executionPolicy(): ExecutionPolicy
}

Each action declares: id, name, category, parameters, required permissions,
capability requirements, execution mode, timeout, retry policy, approval
requirement, result format. Initial registry: Alarm, Notification, LaunchApp,
OpenUrl, ChangeVolume, Dnd, HttpRequest, FileOperation (stubs + concrete impls
added incrementally).

---

## 3. Milestones (ordered backlog)

### M0 — Toolchain & skeleton (foundation) ⟵ in progress
- [x] settings.gradle.kts, root build.gradle.kts, gradle.properties.
- [x] Gradle wrapper (9.4.1) copied from known-good local toolchain.
- [x] :chronos-core module (Kotlin/JVM, JUnit) created.
- [x] app Android module scaffold (AGP 9.2.0, Compose, Navigation; Hilt deferred).
- [x] .gitignore, local.properties (sdk.dir=C:/Android/Sdk).
- Acceptance: gradlew :chronos-core:test runs green + :app:assembleDebug builds APK. DONE.

### M1 — Universal scheduler (core, pure Kotlin)
- [x] Recurrence engine (daily/weekly/monthly/custom-interval, last-day,
 first-working-day, exclusions, start/end bounds).
- [x] nextOccurrence(from) and occurrencesBetween(from,to).
- [x] Clock-time vs elapsed-interval separation.
- [~] DST / timezone handling via java.time.ZonedDateTime (java.time used; ZonedDateTime wrapper pending).
- [x] Acceptance: 9 unit tests cover recurrence edge cases. DONE.

### M2 — Workflow model + compiler/validator
- [x] Data classes for the versioned workflow JSON model (Workflow, WorkflowNode).
- [x] WorkflowValidator: schema, capability, duplicate, unknown-action rejection.
- [x] IntentCompiler: ParsedIntent -> draft Workflow (validated, never auto-run).
- [~] Execution state machine + checkpoint/idempotency metadata (modeled; runtime pending).
- [x] Acceptance: valid workflows compile; invalid return structured errors. DONE.

### M3 — Action registry
- [ ] ChronosAction, ActionResult, ActionParameters, ExecutionPolicy.
- [ ] ActionRegistry (register/lookup by type, capability introspection).
- [x] Concrete core actions: Notification, LaunchApp, OpenUrl, Delay (4 built-in).
- Acceptance: registry reports required capabilities; unknown action type
 is rejected by the validator.

### M4 — Android app shell (Compose)
- [ ] MainActivity, ChronosApplication, Navigation Compose.
- [ ] Six sections: Home, Schedule, Automations, AI Assistant, Activity,
 Settings (PRD §9).
- [ ] Hilt DI, Room database + entities (Schedule, Workflow, WorkflowNode,
 Trigger, Execution, ExecutionStep, Capability, Permission, Audit).
- Acceptance: app compiles and launches to the dashboard.

### M5 — Android scheduling adapters
- [ ] AlarmScheduler (AlarmManager exact, runtime permission checks,
 SCHEDULE_EXACT_ALARM reconciliation).
- [ ] WorkScheduler (WorkManager, constraints, retries).
- [ ] BootReceiver, timezone/clock-change receivers, app-upgrade recovery.
- [ ] Schedule reconciliation on app launch; duplicate-execution prevention.
- Acceptance: schedules survive reboot (manual test matrix, PRD §11).

### M6 — Android integrations
- [ ] Installed-app discovery (package visibility <queries>), launch, deep
 links, shortcuts.
- [ ] Device controls: volume, DND, flashlight, brightness (capability-gated).
- [ ] Event triggers: battery, network, Bluetooth, location, NFC/QR, calendar.
- [ ] CapabilityManager (live inventory) + AutomationSupervisor
 (broken schedule / missing permission / conflict detection).

### M7 — Local AI
- [ ] LocalModelProvider interface (replaceable runtime).
- [ ] LiteRT-LM provider; FunctionGemma 270M (Lite), Gemma 3 1B (Standard),
 Gemma 4 E2B (Pro) tiered selection by device profiler.
- [ ] Wi-Fi-only model manager (consent, checksum, resume, uninstall).
- [ ] Language engine: English / Hindi / Hinglish detection + normalisation;
 ambiguous-date clarification.
- [ ] NL → workflow proposal → compiler validation → user approval → save.
- Acceptance: AI acceptance criteria (PRD §6) — AI never executes directly;
 missing model falls back to rules engine without crashing.

### M8 — Advanced mode & release
- [ ] Shizuku / Termux / Tasker / shell action bridges (optional, gated).
- [ ] Import/export workflows, templates, remote dashboard (later).
- [ ] Security audit, Android Keystore credential storage, audit log.
- [ ] Device-compatibility testing (high / mid / aggressive-OEM phones).
- Acceptance: full acceptance matrix in PRD §11 passes.

---

## 4. Test strategy

| Layer | Tooling | Focus |
|---|---|---|
| Domain (:chronos-core) | JUnit 4 | recurrence math, validator, state machine, action registry |
| Android unit | JUnit + Robolectric + MockK | receivers, schedulers, notifiers |
| UI | Compose UI tests | navigation, forms |
| Instrumentation | AndroidJUnitRunner | alarm/reboot end-to-end |
| Acceptance | manual device matrix | PRD §11 reliability table |

Definition of done for every milestone: unit tests written alongside code,
gradlew test green, and PRD acceptance rows for that area satisfied.

---

## 5. Risks & mitigations

| Risk | Mitigation |
|---|---|
| OEM background restrictions | Diagnostics + honest reporting, test on Samsung/Xiaomi |
| SCHEDULE_EXACT_ALARM revoked | Permission-change receiver + schedule reconciliation |
| Background activity launch limits | Notification with one-tap launch fallback |
| AI inaccuracy | Specialist FunctionGemma + fine-tune on Chronos action catalogue; validator rejects invalid output |
| Large model on low-RAM phones | Tiered models, on-demand download, rules-engine fallback |
| Scope creep | Foundation (M0–M3) must ship before AI (M7) |

---

## 6. Immediate next actions

1. Finish M0: add app module scaffold + local.properties; verify
 gradlew :chronos-core:test passes.
2. Implement M1 recurrence engine with full unit-test coverage.
3. Implement M2/M3 workflow compiler, validator and action registry.
4. Only after M1–M3 are green, begin the Android UI (M4).

> Guiding principle: even a low-cost Android phone should run the complete
> scheduling engine; better hardware only adds intelligence.
