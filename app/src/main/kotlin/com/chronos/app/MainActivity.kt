package com.chronos.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chronos.app.onboarding.DeviceCapabilities
import com.chronos.app.onboarding.OnboardingScreen
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
        setContent { ChronosTheme { ChronosRoot() } }
    }
}

// The first run shows the guide; every later launch goes straight to the app.
@Composable
fun ChronosRoot() {
    val onboarding = remember { ServiceLocator.newOnboardingViewModel() }
    val onboardingState by onboarding.state.collectAsState()

    if (onboardingState.completed) {
        ChronosApp(onReplayGuide = onboarding::reopen)
    } else {
        OnboardingScreen(
            state = onboardingState,
            onNext = onboarding::next,
            onBack = onboarding::back,
            onRefresh = onboarding::refresh,
            onFinish = onboarding::finish,
            onActionUnavailable = onboarding::reportActionUnavailable,
        )
    }
}

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

// Real icons rather than the first letter of each label: a letter in a circle
// reads as a placeholder, and the navigation is the most-seen surface in the app.
private val navItems = listOf(
    NavItem("home", "Home", Icons.Filled.Home),
    NavItem("schedule", "Schedule", Icons.Filled.DateRange),
    NavItem("automations", "Automations", Icons.Filled.Build),
    NavItem("ai", "AI", Icons.Filled.Star),
    NavItem("activity", "Activity", Icons.Filled.List),
    NavItem("settings", "Settings", Icons.Filled.Settings),
)

@Composable
fun ChronosApp(onReplayGuide: () -> Unit = {}) {
    val navController = rememberNavController()
    val homeViewModel = remember { ServiceLocator.newHomeViewModel() }
    val homeState by homeViewModel.state.collectAsState()
    val updateManager = remember { ServiceLocator.updateManager }
    val updateState by updateManager.state.collectAsState()
    val activityViewModel = remember { ServiceLocator.newActivityViewModel() }
    val activityState by activityViewModel.state.collectAsState()
    var permissions by remember { mutableStateOf(ServiceLocator.capabilityProbe.read()) }

    // Throttled inside the manager, so this is cheap on every cold start.
    LaunchedEffect(Unit) { updateManager.checkOnStart() }

    Scaffold(
        topBar = {
            Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Chronos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Your phone. Your rules. Your automation.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        },
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
                        icon = { Icon(item.icon, contentDescription = null) },
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
            composable("home") {
                HomeScreen(
                    state = homeState,
                    onCreateSchedule = { navController.navigate("schedule") },
                    onOpenSettings = { navController.navigate("settings") },
                )
            }
            composable("schedule") { ScheduleScreen() }
            composable("automations") { AutomationsScreen() }
            composable("ai") { AiAssistantScreen() }
            composable("activity") { ActivityScreen(activityState) }
            composable("settings") {
                // Permission state is re-read whenever this screen appears, so it
                // reflects what the user just changed outside the app.
                LaunchedEffect(Unit) { permissions = ServiceLocator.capabilityProbe.read() }
                SettingsScreen(
                    updateState = updateState,
                    currentVersion = BuildConfig.VERSION_NAME,
                    permissions = permissions,
                    onCheckForUpdates = updateManager::checkNow,
                    onInstall = updateManager::install,
                    onReplayGuide = onReplayGuide,
                    onRefreshPermissions = { permissions = ServiceLocator.capabilityProbe.read() },
                    onOpenInstallPermission = updateManager::openInstallPermissionSettings,
                )
            }
        }
    }
}
