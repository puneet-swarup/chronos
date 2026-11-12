# Contributing to Chronos

Thanks for your interest! Chronos aims to be a reliable, private, extensible
Android automation platform.

## Ground rules

- Reliability and honesty over feature count. Do not claim a capability works
 unless the phone actually allows it.
- Local-first by default. No cloud dependency for core scheduling.
- Small, focused, tested changes.

## Development setup

1. Install JDK 17 and the Android SDK (platform 36).
2. Clone the repository.
3. Run ./gradlew test to confirm the baseline is green.

## Adding a new action

1. Implement ChronosAction in chronos-core/.../action/impl/.
2. Register it in DefaultActions.registry().
3. Add unit tests in chronos-core/src/test/.
4. Document required capabilities in docs/PERMISSIONS.md if new permissions
 are involved.

## Code style

- Kotlin official style (kotlin.code.style=official).
- Prefer small files and clear names.
- Handle errors explicitly; never swallow failures silently.

## Pull requests

- Keep the build and all tests green.
- Describe what changed and why, and note any permission or reliability impact.
