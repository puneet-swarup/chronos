package com.chronos.app.ui

import com.chronos.app.data.ScheduleEntity
import com.chronos.app.onboarding.DeviceCapabilities
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

 private fun schedule(id: String, enabled: Boolean, nextRun: String? = null) =
 ScheduleEntity(id, "s" + id, "FIXED", null, null, enabled, nextRun)

 @Test
 fun emptySchedulesGiveZeroCounts() {
 val state = HomeUiState.from(emptyList())
 assertEquals(0, state.scheduleCount)
 assertEquals(0, state.enabledCount)
 assertTrue(state.greeting.contains("No schedules"))
 }

 @Test
 fun countsTotalAndEnabled() {
 val state = HomeUiState.from(listOf(schedule("1", true), schedule("2", false)))
 assertEquals(2, state.scheduleCount)
 assertEquals(1, state.enabledCount)
 assertTrue(state.greeting.contains("1 active"))
 }

 @Test
 fun allEnabledCountsMatch() {
 val state = HomeUiState.from(listOf(schedule("1", true), schedule("2", true)))
 assertEquals(2, state.enabledCount)
 }

    @Test
    fun `the soonest future run is labelled for the dashboard`() {
        val now = LocalDateTime.of(2026, 11, 25, 9, 0)
        val state = HomeUiState.from(
            schedules = listOf(
                schedule("1", true, "2026-11-27T08:00"),
                schedule("2", true, "2026-11-25T18:30"),
            ),
            capabilities = null,
            now = now,
        )
        assertEquals("Today 18:30", state.nextRunLabel)
    }

    @Test
    fun `a disabled schedule does not become the next alarm`() {
        val now = LocalDateTime.of(2026, 11, 25, 9, 0)
        val state = HomeUiState.from(
            schedules = listOf(schedule("1", false, "2026-11-25T10:00")),
            capabilities = null,
            now = now,
        )
        assertNull(state.nextRunLabel)
    }

    @Test
    fun `missing permissions are surfaced as consequences`() {
        val state = HomeUiState.from(
            schedules = listOf(schedule("1", true)),
            capabilities = DeviceCapabilities(
                notificationsEnabled = true,
                exactAlarmsAllowed = false,
                batteryOptimisationIgnored = false,
                dndAccessGranted = true,
            ),
        )
        assertEquals(2, state.missingPermissions.size)
        assertTrue(state.missingPermissions.any { it.contains("Exact alarms") })
    }

    @Test
    fun `unknown capabilities produce no warnings`() {
        val state = HomeUiState.from(listOf(schedule("1", true)), capabilities = null)
        assertTrue(state.missingPermissions.isEmpty())
    }

    @Test
    fun `all permissions granted produces no warnings`() {
        val state = HomeUiState.from(
            schedules = listOf(schedule("1", true)),
            capabilities = DeviceCapabilities(
                notificationsEnabled = true,
                exactAlarmsAllowed = true,
                batteryOptimisationIgnored = true,
                dndAccessGranted = true,
            ),
        )
        assertTrue(state.missingPermissions.isEmpty())
    }
}
