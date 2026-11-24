package com.chronos.app.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import com.chronos.app.ServiceLocator
import kotlinx.coroutines.launch

// The system reports the outcome of an install session here. A rejected install
// must not look like a successful one, so the result is recorded and surfaced.
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_RESULT) return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val success = status == PackageInstaller.STATUS_SUCCESS

        Log.i(TAG, "install result status=" + status + " message=" + message)
        ServiceLocator.updateManager.onInstallResult(success, message)
        ServiceLocator.appScope.launch {
            ServiceLocator.repository.recordAudit(
                "updater",
                if (success) "update installed" else "update rejected: " + (message ?: status.toString()),
            )
        }
    }

    companion object {
        private const val TAG = "ChronosInstallResult"
        const val ACTION_INSTALL_RESULT = "com.chronos.app.action.INSTALL_RESULT"
    }
}
