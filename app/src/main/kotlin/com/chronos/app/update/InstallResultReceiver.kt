package com.chronos.app.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.chronos.app.ServiceLocator
import kotlinx.coroutines.launch

// The system reports the outcome of an install session here.
//
// The important case is STATUS_PENDING_USER_ACTION: a normal app cannot install
// silently, so Android hands back an Intent that shows its own confirmation
// dialog. If that intent is not launched, the install simply never happens - which
// is exactly what "handed over to Android and nothing occurred" looks like.
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_INSTALL_RESULT) return
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmation = confirmationIntent(intent)
                if (confirmation == null) {
                    Log.e(TAG, "system asked for confirmation but supplied no intent")
                    report(context, false, "Android asked for confirmation but supplied no dialog")
                    return
                }
                // The dialog belongs to the system; Chronos only opens the door.
                runCatching {
                    confirmation.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmation)
                }.onFailure {
                    Log.e(TAG, "could not open the installer dialog", it)
                    report(context, false, "Could not open the Android installer: " + it.message)
                }
            }

            PackageInstaller.STATUS_SUCCESS -> report(context, true, null)

            else -> report(context, false, message ?: "Android rejected the update (status " + status + ")")
        }
    }

    // The confirmation dialog arrives as Intent.EXTRA_INTENT ("android.intent.extra.INTENT"),
    // not a PackageInstaller constant.
    private fun confirmationIntent(intent: Intent): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Intent.EXTRA_INTENT)
        }

    private fun report(context: Context, success: Boolean, message: String?) {
        Log.i(TAG, "install result success=" + success + " message=" + message)
        ServiceLocator.updateManager.onInstallResult(success, message)
        val audit = if (success) "update installed" else "update rejected: " + (message ?: "unknown")
        ServiceLocator.appScope.launch {
            ServiceLocator.repository.recordAudit("updater", audit)
        }
    }

    companion object {
        private const val TAG = "ChronosInstallResult"
        const val ACTION_INSTALL_RESULT = "com.chronos.app.action.INSTALL_RESULT"
    }
}
