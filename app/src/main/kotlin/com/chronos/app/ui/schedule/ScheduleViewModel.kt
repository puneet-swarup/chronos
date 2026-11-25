package com.chronos.app.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chronos.app.data.ScheduleMapper
import com.chronos.app.data.ScheduleRepository
import com.chronos.app.scheduling.ScheduleReconciler
import com.chronos.app.ui.NextRunFormatter
import com.chronos.core.scheduling.ScheduleKind
import java.time.Clock
import java.time.LocalDateTime
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Owns the schedule list and the editor. Saving a schedule re-runs reconciliation
// immediately, so a new alarm is armed before the user leaves the screen rather
// than at the next boot.
class ScheduleViewModel(
    private val repository: ScheduleRepository,
    private val reconciler: ScheduleReconciler,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val draft = MutableStateFlow<ScheduleDraft?>(null)
    private val errors = MutableStateFlow<List<String>>(emptyList())
    private val saving = MutableStateFlow(false)

    val state: StateFlow<ScheduleUiState> = combine(
        repository.observeSchedules(),
        draft,
        errors,
        saving,
    ) { schedules, currentDraft, currentErrors, isSaving ->
        val now = LocalDateTime.now(clock)
        ScheduleUiState(
            rows = schedules.map { entity ->
                val next = entity.nextRun?.let { text ->
                    runCatching { LocalDateTime.parse(text) }.getOrNull()
                }
                ScheduleRow(
                    id = entity.id,
                    name = entity.name,
                    whenLabel = NextRunFormatter.format(next, now) ?: "Not armed",
                    repeatLabel = repeatLabelFor(entity.kind, entity.recurrenceJson != null),
                    enabled = entity.enabled,
                )
            },
            draft = currentDraft,
            errors = currentErrors,
            saving = isSaving,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ScheduleUiState())

    fun startNew() {
        draft.value = ScheduleDraft()
        errors.value = emptyList()
    }

    fun cancelEdit() {
        draft.value = null
        errors.value = emptyList()
    }

    fun updateDraft(updated: ScheduleDraft) {
        draft.value = updated
    }

    fun save() {
        val current = draft.value ?: return
        val problems = current.validate()
        if (problems.isNotEmpty()) {
            errors.value = problems
            return
        }
        saving.value = true
        viewModelScope.launch {
            try {
                val schedule = current.toSchedule(UUID.randomUUID().toString(), LocalDateTime.now(clock))
                repository.upsert(ScheduleMapper.toEntity(schedule))
                // Arm it now, so the list shows a real next run immediately.
                reconciler.reconcile()
                draft.value = null
                errors.value = emptyList()
            } catch (t: Throwable) {
                errors.value = listOf("Could not save the schedule: " + (t.message ?: "unknown error"))
            } finally {
                saving.value = false
            }
        }
    }

    fun setEnabled(id: String, enabled: Boolean) {
        viewModelScope.launch {
            val entity = repository.get(id) ?: return@launch
            repository.upsert(entity.copy(enabled = enabled))
            reconciler.reconcile()
        }
    }

    fun delete(id: String) {
        viewModelScope.launch {
            repository.delete(id)
            reconciler.reconcile()
        }
    }

    private fun repeatLabelFor(kind: String, hasRecurrence: Boolean): String = when {
        kind == ScheduleKind.FIXED.name -> "Once"
        !hasRecurrence -> "Custom"
        else -> "Repeats"
    }
}
