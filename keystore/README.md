# Test signing key

`test-release.p12` is the key every CI build of Chronos is signed with.

| | |
|---|---|
| Store type | PKCS12 |
| Alias | `chronos` |
| Store password | `chronos-test` |
| Key password | `chronos-test` |
| Valid until | 2056-11-16 |

## Why it is in the repository

Android only installs an update over an existing app if both APKs carry the same
signing key. CI runners are ephemeral, so a key generated per run would produce
builds that refuse to install over each other. The alternatives were:

1. Store the key in GitHub Actions secrets - **not possible here**: the automation
   account has no `secrets: write` permission, and asking the maintainer to paste a
   key by hand is exactly the manual step this is meant to remove.
2. Commit a key that never changes.

Option 2 was chosen deliberately.

## What that costs

**This key is public. Treat every build signed with it as a test build, not a
trusted one.**

- Anyone can produce an APK signed with this key that installs over a Chronos
  installation, replacing it. Installing such an APK requires the user to accept a
  system prompt, so it needs to be delivered to them, but the signature itself
  provides no protection.
- The updater mitigates the common case, not the determined one: it downloads only
  from this repository's GitHub Releases over HTTPS and verifies the published
  SHA-256 before installing. An attacker who can publish to this repository, or
  serve content in place of GitHub, is not stopped by the checksum.

## When to replace it

Before any distribution that matters - Play Store, F-Droid, sharing an APK with
anyone who is not testing:

1. Generate a private key outside the repository.
2. Add it as `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS` and
   `KEY_PASSWORD` in the repository's Actions secrets.
3. Point `signingConfigs` in `app/build.gradle.kts` at those secrets instead of
   this file, and delete this directory.

Note that changing the key breaks updates: an APK signed with a new key will not
install over one signed with the old key, so the first build after the switch has
to be installed by uninstalling and reinstalling.
