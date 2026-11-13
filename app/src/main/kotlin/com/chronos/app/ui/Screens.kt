package com.chronos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Presentational screens. They read only MaterialTheme tokens and immutable UI
// state, never concrete colors or data sources, so the UI can be redesigned.

@Composable
fun HomeScreen(state: HomeUiState) {
 SectionScreen(
 title = "Dashboard",
 subtitle = state.greeting,
 detail = state.scheduleCount.toString() + " schedules, " + state.enabledCount + " active",
 )
}

@Composable
fun ScheduleScreen() = SectionScreen(
 title = "Schedule",
 subtitle = "Alarms, recurring events and the task timeline.",
)

@Composable
fun AutomationsScreen() = SectionScreen(
 title = "Automations",
 subtitle = "Build workflows from triggers, conditions and actions.",
)

@Composable
fun AiAssistantScreen() = SectionScreen(
 title = "AI Assistant",
 subtitle = "Describe an automation in words; Chronos proposes and validates it.",
)

@Composable
fun ActivityScreen() = SectionScreen(
 title = "Activity",
 subtitle = "Execution history, failures and the audit trail.",
)

@Composable
fun SettingsScreen() = SectionScreen(
 title = "Settings",
 subtitle = "Permissions, integrations, AI models and standard/advanced mode.",
)

@Composable
private fun SectionScreen(title: String, subtitle: String, detail: String? = null) {
 Column(
 modifier = Modifier.fillMaxSize().padding(24.dp),
 verticalArrangement = Arrangement.Center,
 horizontalAlignment = Alignment.CenterHorizontally,
 ) {
 Text(text = title, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onBackground)
 Text(text = subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
 if (detail != null) {
 Text(text = detail, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
 }
 }
}
