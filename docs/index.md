# Chronos documentation

Chronos is a local-first Android automation and scheduling platform: alarms,
reminders, app launches, webhooks and multi-step workflows that run on your device
and nowhere else.

> **Status: pre-alpha.** The domain engine is implemented and tested; the Android
> app is a working shell. No alarm is armed on a device yet — see the
> [roadmap](ROADMAP.md) for the exact state.

## Start here

- [Architecture](ARCHITECTURE.md) — how the modules fit together and why.
- [Roadmap](ROADMAP.md) — milestone status, what works, what is next.
- [Permissions](PERMISSIONS.md) — every permission used and the limits Android imposes.
- [Testing](TESTING.md) — what is covered and how to run it.
- [Contributing](CONTRIBUTING.md) — setup, code style, commit and PR workflow.
- [Support](SUPPORT.md) — ways to help and donation channels.

## Design in one paragraph

All framework-independent logic lives in `chronos-core`, a pure Kotlin/JVM module
with no Android dependency, so recurrence maths, the workflow validator, the action
registry and the natural-language parser are unit-tested on any machine. The `app`
module adds the Compose UI, Room persistence, receivers and platform adapters.
Every schedulable capability implements one `ChronosAction` contract, so the
scheduler never references a concrete action, and model output is always validated
by a deterministic compiler before it can run.
