# Security Policy

Chronos is a local-first Android app. It has no server, no account and no telemetry,
which shapes both its threat model and what counts as a vulnerability.

## Supported versions

Only the latest commit on `main` is supported. Chronos is pre-alpha and has no
published release, so there are no maintenance branches.

| Version | Supported |
|---------|-----------|
| `main` | Yes |
| Anything older | No |

## Reporting a vulnerability

**Do not open a public issue.** Report privately through GitHub's
[private vulnerability reporting](https://github.com/puneet-swarup/chronos/security/advisories/new).

Please include:

- What the issue is and what an attacker gains from it.
- Steps to reproduce, or a proof-of-concept workflow / schedule JSON.
- Device, Android version and the commit you tested.
- Relevant logcat output, with anything personal removed.

## What we care about most

- **Workflow injection.** Model or imported-workflow output reaching execution
  without passing `WorkflowValidator`. The design rule is that AI output is never
  executed directly; a path that bypasses validation is a vulnerability.
- **Action capability escapes.** An action performing something outside its declared
  `requiredCapabilities()`, or a capability check that can be skipped.
- **Exported component abuse.** `BootReceiver` is exported; a crafted broadcast that
  triggers work, or any other exported surface reachable by another app.
- **Credential and data exposure.** Secrets in HTTP action configuration, model
  downloads without checksum verification, or schedules and execution history being
  readable by other apps.
- **Supply chain.** A dependency, Gradle wrapper or model artefact that can be
  substituted without detection.

## Out of scope

- Android platform limitations described in [docs/PERMISSIONS.md](docs/PERMISSIONS.md):
  background launch restrictions, Doze, force-stop, OEM task killers.
- Anything requiring root, Shizuku or ADB, unless Chronos grants it more privilege
  than the user asked for.
- Missing hardening on features that are not implemented yet (see the roadmap).

## Disclosure

Once a report arrives we aim to acknowledge it within 5 days, confirm or reject it
within 14 days, and ship a fix as soon as practical. Reporters are credited unless
they prefer to stay anonymous.
