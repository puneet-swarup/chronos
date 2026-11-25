package com.chronos.app.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

// The first-run guide. It explains what Chronos can do, then asks for the four
// things Android requires from the user, one at a time, with the consequence of
// refusing stated up front.
//
// Two behaviours matter for how it feels: it re-reads permission state whenever
// the screen resumes (so returning from a system dialog is noticed), and it moves
// on by itself once the current step has been granted.
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFinish: () -> Unit,
    onActionUnavailable: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) onRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { onRefresh() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp)) {
            StepIndicator(current = state.stepNumber, total = state.stepCount)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = state.step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.step.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                if (!state.step.isInformational) {
                    Spacer(Modifier.height(20.dp))
                    PermissionCard(
                        state = state,
                        onAllow = {
                            val opened = when (state.step.action) {
                                OnboardingAction.REQUEST_NOTIFICATIONS -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        true
                                    } else {
                                        openNotificationSettings(context)
                                    }
                                }

                                OnboardingAction.OPEN_EXACT_ALARM_SETTINGS ->
                                    openExactAlarmSettings(context)

                                OnboardingAction.OPEN_BATTERY_SETTINGS ->
                                    openSettings(
                                        context,
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        context.packageName,
                                    )

                                OnboardingAction.OPEN_DND_SETTINGS ->
                                    openSettings(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, null)

                                OnboardingAction.NONE -> true
                            }
                            if (!opened) {
                                onActionUnavailable(
                                    "This device has no screen for that setting. Open Settings -> Apps -> " +
                                        "Chronos manually, then come back.",
                                )
                            }
                        },
                        onRefresh = onRefresh,
                    )
                }

                if (state.step == OnboardingStep.WELCOME) {
                    Spacer(Modifier.height(20.dp))
                    Principles()
                }

                if (state.step == OnboardingStep.TOUR) {
                    Spacer(Modifier.height(20.dp))
                    Tour()
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Permissions granted so far: " + state.capabilities.grantedCount +
                            " of " + state.capabilities.totalCount +
                            ". Anything you skipped stays visible in Settings, and every action " +
                            "Chronos cannot perform will say so instead of failing quietly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                state.message?.let { message ->
                    Spacer(Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack, enabled = !state.isFirstStep) { Text("Back") }
                Spacer(Modifier.weight(1f))
                if (!state.isLastStep) {
                    TextButton(onClick = onFinish) { Text("Skip") }
                }
                Spacer(Modifier.weight(1f))
                Button(onClick = { if (state.isLastStep) onFinish() else onNext() }) {
                    Text(if (state.isLastStep) "Start using Chronos" else "Next")
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { index ->
            val active = index < current
            Box(
                modifier = Modifier
                    .size(if (index == current - 1) 22.dp else 8.dp, 8.dp)
                    .background(
                        color = if (active) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun PermissionCard(
    state: OnboardingUiState,
    onAllow: () -> Unit,
    onRefresh: () -> Unit,
) {
    val granted = state.currentStepGranted == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (granted) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = when {
                    granted -> "Granted"
                    state.step.essential -> "Not granted yet - recommended"
                    else -> "Not granted yet - optional"
                },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (granted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.step.consequence,
                style = MaterialTheme.typography.bodyMedium,
                color = if (granted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (granted) {
                    Button(onClick = onRefresh) { Text("Re-check") }
                    TextButton(onClick = onAllow) { Text("Open settings") }
                } else {
                    Button(onClick = onAllow) { Text("Allow") }
                    TextButton(onClick = onRefresh) { Text("Re-check") }
                }
            }
        }
    }
}

@Composable
private fun Principles() {
    val lines = listOf(
        "Everything stays on this phone. No account, no cloud, no telemetry.",
        "Chronos only claims what Android actually permits, and says so when it cannot.",
        "Describe an automation in plain language; a deterministic compiler checks it before it can run.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        lines.forEach { line ->
            Row {
                Text(
                    text = "\u2022",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(10.dp))
                Text(
                    text = line,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Tour() {
    val sections = listOf(
        "Home" to "What is scheduled and whether it is healthy.",
        "Schedule" to "Create and edit alarms, reminders and recurring rules.",
        "Automations" to "Multi-step workflows: conditions, delays, approvals.",
        "AI Assistant" to "Describe what you want; review the proposal before saving.",
        "Activity" to "Every execution, including the ones that failed and why.",
        "Settings" to "Permissions, capabilities and this guide.",
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        sections.forEach { (name, description) ->
            Column {
                Text(text = name, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// Notification settings are addressed by the EXTRA_APP_PACKAGE extra, not by a
// package: URI like the others.
private fun openNotificationSettings(context: Context): Boolean = runCatching {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}.isSuccess

private fun openExactAlarmSettings(context: Context): Boolean {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
    return openSettings(context, Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, context.packageName)
}

// Returns false when the device has no activity for the intent, so the caller can
// explain it instead of leaving a button that appears dead.
private fun openSettings(context: Context, action: String, packageName: String?): Boolean = runCatching {
    val intent = if (packageName == null) {
        Intent(action)
    } else {
        Intent(action, Uri.parse("package:" + packageName))
    }
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}.isSuccess
