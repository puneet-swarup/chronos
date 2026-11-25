package com.chronos.app

import android.content.Context
import com.chronos.app.data.ChronosDatabase
import com.chronos.app.data.ScheduleRepository
import com.chronos.app.notifications.ExecutionNotifier
import com.chronos.app.onboarding.AndroidCapabilityProbe
import com.chronos.app.onboarding.CapabilityProbe
import com.chronos.app.onboarding.OnboardingStore
import com.chronos.app.onboarding.OnboardingViewModel
import com.chronos.app.onboarding.SharedPreferencesOnboardingStore
import com.chronos.app.scheduling.AlarmScheduler
import com.chronos.app.scheduling.ScheduleExecutor
import com.chronos.app.scheduling.ScheduleReconciler
import com.chronos.app.scheduling.WorkScheduler
import com.chronos.app.ui.ActivityViewModel
import com.chronos.app.ui.schedule.ScheduleViewModel
import com.chronos.app.ui.HomeViewModel
import com.chronos.app.update.AndroidUpdateInstaller
import com.chronos.app.update.GitHubReleaseSource
import com.chronos.app.update.SharedPreferencesUpdateStore
import com.chronos.app.update.UpdateManager
import com.chronos.core.action.ActionRegistry
import com.chronos.core.action.DefaultActions
import com.chronos.core.ai.LocalModelProvider
import com.chronos.core.ai.SimulatedModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

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

    lateinit var alarmScheduler: AlarmScheduler
        private set

    lateinit var workScheduler: WorkScheduler
        private set

    lateinit var scheduleReconciler: ScheduleReconciler
        private set

    lateinit var scheduleExecutor: ScheduleExecutor
        private set

    lateinit var updateManager: UpdateManager
        private set

    // Broadcast receivers and the alarm path outlive any screen, so their work
    // runs here rather than in a ViewModel scope.
    lateinit var appScope: CoroutineScope
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
            alarmScheduler = AlarmScheduler(appContext)
            workScheduler = WorkScheduler(appContext)
            scheduleReconciler = ScheduleReconciler(repository, alarmScheduler, workScheduler)
            scheduleExecutor = ScheduleExecutor(
                repository = repository,
                notifier = ExecutionNotifier(appContext),
                reconciler = scheduleReconciler,
            )
            appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            updateManager = UpdateManager(
                source = GitHubReleaseSource(),
                installer = AndroidUpdateInstaller(appContext),
                store = SharedPreferencesUpdateStore(appContext),
                currentVersionCode = BuildConfig.VERSION_CODE,
                currentVersionName = BuildConfig.VERSION_NAME,
                scope = appScope,
            )
            initialized = true
        }
    }

    fun newHomeViewModel(): HomeViewModel = HomeViewModel(repository, capabilityProbe)

    fun newOnboardingViewModel(): OnboardingViewModel =
        OnboardingViewModel(capabilityProbe, onboardingStore)

    fun newActivityViewModel(): ActivityViewModel = ActivityViewModel(repository)

    fun newScheduleViewModel(): ScheduleViewModel =
        ScheduleViewModel(repository, scheduleReconciler)
}
