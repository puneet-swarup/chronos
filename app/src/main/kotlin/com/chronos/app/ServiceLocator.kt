package com.chronos.app

import android.content.Context
import com.chronos.app.data.ChronosDatabase
import com.chronos.app.data.ScheduleRepository
import com.chronos.app.onboarding.AndroidCapabilityProbe
import com.chronos.app.onboarding.CapabilityProbe
import com.chronos.app.onboarding.OnboardingStore
import com.chronos.app.onboarding.OnboardingViewModel
import com.chronos.app.onboarding.SharedPreferencesOnboardingStore
import com.chronos.app.ui.HomeViewModel
import com.chronos.core.action.ActionRegistry
import com.chronos.core.action.DefaultActions
import com.chronos.core.ai.LocalModelProvider
import com.chronos.core.ai.SimulatedModelProvider

// Manual dependency injection (Hilt's plugin is not yet compatible with
// AGP 9.2.0). Initialised once from ChronosApplication.
object ServiceLocator {

    @Volatile
    private var initialized = false

    lateinit var actionRegistry: ActionRegistry
        private set

    lateinit var repository: ScheduleRepository
        private set

    lateinit var modelProvider: LocalModelProvider
        private set

    lateinit var capabilityProbe: CapabilityProbe
        private set

    lateinit var onboardingStore: OnboardingStore
        private set

    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            val appContext = context.applicationContext
            val db = ChronosDatabase.get(appContext)
            actionRegistry = DefaultActions.registry()
            repository = ScheduleRepository(db.scheduleDao(), db.executionDao(), db.auditDao())
            modelProvider = SimulatedModelProvider()
            capabilityProbe = AndroidCapabilityProbe(appContext)
            onboardingStore = SharedPreferencesOnboardingStore(appContext)
            initialized = true
        }
    }

    fun newHomeViewModel(): HomeViewModel = HomeViewModel(repository)

    fun newOnboardingViewModel(): OnboardingViewModel =
        OnboardingViewModel(capabilityProbe, onboardingStore)
}
