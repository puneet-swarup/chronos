# Contributing to Chronos

Thanks for your interest. Chronos aims to be a reliable, private, extensible Android
automation platform, and it would rather do less correctly than more dishonestly.

## Ground rules

- **Reliability and honesty over feature count.** Do not claim a capability works
  unless the phone actually allows it.
- **Local-first by default.** No cloud dependency for core scheduling.
- **Small, focused, tested changes.** One concern per pull request.

## Development setup

1. Install JDK 17 (Temurin or Microsoft OpenJDK) and the Android SDK with platform
   36 and build-tools 36.0.0.
2. Clone the repository.
3. Run `./gradlew test` to confirm the baseline is green.

```bash
./gradlew test                     # all unit tests
./gradlew :chronos-core:test       # core domain tests only, no Android SDK needed
./gradlew :app:assembleDebug       # debug APK
```

On Windows use `gradlew.bat`.

## Toolchain constraints

These are deliberate, and the build breaks if they are "fixed" naively:

- **Hilt is not usable.** Its Gradle plugin does not support AGP 9.2.0
  (`Android BaseExtension not found`). Dependency injection is manual, through
  `ServiceLocator`.
- **Room 2.7.1 is the minimum**, for Kotlin 2.0 + KSP2 compatibility.
- **`android.disallowKotlinSourceSets=false`** in `gradle.properties` is required so
  Room's KSP processor can contribute generated sources under AGP 9.
- **Kotlin 2.0.21** is pinned across the compiler and the Compose plugin; Dependabot
  is configured not to bump it out from under the build.

## Adding a new action

1. Implement `ChronosAction` in `chronos-core/src/main/kotlin/com/chronos/core/action/impl/`.
2. Register it in `DefaultActions.registry()`.
3. Add unit tests under `chronos-core/src/test/`.
4. Document any new permission or capability in `docs/PERMISSIONS.md`.

## Code style

- Kotlin official style (`kotlin.code.style=official`), enforced for whitespace and
  line endings by `.editorconfig`.
- Prefer small files and clear names.
- Handle errors explicitly; never swallow a failure silently.

## Commit messages

[Conventional Commits](https://www.conventionalcommits.org/): `feat:`, `fix:`,
`docs:`, `test:`, `refactor:`, `chore:`, `ci:`. One logical change per commit, and a
subject that says what changed — `state` or `wip` tells a reader nothing.

## Pull requests

1. Branch from `main` (`feat/alarm-scheduler`, `fix/day-31-clamping`).
2. Keep the build and all tests green.
3. Describe what changed, why, and any permission or reliability impact.
4. Fill in the pull request template.

`main` is protected; changes should land through a pull request, including the maintainer's.

## Documentation

The docs in `docs/` are published as a site with MkDocs Material. To preview them:

```bash
pip install mkdocs-material
mkdocs serve
```

Then open <http://127.0.0.1:8000>. CI runs `mkdocs build --strict`, so a broken
internal link or a missing nav entry fails the build.

## Licence of contributions

Chronos is licensed under PolyForm Noncommercial 1.0.0. By opening a pull request you
agree that your contribution is distributed under the same licence.
