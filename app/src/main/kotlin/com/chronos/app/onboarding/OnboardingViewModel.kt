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

    // Called when the screen resumes - i.e. when the user comes back from a
    // system dialog or settings screen.
    //
    // If the permission this step asked for has just been granted, the guide moves
    // on by itself. Making the user find "Next" after they have already answered
    // the question is the kind of friction that makes an app feel unresponsive.
    fun refresh() {
        val before = _state.value
        val after = probe.read()
        val grantedNow = before.currentStepGranted == false && after.isGranted(before.step.action) == true
        val advance = grantedNow && !before.isLastStep
        _state.update { current ->
            current.copy(
                capabilities = after,
                step = if (advance) {
                    OnboardingStep.entries[current.step.ordinal + 1]
                } else {
                    current.step
                },
                message = if (grantedNow) null else current.message,
            )
        }
    }

    // A step whose system screen does not exist on this device. Reported rather
    // than swallowed, so the button never looks broken.
    fun reportActionUnavailable(reason: String) {
        _state.update { it.copy(message = reason) }
    }

    fun clearMessage() {
        _state.update { it.copy(message = null) }
    }

    fun next() {
        _state.update { current ->
            val following = OnboardingStep.entries.getOrNull(current.step.ordinal + 1)
            if (following == null) current else current.copy(step = following, message = null)
        }
    }

    fun back() {
        _state.update { current ->
            val previous = OnboardingStep.entries.getOrNull(current.step.ordinal - 1)
            if (previous == null) current else current.copy(step = previous, message = null)
        }
    }

    fun goTo(step: OnboardingStep) {
        _state.update { it.copy(step = step, message = null) }
    }

    // The guide is never a gate: skipping is allowed from the first page, and
    // Settings can reopen it later.
    fun finish() {
        store.markCompleted()
        _state.update { it.copy(completed = true) }
    }

    fun reopen() {
        store.reset()
        _state.update {
            it.copy(
                completed = false,
                step = OnboardingStep.first,
                capabilities = probe.read(),
                message = null,
            )
        }
    }
}
