package com.chronos.app

import android.content.Context
import com.chronos.app.data.ChronosDatabase
import com.chronos.app.data.ScheduleRepository
import com.chronos.core.action.ActionRegistry
import com.chronos.core.action.DefaultActions
import com.chronos.core.ai.LocalModelProvider
import com.chronos.core.ai.SimulatedModelProvider

// Manual dependency injection. Hilt's Gradle plugin (2.52) does not support
// AGP 9.2.0 yet, so a small, explicit ServiceLocator is used instead. It is
// initialised once from ChronosApplication and provides the app-wide graph.
object ServiceLocator {

 @Volatile
 private var initialized = false

 lateinit var actionRegistry: ActionRegistry
 private set

 lateinit var repository: ScheduleRepository
 private set

 lateinit var modelProvider: LocalModelProvider
 private set

 fun init(context: Context) {
 if (initialized) return
 synchronized(this) {
 if (initialized) return
 val db = ChronosDatabase.get(context)
 actionRegistry = DefaultActions.registry()
 repository = ScheduleRepository(db.scheduleDao(), db.executionDao(), db.auditDao())
 modelProvider = SimulatedModelProvider()
 initialized = true
 }
 }
}
