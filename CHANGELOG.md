# Changelog

All notable changes to Chronos are documented here.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/);
versioning follows [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Self-updates: `UpdatePolicy` (pure), `UpdateManager`, `GitHubReleaseSource`,
  `AndroidUpdateInstaller`, `InstallResultReceiver` and a Settings section that
  checks, downloads, verifies and installs. A `Release` workflow publishes a signed
  APK and its SHA-256 on every push to `main`, versioned by workflow run number.
- Scheduling pipeline: `ReconciliationPlanner` (pure), `AlarmScheduler`,
  `ScheduleReconciler`, `AlarmReceiver`, `ScheduleExecutor` and `TimeChangeReceiver`.
  Schedules now arm on a real `AlarmManager`, fire, record an execution row and
  re-arm the next occurrence. Exact alarms are used when the user has granted
  access; otherwise the alarm is armed inexactly and the degradation is recorded.
- `RecurrenceCodec` and `ScheduleMapper`, closing a gap where `recurrenceJson` was a
  column nothing wrote or read, so a stored schedule could not be armed at all.
- Launcher icon (adaptive, vector) and notification icon.
- First-run guide: a six-step walkthrough that explains what Chronos does and asks
  for the four things Android requires from the user - notifications, exact-alarm
  access, battery-optimisation exemption and Do Not Disturb access - stating the
  consequence of refusing each one. Nothing is blocking, every permission can be
  re-checked from the guide, and the flow is driven by pure-Kotlin state so it is
  unit-tested without a device.
- Recurrence engine: daily, weekly, monthly and custom-interval rules, month-end and
  first-working-day handling, day-31 clamping, exclusion dates, inclusive start and
  end bounds.
- Action contract (`ChronosAction`) and `ActionRegistry` with capability introspection.
- Built-in actions: notification, open URL, launch app, delay.
- Versioned workflow model and deterministic `WorkflowValidator`.
- Rules-based natural-language parser (English / Hindi / Hinglish) and `IntentCompiler`
  producing validated workflow drafts.
- Replaceable `LocalModelProvider` AI seam with a simulated provider.
- `Schedule` model and `ExecutionRouter` (exact alarm vs flexible work).
- Room persistence: entities, DAOs, `ChronosDatabase` and `ScheduleRepository`.
- Compose UI shell with navigation across six sections, plus a boot receiver and
  manifest resources.
- CI workflow running unit tests and the debug build on every push and pull request.
- Documentation site (MkDocs Material) published to GitHub Pages from `docs/`.
- `docs/ROADMAP.md` recording milestone status against `FORGE_PLAN.md`.

### Changed

- Theme-agnostic Compose UI: centralised design tokens (`ChronosColors`, `ChronosType`)
  and a `ChronosTheme` wrapper, so screens read `MaterialTheme` only and the UI can be
  redesigned without touching logic.
- State-driven Home screen via `HomeUiState` + `HomeViewModel`, backed by the repository.
- Dependency injection is manual, through `ServiceLocator`, because Hilt's Gradle plugin
  does not support AGP 9.2.0.
- Donation channels finalised: GitHub Sponsors and Ko-fi (<https://ko-fi.com/puneetswarup>).

### Fixed

- README badges, documentation links and code fences restored after being flattened to
  bare URLs by the authoring toolchain.
- Gradle wrapper downloads from the official Gradle distribution host instead of a
  third-party mirror.
- The project-state notes no longer instruct pushing with a personal access token in the
  remote URL; that pattern leaks credentials into shell history and `.git/config`.

### Removed

- `docs/PROJECT_STATE.md` and `docs/AGENT_TRANSPORT_DIAGNOSTIC.md`. These were session
  notes about the authoring toolchain rather than Chronos documentation; their content
  lives in commit `5a9444a`, and the useful parts (toolchain constraints, milestone
  status) moved into `docs/CONTRIBUTING.md` and `docs/ROADMAP.md`.

## [0.1.0] - 2026-11-12

### Added

- Initial project scaffold: Gradle build, `chronos-core` and `app` modules, licence,
  and first documentation set.
