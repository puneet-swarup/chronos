package com.chronos.app.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Drives the first-run guide. Deliberately dependency-free: the probe and the
// store are interfaces, so this class is unit-tested without Robolectric.
class OnboardingViewModel(
    private val probe: CapabilityProbe,
    private val store: OnboardingStore,
) : ViewModel() {

    private val _state = MutableStateFlow(
        OnboardingUiState(
            step = OnboardingStep.first,
            capabilities = probe.read(),
            completed = store.isCompleted(),
        ),
    )
    val state: StateFlow<OnboardingUiState> = _state.asStateFlow()

    // Called on entering the screen, when the user returns from a system
    // settings screen, and after a permission dialog resolves.
    fun refresh() {
        _state.update { it.copy(capabilities = probe.read()) }
    }

    fun next() {
        _state.update { current ->
            val following = OnboardingStep.entries.getOrNull(current.step.ordinal + 1)
            if (following == null) current else current.copy(step = following)
        }
    }

    fun back() {
        _state.update { current ->
            val previous = OnboardingStep.entries.getOrNull(current.step.ordinal - 1)
            if (previous == null) current else current.copy(step = previous)
        }
    }

    fun goTo(step: OnboardingStep) {
        _state.update { it.copy(step = step) }
    }

    // The guide is never a gate: skipping is allowed from the first page, and
    // Settings can reopen it later.
    fun finish() {
        store.markCompleted()
        _state.update { it.copy(completed = true) }
    }

    fun reopen() {
        store.reset()
        _state.update { it.copy(completed = false, step = OnboardingStep.first, capabilities = probe.read()) }
    }
}
