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
import androidx.compose.material.icons.filled.DateRange
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
fun ScheduleScreen() = PlaceholderScreen(
    title = "Schedule",
    subtitle = "Alarms, recurring events and the task timeline.",
    detail = "The engine behind this is built and tested; the list and editor land next.",
    icon = { Icon(Icons.Filled.DateRange, contentDescription = null, modifier = Modifier.size(28.dp)) },
)

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
fun ActivityScreen() = PlaceholderScreen(
    title = "Activity",
    subtitle = "Execution history, failures and the audit trail.",
    detail = "Every execution is already recorded, including failures. This screen shows them next.",
    icon = { Icon(Icons.Filled.Info, contentDescription = null, modifier = Modifier.size(28.dp)) },
)

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
