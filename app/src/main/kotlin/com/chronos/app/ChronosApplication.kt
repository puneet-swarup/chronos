package com.chronos.app

import android.app.Application

// Application entry point. Initialises the manual dependency graph once.
class ChronosApplication : Application() {
 override fun onCreate() {
 super.onCreate()
 ServiceLocator.init(this)
 }
}
