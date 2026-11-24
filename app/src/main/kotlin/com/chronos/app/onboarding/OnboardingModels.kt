package com.chronos.app.onboarding

// Pure-Kotlin onboarding model. Nothing here touches Android, so the step order,
// the gating rules and the summary maths are unit-testable on any JVM.

// What the user can do about a step. Each value maps to a concrete Android
// action in OnboardingScreen; NONE means the step is informational.
enum class OnboardingAction {
    NONE,
    REQUEST_NOTIFICATIONS,
    OPEN_EXACT_ALARM_SETTINGS,
    OPEN_BATTERY_SETTINGS,
    OPEN_DND_SETTINGS,
}

// One page of the first-run guide. The order of the enum constants is the order
// the user sees.
//
// `essential` means Chronos is much less useful without it; `optional` means the
// feature simply stays off. Nothing is mandatory: blocking a user behind a
// permission they do not want to grant would contradict the honesty principle.
enum class OnboardingStep(
    val title: String,
    val summary: String,
    val consequence: String,
    val action: OnboardingAction,
    val essential: Boolean,
) {
    WELCOME(
        title = "Welcome to Chronos",
        summary = "Schedule anything Android permits - alarms, reminders, app launches, " +
            "webhooks and multi-step workflows - and describe what you want in plain " +
            "English, Hindi or Hinglish.",
        consequence = "",
        action = OnboardingAction.NONE,
        essential = false,
    ),
    NOTIFICATIONS(
        title = "Notifications",
        summary = "Chronos delivers alarms and reminders as notifications. Without this " +
            "permission a schedule can still run, but you will not see that it did.",
        consequence = "Without it: alarms fire silently or not at all on Android 13+.",
        action = OnboardingAction.REQUEST_NOTIFICATIONS,
        essential = true,
    ),
    EXACT_ALARMS(
        title = "Exact alarms",
        summary = "Exact alarm access lets Chronos fire a schedule at 06:00 rather than " +
            "\"sometime in the next quarter of an hour\". Android grants it per app and " +
            "the user can revoke it later.",
        consequence = "Without it: schedules fall back to inexact delivery, which can be " +
            "minutes late.",
        action = OnboardingAction.OPEN_EXACT_ALARM_SETTINGS,
        essential = true,
    ),
    BATTERY(
        title = "Battery optimisation",
        summary = "Android puts idle apps into Doze, which can delay or drop background " +
            "work. Exempting Chronos keeps schedules reliable through the night.",
        consequence = "Without it: schedules may not run while the phone is idle.",
        action = OnboardingAction.OPEN_BATTERY_SETTINGS,
        essential = true,
    ),
    DO_NOT_DISTURB(
        title = "Do Not Disturb access",
        summary = "Automations that switch Do Not Disturb on or off need this special " +
            "access. It is only used when a workflow explicitly asks for it.",
        consequence = "Without it: DND actions are reported as unavailable, and nothing " +
            "else changes.",
        action = OnboardingAction.OPEN_DND_SETTINGS,
        essential = false,
    ),
    TOUR(
        title = "What is where",
        summary = "Six sections, each with one job. You can come back to this guide from " +
            "Settings at any time.",
        consequence = "",
        action = OnboardingAction.NONE,
        essential = false,
    ),
    ;

    val isInformational: Boolean get() = action == OnboardingAction.NONE

    companion object {
        val first: OnboardingStep get() = entries.first()
        val last: OnboardingStep get() = entries.last()
    }
}

// A snapshot of what the device currently allows. Defaults are the pessimistic
// answer, so a failed probe never looks like a granted permission.
data class DeviceCapabilities(
    val notificationsEnabled: Boolean = false,
    val exactAlarmsAllowed: Boolean = false,
    val batteryOptimisationIgnored: Boolean = false,
    val dndAccessGranted: Boolean = false,
) {
    fun isGranted(action: OnboardingAction): Boolean? = when (action) {
        OnboardingAction.NONE -> null
        OnboardingAction.REQUEST_NOTIFICATIONS -> notificationsEnabled
        OnboardingAction.OPEN_EXACT_ALARM_SETTINGS -> exactAlarmsAllowed
        OnboardingAction.OPEN_BATTERY_SETTINGS -> batteryOptimisationIgnored
        OnboardingAction.OPEN_DND_SETTINGS -> dndAccessGranted
    }

    val grantedCount: Int
        get() = listOf(
            notificationsEnabled,
            exactAlarmsAllowed,
            batteryOptimisationIgnored,
            dndAccessGranted,
        ).count { it }

    val totalCount: Int get() = 4
}

// Reads the live permission state. Implemented against Android in
// AndroidCapabilities.kt and faked in tests.
interface CapabilityProbe {
    fun read(): DeviceCapabilities
}

// Remembers that the guide has been seen, so it only opens on a first run.
interface OnboardingStore {
    fun isCompleted(): Boolean
    fun markCompleted()
    fun reset()
}

data class OnboardingUiState(
    val step: OnboardingStep = OnboardingStep.first,
    val capabilities: DeviceCapabilities = DeviceCapabilities(),
    val completed: Boolean = false,
) {
    val stepNumber: Int get() = step.ordinal + 1
    val stepCount: Int get() = OnboardingStep.entries.size
    val isFirstStep: Boolean get() = step == OnboardingStep.first
    val isLastStep: Boolean get() = step == OnboardingStep.last

    // True when this step asks the user for something and they have not granted
    // it yet. The screen uses it to decide whether to show the status badge.
    val currentStepGranted: Boolean? get() = capabilities.isGranted(step.action)
}
