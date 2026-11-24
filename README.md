# Chronos

**Your phone. Your rules. Your automation.**

[![CI](https://github.com/puneet-swarup/chronos/actions/workflows/ci.yml/badge.svg)](https://github.com/puneet-swarup/chronos/actions/workflows/ci.yml)
[![Docs](https://img.shields.io/badge/docs-GitHub%20Pages-blue)](https://puneet-swarup.github.io/chronos/)
[![License: PolyForm Noncommercial 1.0.0](https://img.shields.io/badge/license-PolyForm%20Noncommercial%201.0.0-orange.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/platform-Android%20API%2028%2B-3DDC84.svg)](https://developer.android.com/about/versions)

Chronos is a local-first Android automation and scheduling platform. It goes far
beyond alarms: schedule anything Android permits — alarms, reminders, app launches,
webhooks, multi-step workflows and conditional automations — and optionally describe
what you want in plain English, Hindi or Hinglish.

Everything stays on your device. No account, no cloud, no telemetry.

> **Status: pre-alpha.** The scheduling engine, workflow validator, action registry
> and rules-based natural-language parser are implemented and unit-tested. The
> Android app is a working Compose shell on top of a Room database. Nothing is
> published on Play; there is no downloadable APK yet.

## What works today

| Area | State |
|------|-------|
| Recurrence engine — daily / weekly / monthly / interval, month-end, first working day, exclusions, start & end bounds | Implemented, tested |
| Schedule model + execution routing (exact alarm vs. flexible work) | Implemented, tested |
| Workflow model + deterministic validator | Implemented, tested |
| Action contract + registry, 4 built-in actions (notification, open URL, launch app, delay) | Implemented, tested |
| Rules-based parser (English / Hindi / Hinglish) → validated workflow draft | Implemented, tested |
| Room persistence (entities, DAOs, repository) | Implemented, wired through the app |
| Compose UI shell, Home screen, theming | Implemented (Home wired; other sections are static) |

## What does not work yet

Being explicit about this is the point of the project:

- **No alarm is actually armed.** `AlarmScheduler` / `WorkScheduler` are not written
  yet; `ScheduleReconciler` is a logging stub. The routing decision exists, the
  execution does not.
- **No on-device AI.** `LocalModelProvider` is a seam with a simulated implementation;
  no LiteRT-LM model is bundled or downloaded.
- **Only the Home screen reads real data.** Schedule, Automations, AI, Activity and
  Settings are layout-only.
- **No app icon** and no release build pipeline yet.

## Why Chronos

- **Reliability first** — schedules are designed to survive app restarts and reboots,
  wherever Android allows it.
- **Local-first and private** — schedules and workflows never leave the device; cloud
  AI is never required.
- **Extensible** — every capability is a module behind one `ChronosAction` contract.
- **Honest** — the app reports failures and permission problems instead of silently
  pretending a task succeeded.
- **Accessible** — a low-cost Android phone runs the full scheduling engine; better
  hardware only adds intelligence.

## Project structure

```
chronos/
├── chronos-core/   Pure Kotlin/JVM domain: recurrence, schedules, workflow model,
│                   action registry, validator, rules-based NL parser. No Android
│                   dependency — unit-tested on any JVM.
├── app/            Android application: Compose UI, Navigation, Room, receivers,
│                   manual dependency wiring.
├── docs/           Architecture, roadmap, permissions, testing, contributing,
│                   support. Published as a documentation site.
├── FORGE_PLAN.md   Milestone backlog (M0–M8) and risk register.
└── FORGE_TODO.md   Working notes and research gathered while planning.
```

## Build and test

Requirements: JDK 17 and the Android SDK (platform 36, build-tools 36.0.0).

```bash
# Unit tests (pure JVM for the core, Robolectric for the app)
./gradlew test

# Core domain tests only — fast
./gradlew :chronos-core:test

# Debug APK
./gradlew :app:assembleDebug
```

On Windows use `gradlew.bat`. The debug APK is written to
`app/build/outputs/apk/debug/app-debug.apk`.

## Documentation

| Document | Contents |
|----------|----------|
| [Documentation site](https://puneet-swarup.github.io/chronos/) | Rendered version of everything in `docs/` |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Module layout, action contract, scheduling, AI seam, theming |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Milestone status: what is done, what is next |
| [docs/PERMISSIONS.md](docs/PERMISSIONS.md) | Every permission used, and the honest limits of Android |
| [docs/TESTING.md](docs/TESTING.md) | Test strategy, coverage today, device matrix |
| [docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) | Setup, code style, commit and PR workflow |
| [docs/SUPPORT.md](docs/SUPPORT.md) | Ways to help, donation channels |
| [CHANGELOG.md](CHANGELOG.md) | Notable changes per version |

## Contributing

Issues and pull requests are welcome. Read
[docs/CONTRIBUTING.md](docs/CONTRIBUTING.md) and
[CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) first. Security issues should follow
[SECURITY.md](SECURITY.md) — please do not open a public issue for them.

## License

Licensed under [PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0/).
Commercial use requires a licence — write to puneetswarup@gmail.com.
