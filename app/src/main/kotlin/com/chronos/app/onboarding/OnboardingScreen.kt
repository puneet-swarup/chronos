package com.chronos.app.onboarding

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// The first-run guide. It explains what Chronos can do, then asks for the four
// things Android requires from the user, one at a time, with the consequence of
// refusing stated up front. Nothing is blocking and nothing is requested twice.
@Composable
fun OnboardingScreen(
    state: OnboardingUiState,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onFinish: () -> Unit,
) {
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { onRefresh() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    text = "Step " + state.stepNumber + " of " + state.stepCount,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = state.step.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(12.dp))
                Text(text = state.step.summary, style = MaterialTheme.typography.bodyLarge)

                if (!state.step.isInformational) {
                    Spacer(Modifier.height(20.dp))
                    PermissionCard(
                        state = state,
                        onAllow = {
                            when (state.step.action) {
                                OnboardingAction.REQUEST_NOTIFICATIONS -> {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        openNotificationSettings(context)
                                    }
                                }
                                OnboardingAction.OPEN_EXACT_ALARM_SETTINGS ->
                                    openExactAlarmSettings(context)
                                OnboardingAction.OPEN_BATTERY_SETTINGS ->
                                    openBatterySettings(context)
                                OnboardingAction.OPEN_DND_SETTINGS ->
                                    openSettings(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS, null)
                                OnboardingAction.NONE -> Unit
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
                        text = "Permissions granted so far: " +
                            state.capabilities.grantedCount + " of " + state.capabilities.totalCount +
                            ". Anything you skipped stays visible in Settings, and every action " +
                            "Chronos cannot perform will say so instead of failing quietly.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBack, enabled = !state.isFirstStep) {
                    Text("Back")
                }
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
private fun PermissionCard(
    state: OnboardingUiState,
    onAllow: () -> Unit,
    onRefresh: () -> Unit,
) {
    val granted = state.currentStepGranted == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (granted) "Granted" else if (state.step.essential) "Not granted yet - recommended" else "Not granted yet - optional",
                style = MaterialTheme.typography.labelLarge,
                color = if (granted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = state.step.consequence,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onAllow) {
                    Text(if (granted) "Open settings again" else "Allow")
                }
                TextButton(onClick = onRefresh) { Text("Check again") }
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        lines.forEach { line ->
            Text(text = "- " + line, style = MaterialTheme.typography.bodyMedium)
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
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        sections.forEach { (name, description) ->
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// Notification settings are addressed by the EXTRA_APP_PACKAGE extra, not by a
// package: URI like the other two screens.
private fun openNotificationSettings(context: Context) {
    runCatching {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

private fun openExactAlarmSettings(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        openSettings(
            context,
            Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
            context.packageName,
        )
    }
}

private fun openBatterySettings(context: Context) =
    openSettings(context, Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, context.packageName)

// Settings screens are not guaranteed to exist on every OEM build. A missing
// screen must leave the guide usable, so the failure is swallowed here and the
// card keeps showing "not granted", which is the truthful state.
private fun openSettings(context: Context, action: String, packageName: String?) {
    runCatching {
        val intent = if (packageName == null) {
            Intent(action)
        } else {
            Intent(action, Uri.parse("package:" + packageName))
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
