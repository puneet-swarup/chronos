# Testing

## Philosophy

Tests are written alongside code. The pure-Kotlin chronos-core module is the
primary test target because it holds all the logic that must be correct: recurrence
math, the workflow validator, the action registry and the natural-language parser.

## Running tests

bash
./gradlew test # all JVM unit tests
./gradlew :chronos-core:test # core domain tests only


On Windows use gradlew.bat.

## What is covered today

| Test class | Focus |
|------------|-------|
| RecurrenceEngineTest | daily/weekly/monthly recurrence, month-end, day-31 clamping, exclusions, interval vs clock-time, ranges, end-date |
| CoreActionsTest | validation + execution of built-in actions, registry contents, capability reporting |
| ScheduleAndRoutingTest | next-occurrence computation, disabled schedules, exact-alarm vs flexible-work routing |
| RuleBasedParserTest | English/Hindi/Hinglish parsing, time extraction, URL detection, intent-to-workflow compilation + validation |
| WorkflowValidatorTest | valid workflows, unknown actions, duplicate ids, empty workflows, capability warnings |

## Test matrix (device)

The PRD defines a manual device matrix for areas that require a real device
(alarms, reboot recovery, OEM background restrictions):

- Application closed -> eligible scheduled tasks continue
- Device rebooted -> eligible schedules are restored
- Timezone / clock changed -> schedules are recalculated
- Exact-alarm access revoked -> affected schedules are reported
- Duplicate trigger -> no duplicate execution

Test on at least: a flagship device, a mid-range device, and a device with
aggressive background restrictions (e.g. Samsung, Xiaomi).
