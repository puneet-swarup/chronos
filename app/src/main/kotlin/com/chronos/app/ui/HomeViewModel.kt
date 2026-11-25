package com.chronos.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.app.data.ScheduleEntity
import com.chronos.app.data.ScheduleRepository
import com.chronos.app.onboarding.CapabilityProbe
import com.chronos.app.onboarding.DeviceCapabilities
import java.time.Clock
import java.time.LocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Immutable UI state consumed by composables.
data class HomeUiState(
    val scheduleCount: Int = 0,
    val enabledCount: Int = 0,
    val greeting: String = "Welcome to Chronos",
    // "Today 07:30", or null when nothing is armed.
    val nextRunLabel: String? = null,
    // Permission problems, phrased as consequences. Empty when all is well.
    val missingPermissions: List<String> = emptyList(),
) {
    val hasSchedules: Boolean get() = scheduleCount > 0

    companion object {
        // capabilities = null means "unknown", which suppresses the warnings
        // rather than inventing problems.
        fun from(
            schedules: List<ScheduleEntity>,
            capabilities: DeviceCapabilities? = null,
            now: LocalDateTime? = null,
        ): HomeUiState {
            val enabled = schedules.count { it.enabled }
            val greeting = if (schedules.isEmpty()) {
                "No schedules yet - create your first one"
            } else {
                "You have " + enabled + " active schedules"
            }

            val nextLabel = if (now == null) {
                null
            } else {
                val runs = schedules
                    .filter { it.enabled }
                    .mapNotNull { entity ->
                        entity.nextRun?.let { text ->
                            runCatching { LocalDateTime.parse(text) }.getOrNull()
                        }
                    }
                NextRunFormatter.format(NextRunFormatter.soonest(runs, now), now)
            }

            return HomeUiState(
                scheduleCount = schedules.size,
                enabledCount = enabled,
                greeting = greeting,
                nextRunLabel = nextLabel,
                missingPermissions = capabilities?.missing().orEmpty(),
            )
        }
    }
}

class HomeViewModel(
    private val repository: ScheduleRepository,
    private val probe: CapabilityProbe? = null,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    val state: StateFlow<HomeUiState> = repository.observeSchedules()
        .map { HomeUiState.from(it, probe?.read(), LocalDateTime.now(clock)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
