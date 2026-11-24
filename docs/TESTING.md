# Testing

## Philosophy

Tests are written alongside code. The pure-Kotlin `chronos-core` module is the
primary test target because it holds the logic that must be correct: recurrence
maths, the workflow validator, the action registry and the natural-language parser.
The Android module is tested with Robolectric so receivers, ViewModels and the Room
layer run on a plain JVM.

## Running tests

```bash
./gradlew test                  # all JVM unit tests
./gradlew :chronos-core:test    # core domain tests only
./gradlew :app:testDebugUnitTest
```

On Windows use `gradlew.bat`. CI runs the same commands on every push and pull
request.

## What is covered today

| Test class | Focus |
|------------|-------|
| `RecurrenceEngineTest` | daily/weekly/monthly recurrence, month-end, day-31 clamping, exclusions, interval vs clock-time, ranges, end date |
| `CoreActionsTest` | validation and execution of built-in actions, registry contents, capability reporting |
| `ScheduleAndRoutingTest` | next-occurrence computation, disabled schedules, exact-alarm vs flexible-work routing |
| `RuleBasedParserTest` | English/Hindi/Hinglish parsing, time extraction, URL detection, intent-to-workflow compilation and validation |
| `WorkflowValidatorTest` | valid workflows, unknown actions, duplicate ids, empty workflows, capability warnings |
| `HomeViewModelTest` | UI state emitted from a fake repository |

## What is not covered yet

- Anything that touches a real `AlarmManager` or `WorkManager` — the adapters do not
  exist yet (M5). This is the largest gap and the first thing to close.
- The Compose screens beyond the Home screen.
- DST and timezone transitions (M1 remainder).

## Device test matrix

The PRD defines a manual matrix for behaviour that only a real device can prove:

- App closed → eligible scheduled tasks still fire
- Device rebooted → eligible schedules are restored
- Timezone or clock changed → schedules are recalculated, not duplicated
- Exact-alarm access revoked → affected schedules are reported, not silently dropped
- Duplicate trigger → exactly one execution

Test on at least three classes of device: a flagship, a mid-range phone, and one with
aggressive background restrictions (Samsung, Xiaomi, Oppo).
