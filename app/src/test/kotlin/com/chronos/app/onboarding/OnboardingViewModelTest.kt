package com.chronos.app.onboarding

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeProbe(var capabilities: DeviceCapabilities) : CapabilityProbe {
    override fun read(): DeviceCapabilities = capabilities
}

private class FakeStore(var completed: Boolean = false) : OnboardingStore {
    override fun isCompleted(): Boolean = completed
    override fun markCompleted() {
        completed = true
    }
    override fun reset() {
        completed = false
    }
}

class OnboardingViewModelTest {

    private fun viewModel(
        capabilities: DeviceCapabilities = DeviceCapabilities(),
        completed: Boolean = false,
    ): Triple<OnboardingViewModel, FakeProbe, FakeStore> {
        val probe = FakeProbe(capabilities)
        val store = FakeStore(completed)
        return Triple(OnboardingViewModel(probe, store), probe, store)
    }

    @Test
    fun `starts on the welcome step and is not completed on a first run`() {
        val (vm, _, _) = viewModel()
        val state = vm.state.value
        assertEquals(OnboardingStep.WELCOME, state.step)
        assertFalse(state.completed)
        assertTrue(state.isFirstStep)
        assertEquals(1, state.stepNumber)
    }

    @Test
    fun `step order walks the whole guide`() {
        val (vm, _, _) = viewModel()
        val seen = mutableListOf(vm.state.value.step)
        repeat(OnboardingStep.entries.size) {
            vm.next()
            seen.add(vm.state.value.step)
        }
        assertEquals(OnboardingStep.entries.toList(), seen.distinct())
        assertTrue(vm.state.value.isLastStep)
    }

    @Test
    fun `next and back stop at the ends instead of wrapping`() {
        val (vm, _, _) = viewModel()
        vm.back()
        assertEquals(OnboardingStep.first, vm.state.value.step)

        repeat(OnboardingStep.entries.size + 5) { vm.next() }
        assertEquals(OnboardingStep.last, vm.state.value.step)
    }

    @Test
    fun `finish records completion so the guide does not open again`() {
        val (vm, _, store) = viewModel()
        vm.finish()
        assertTrue(store.completed)
        assertTrue(vm.state.value.completed)
    }

    @Test
    fun `a completed store means the app opens directly next launch`() {
        val (vm, _, _) = viewModel(completed = true)
        assertTrue(vm.state.value.completed)
    }

    @Test
    fun `refresh picks up permissions granted while the guide was open`() {
        val (vm, probe, _) = viewModel()
        assertEquals(false, vm.state.value.capabilities.notificationsEnabled)

        probe.capabilities = DeviceCapabilities(notificationsEnabled = true, exactAlarmsAllowed = true)
        vm.refresh()

        assertTrue(vm.state.value.capabilities.notificationsEnabled)
        assertTrue(vm.state.value.capabilities.exactAlarmsAllowed)
        assertEquals(2, vm.state.value.capabilities.grantedCount)
    }

    @Test
    fun `each step reports its own permission state`() {
        val capabilities = DeviceCapabilities(
            notificationsEnabled = true,
            exactAlarmsAllowed = false,
            batteryOptimisationIgnored = true,
            dndAccessGranted = false,
        )
        assertEquals(true, capabilities.isGranted(OnboardingAction.REQUEST_NOTIFICATIONS))
        assertEquals(false, capabilities.isGranted(OnboardingAction.OPEN_EXACT_ALARM_SETTINGS))
        assertEquals(true, capabilities.isGranted(OnboardingAction.OPEN_BATTERY_SETTINGS))
        assertEquals(false, capabilities.isGranted(OnboardingAction.OPEN_DND_SETTINGS))
        assertNull(capabilities.isGranted(OnboardingAction.NONE))
        assertEquals(2, capabilities.grantedCount)
        assertEquals(4, capabilities.totalCount)
    }

    @Test
    fun `informational steps ask for nothing`() {
        val informational = OnboardingStep.entries.filter { it.isInformational }
        assertEquals(
            listOf(OnboardingStep.WELCOME, OnboardingStep.TOUR),
            informational,
        )
        assertNull(viewModel().first.state.value.capabilities.isGranted(OnboardingStep.WELCOME.action))
    }

    @Test
    fun `reopen resets the store and returns to the first step`() {
        val (vm, _, store) = viewModel(completed = true)
        vm.next()
        vm.reopen()
        assertFalse(store.completed)
        assertFalse(vm.state.value.completed)
        assertEquals(OnboardingStep.WELCOME, vm.state.value.step)
    }

    @Test
    fun `granting the current permission advances without a tap`() {
        val (vm, probe, _) = viewModel()
        vm.next()
        assertEquals(OnboardingStep.NOTIFICATIONS, vm.state.value.step)

        probe.capabilities = DeviceCapabilities(notificationsEnabled = true)
        vm.refresh()

        assertEquals(OnboardingStep.EXACT_ALARMS, vm.state.value.step)
    }

    @Test
    fun `a refresh with nothing granted stays on the step`() {
        val (vm, _, _) = viewModel()
        vm.next()
        vm.refresh()
        assertEquals(OnboardingStep.NOTIFICATIONS, vm.state.value.step)
    }

    @Test
    fun `revoking a permission never advances`() {
        val (vm, probe, _) = viewModel(DeviceCapabilities(notificationsEnabled = true))
        vm.next()

        probe.capabilities = DeviceCapabilities(notificationsEnabled = false)
        vm.refresh()

        assertEquals(OnboardingStep.NOTIFICATIONS, vm.state.value.step)
    }

    @Test
    fun `auto-advance stops at the last step`() {
        val (vm, probe, _) = viewModel()
        vm.goTo(OnboardingStep.TOUR)
        probe.capabilities = DeviceCapabilities(notificationsEnabled = true)
        vm.refresh()
        assertEquals(OnboardingStep.TOUR, vm.state.value.step)
    }

    @Test
    fun `an unavailable system screen is reported instead of swallowed`() {
        val (vm, _, _) = viewModel()
        vm.reportActionUnavailable("no screen on this device")
        assertEquals("no screen on this device", vm.state.value.message)

        vm.clearMessage()
        assertNull(vm.state.value.message)
    }

    @Test
    fun `moving between steps clears a stale message`() {
        val (vm, _, _) = viewModel()
        vm.reportActionUnavailable("no screen on this device")
        vm.next()
        assertNull(vm.state.value.message)
    }
}
