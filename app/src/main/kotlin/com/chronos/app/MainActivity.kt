package com.chronos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chronos.app.ui.ActivityScreen
import com.chronos.app.ui.AiAssistantScreen
import com.chronos.app.ui.AutomationsScreen
import com.chronos.app.ui.HomeScreen
import com.chronos.app.ui.ScheduleScreen
import com.chronos.app.ui.SettingsScreen
import com.chronos.app.ui.theme.ChronosTheme

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
 super.onCreate(savedInstanceState)
 setContent { ChronosTheme { ChronosApp() } }
 }
}

private data class NavItem(val route: String, val label: String)

private val navItems = listOf(
 NavItem("home", "Home"),
 NavItem("schedule", "Schedule"),
 NavItem("automations", "Automations"),
 NavItem("ai", "AI"),
 NavItem("activity", "Activity"),
 NavItem("settings", "Settings"),
)

@Composable
fun ChronosApp() {
 val navController = rememberNavController()
 val homeViewModel = ServiceLocator.newHomeViewModel()
 val homeState by homeViewModel.state.collectAsState()

 Scaffold(
 bottomBar = {
 val backStack by navController.currentBackStackEntryAsState()
 val current = backStack?.destination
 NavigationBar {
 navItems.forEach { item ->
 NavigationBarItem(
 selected = current?.hierarchy?.any { it.route == item.route } == true,
 onClick = {
 navController.navigate(item.route) {
 popUpTo(navController.graph.startDestinationId) { saveState = true }
 launchSingleTop = true
 restoreState = true
 }
 },
 label = { Text(item.label) },
 icon = { Text(item.label.take(1)) },
 )
 }
 }
 },
 ) { innerPadding ->
 NavHost(
 navController = navController,
 startDestination = "home",
 modifier = Modifier.padding(innerPadding),
 ) {
 composable("home") { HomeScreen(homeState) }
 composable("schedule") { ScheduleScreen() }
 composable("automations") { AutomationsScreen() }
 composable("ai") { AiAssistantScreen() }
 composable("activity") { ActivityScreen() }
 composable("settings") { SettingsScreen() }
 }
 }
}
