package com.chronos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Presentational screens. They read only MaterialTheme tokens and immutable UI
// state, never concrete colours or data sources, so the UI can be redesigned.

@Composable
fun HomeScreen(
    state: HomeUiState,
    onCreateSchedule: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = state.greeting,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(18.dp))

        NextRunCard(state.nextRunLabel)
        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                modifier = Modifier.weight(1f),
                value = state.enabledCount.toString(),
                label = "Active",
            )
            StatCard(
                modifier = Modifier.weight(1f),
                value = state.scheduleCount.toString(),
                label = "Total",
            )
        }

        if (state.missingPermissions.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            AttentionCard(state.missingPermissions, onOpenSettings)
        }

        Spacer(Modifier.height(20.dp))
        Button(onClick = onCreateSchedule, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text(if (state.hasSchedules) "Add another schedule" else "Create your first schedule")
        }
    }
}

@Composable
private fun NextRunCard(label: String?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "NEXT ALARM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = label ?: "Nothing scheduled",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (label == null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Create a schedule and it will show here once armed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, value: String, label: String) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AttentionCard(problems: List<String>, onOpenSettings: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Needs attention",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            Spacer(Modifier.height(8.dp))
            problems.forEach { problem ->
                Text(
                    text = "\u2022 " + problem,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
            TextButton(onClick = onOpenSettings) { Text("Review in Settings") }
        }
    }
}

// Sections that are still placeholders. Each says what it will do, so an empty
// screen is a statement of intent rather than a dead end.
@Composable
fun AutomationsScreen() = PlaceholderScreen(
    title = "Automations",
    subtitle = "Build workflows from triggers, conditions and actions.",
    detail = "The workflow model and its validator are done. The builder UI is not.",
    icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(28.dp)) },
)

@Composable
fun AiAssistantScreen() = PlaceholderScreen(
    title = "AI Assistant",
    subtitle = "Describe an automation in words; Chronos proposes and validates it.",
    detail = "A rules-based parser handles English, Hindi and Hinglish today. No model is bundled yet.",
    icon = { Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(28.dp)) },
)

@Composable
fun ActivityScreen(state: ActivityUiState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        Text(
            text = "Activity",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Everything Chronos attempted, including what failed.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        state.schedulerSummary?.let { summary ->
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Last scheduler pass",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }

        if (state.failedCount > 0) {
            Spacer(Modifier.height(14.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Text(
                    text = state.failedCount.toString() +
                        if (state.failedCount == 1) " run needs attention" else " runs need attention",
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        if (state.isEmpty) {
            Text(
                text = "Nothing has run yet. A schedule appears here the moment it fires - and so " +
                    "does anything that stops it from firing.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            state.rows.forEach { row ->
                ExecutionCard(row)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun ExecutionCard(row: ActivityRow) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = row.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = row.stateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (row.failed) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                )
            }
            if (row.whenLabel.isNotEmpty()) {
                Text(
                    text = row.whenLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            row.detail?.let { detail ->
                Spacer(Modifier.height(4.dp))
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    subtitle: String,
    detail: String,
    icon: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                icon()
            }
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}
