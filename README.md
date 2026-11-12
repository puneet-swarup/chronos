# Chronos

Your phone. Your rules. Your automation.

Chronos is a local-first Android automation and scheduling platform. It goes far beyond
alarms: schedule anything that Android permits - alarms, reminders, app launches,
webhooks, workflows and conditional automations - and (optionally) describe what you
want in plain English, Hindi or Hinglish.

> Status: early development. The core scheduling engine, action registry, workflow
> validator and rules-based natural-language parser are implemented and unit-tested.
> The Android UI is a working Compose shell; Room persistence and on-device AI are
> the next milestones (see the roadmap).

## Why Chronos

- Reliability first - schedules are designed to survive app restarts and reboots
 wherever Android allows it.
- Local-first & private - schedules and workflows stay on your device; cloud AI is
 never required.
- Extensible - every capability is a module behind one ChronosAction contract.
- Honest - the app reports failures and permission problems instead of silently
 pretending tasks succeeded.
- Accessible - even a low-cost Android phone runs the complete scheduling engine;
 better hardware only adds intelligence.

## Feature overview

| Area | What Chronos does |
|------|-------------------|
| Universal scheduler | One-time, recurring (daily/weekly/monthly/custom), interval and conditional schedules |
| Alarms & reminders | Precise alarms, escalation, snooze policies |
| App control | Launch apps, deep links, shortcuts (subject to Android limits) |
| Device control | Volume, DND, flashlight and other permitted settings |
| Workflows | Sequences, conditions, delays, retries, approval steps |
| Natural language | Describe an automation; a deterministic compiler validates it |
| Integrations | HTTP/webhooks and (advanced mode) Termux/Tasker/Shizuku |
| Diagnostics | Execution history, failure reporting, capability checks |

## Project structure


chronos/
 chronos-core/ Pure Kotlin/JVM domain: recurrence, workflow model, actions, validator,
 rules-based NL parser. No Android dependency - unit-tested on any JVM.
 app/ Android application (Jetpack Compose, Navigation).
 docs/ Architecture, permissions, testing and support documentation.


## Build & test

Requirements: JDK 17 and the Android SDK (platform 36).

bash
# Run all unit tests (pure JVM, fast)
./gradlew test

# Build the debug APK
./gradlew :app:assembleDebug


On Windows use gradlew.bat. The debug APK is written to
app/build/outputs/apk/debug/app-debug.apk.

## Documentation

- Architecture
- Permissions
- Testing
- Contributing
- Support & donations
- Changelog

## Roadmap

See FORGE_PLAN.md for the milestone-by-milestone backlog (M0-M8).

## License

MIT - see LICENSE.
