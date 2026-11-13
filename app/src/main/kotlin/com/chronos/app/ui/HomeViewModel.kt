package com.chronos.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.app.data.ScheduleEntity
import com.chronos.app.data.ScheduleRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

// Immutable UI state consumed by composables.
data class HomeUiState(
 val scheduleCount: Int = 0,
 val enabledCount: Int = 0,
 val greeting: String = "Welcome to Chronos",
) {
 companion object {
 fun from(schedules: List<ScheduleEntity>): HomeUiState {
 val enabled = schedules.count { it.enabled }
 return HomeUiState(
 scheduleCount = schedules.size,
 enabledCount = enabled,
 greeting = if (schedules.isEmpty()) {
 "No schedules yet - create your first one"
 } else {
 "You have " + enabled + " active schedules"
 },
 )
 }
 }
}

class HomeViewModel(private val repository: ScheduleRepository) : ViewModel() {

 val state: StateFlow<HomeUiState> = repository.observeSchedules()
 .map { HomeUiState.from(it) }
 .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
}
