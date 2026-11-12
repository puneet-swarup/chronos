# Permissions

Chronos follows a strict principle: never claim a capability works unless the
phone actually allows it. Permissions are requested only when a feature that
needs them is activated, never up-front.

## Capability levels

| Level | Meaning |
|-------|---------|
| NATIVE | Works with standard Android APIs |
| PERMISSION | Requires a user-granted runtime permission |
| SPECIAL_ACCESS | Requires a system settings toggle |
| INTEGRATION | Requires another app or service |
| ADVANCED | Requires Shizuku / ADB / root |
| UNSUPPORTED | Cannot be reliably performed on this device |

## Android permissions used

| Permission | Why |
|-----------|-----|
| POST_NOTIFICATIONS | Show alarms and reminders (runtime on API 33+) |
| SCHEDULE_EXACT_ALARM / USE_EXACT_ALARM | Fire alarms at an exact time |
| RECEIVE_BOOT_COMPLETED | Re-arm schedules after reboot |
| INTERNET / ACCESS_NETWORK_STATE | HTTP actions and on-demand model downloads |
| REQUEST_IGNORE_BATTERY_OPTIMIZATIONS | Ask to avoid Doze killing schedules |
| ACCESS_NOTIFICATION_POLICY | Do Not Disturb control |
| VIBRATE | Alarm/notification vibration |

## Honest limitations

An ordinary third-party Android app cannot:

- reliably launch other apps in the background on newer Android versions
 (Chronos falls back to a notification with a one-tap launch action);
- silently control Wi-Fi or mobile data (Chronos opens the relevant settings screen);
- guarantee execution while the device is powered off, force-stopped, or blocked
 by aggressive manufacturer background restrictions.

When a capability is unavailable, Chronos explains why and offers a supported
alternative instead of failing silently.
