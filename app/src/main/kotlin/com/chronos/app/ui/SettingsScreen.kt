package com.chronos.app.ui

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.chronos.app.update.ReleaseInfo
import com.chronos.app.update.UpdateState

// Settings now has one real section: the updater. The rest of the screen is
// still a placeholder, and says so rather than pretending otherwise.
@Composable
fun SettingsScreen(
    updateState: UpdateState,
    currentVersion: String,
    onCheckForUpdates: () -> Unit,
    onInstall: (ReleaseInfo) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Chronos " + currentVersion,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Updates",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(8.dp))
        UpdateCard(
            state = updateState,
            onCheckForUpdates = onCheckForUpdates,
            onInstall = onInstall,
        )

        Spacer(Modifier.height(24.dp))
        Text(
            text = "Coming here: permissions, integrations, AI models and advanced mode.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UpdateCard(
    state: UpdateState,
    onCheckForUpdates: () -> Unit,
    onInstall: (ReleaseInfo) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (state) {
                is UpdateState.Idle -> {
                    Text("Chronos checks for new builds when it starts.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = onCheckForUpdates) { Text("Check now") }
                }

                is UpdateState.Checking -> {
                    Text("Checking for updates…", style = MaterialTheme.typography.bodyMedium)
                }

                is UpdateState.UpToDate -> {
                    Text(
                        text = "You are on the latest build (" + state.currentVersionName + ").",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onCheckForUpdates) { Text("Check again") }
                }

                is UpdateState.Available -> {
                    Text(
                        text = "Version " + state.release.versionName + " is available",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    state.release.notes?.takeIf { it.isNotBlank() }?.let { notes ->
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = notes.trim().lines().take(6).joinToString("\n"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Android will ask you to confirm the install.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onInstall(state.release) }) { Text("Download and install") }
                        TextButton(onClick = onCheckForUpdates) { Text("Later") }
                    }
                }

                is UpdateState.Downloading -> {
                    Text(
                        text = "Downloading " + state.release.versionName + "…",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                is UpdateState.Installing -> {
                    Text("Handing the update to Android…", style = MaterialTheme.typography.bodyMedium)
                }

                is UpdateState.Installed -> {
                    Text(
                        text = "Installed " + state.release.versionName + ". Reopen Chronos to finish.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                is UpdateState.Failed -> {
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = onCheckForUpdates) { Text("Try again") }
                }
            }
        }
    }
}
