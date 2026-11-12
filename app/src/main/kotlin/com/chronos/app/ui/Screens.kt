package com.chronos.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Dashboard: next actions / active automations summary (PRD section 1).
@Composable
fun HomeScreen() = SectionScreen(
 title = "Dashboard",
 subtitle = "Upcoming actions, active workflows and system status.",
)

// Calendar, alarms and recurring events (PRD section 2).
@Composable
fun ScheduleScreen() = SectionScreen(
 title = "Schedule",
 subtitle = "Alarms, recurring events and the task timeline.",
)

// Workflow builder and triggers (PRD section 3).
@Composable
fun AutomationsScreen() = SectionScreen(
 title = "Automations",
 subtitle = "Build workflows from triggers, conditions and actions.",
)

// Natural-language creation and troubleshooting (PRD section 4).
@Composable
fun AiAssistantScreen() = SectionScreen(
 title = "AI Assistant",
 subtitle = "Describe an automation in words; Chronos proposes and validates it.",
)

// Execution history and diagnostics (PRD section 5).
@Composable
fun ActivityScreen() = SectionScreen(
 title = "Activity",
 subtitle = "Execution history, failures and the audit trail.",
)

// Permissions, models and modes (PRD section 6).
@Composable
fun SettingsScreen() = SectionScreen(
 title = "Settings",
 subtitle = "Permissions, integrations, AI models and standard/advanced mode.",
)

@Composable
private fun SectionScreen(title: String, subtitle: String) {
 Column(
 modifier = Modifier.fillMaxSize().padding(24.dp),
 verticalArrangement = Arrangement.Center,
 horizontalAlignment = Alignment.CenterHorizontally,
 ) {
 Text(text = title, style = MaterialTheme.typography.headlineMedium)
 Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
 }
}
