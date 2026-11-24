package com.chronos.app.update

import android.content.Context

class SharedPreferencesUpdateStore(context: Context) : UpdateStore {

    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    override fun lastCheckMillis(): Long? =
        if (prefs.contains(KEY_LAST_CHECK)) prefs.getLong(KEY_LAST_CHECK, 0L) else null

    override fun recordCheck(nowMillis: Long) {
        prefs.edit().putLong(KEY_LAST_CHECK, nowMillis).apply()
    }

    private companion object {
        const val PREFS = "chronos.updates"
        const val KEY_LAST_CHECK = "lastCheck"
    }
}
