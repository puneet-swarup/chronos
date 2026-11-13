package com.chronos.app.ui

import com.chronos.app.data.ScheduleEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeViewModelTest {

 private fun schedule(id: String, enabled: Boolean) =
 ScheduleEntity(id, "s" + id, "FIXED", null, null, enabled, null)

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
}
