# Changelog

All notable changes to Chronos are documented here.
Format follows Keep a Changelog;
versioning follows Semantic Versioning.

## [Unreleased]

### Added
- Core recurrence engine (daily/weekly/monthly/custom-interval, last-day,
 first-working-day, exclusions, start/end bounds).
- Action contract (ChronosAction) and ActionRegistry.
- Built-in actions: notification, open_url, launch_app, delay.
- Versioned workflow model and deterministic WorkflowValidator.
- Rules-based natural-language parser (English / Hindi / Hinglish) and
 IntentCompiler producing validated workflows.
- Replaceable LocalModelProvider AI seam with NoOpModelProvider.
- Schedule model and ExecutionRouter (exact alarm vs flexible work).
- Android app shell: Compose navigation across six sections, boot receiver,
 manifest and resources.
- Documentation: README, architecture, permissions, testing, contributing,
 support/donations, license and this changelog.

## [0.1.0] - 2026-11-12

### Added
- Initial project scaffold: Gradle build, chronos-core and app modules.
