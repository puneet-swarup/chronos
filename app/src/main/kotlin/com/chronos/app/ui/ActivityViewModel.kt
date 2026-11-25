package com.chronos.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.app.data.ScheduleRepository
import java.time.Clock
import java.time.LocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

// Reads what the executor and the reconciler have already recorded. The point of
// this screen is that nothing Chronos attempts disappears silently.
class ActivityViewModel(
    repository: ScheduleRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    val state: StateFlow<ActivityUiState> = combine(
        repository.observeExecutions(),
        repository.observeAudit(),
        repository.observeSchedules(),
    ) { executions, audit, schedules ->
        ActivityUiState.from(
            executions = executions,
            audit = audit,
            scheduleNames = schedules.associate { it.id to it.name },
            now = LocalDateTime.now(clock),
            zone = clock.zone,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActivityUiState())
}
