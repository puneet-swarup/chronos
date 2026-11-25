package com.chronos.app.update

// Thrown when Android has not been told that Chronos may install packages. It is a
// permission the user grants outside the app, so it gets its own state and its own
// button rather than a generic failure.
class InstallPermissionRequired : Exception("Allow Chronos to install unknown apps")

// One published build, as the app sees it on GitHub Releases.
data class ReleaseInfo(
    val versionCode: Int,
    val versionName: String,
    val downloadUrl: String,
    val sha256Url: String? = null,
    val notes: String? = null,
    val publishedAt: String? = null,
)

sealed class UpdateDecision {
    data object UpToDate : UpdateDecision()
    data class Available(val release: ReleaseInfo) : UpdateDecision()
}

// What the updater is doing right now. The UI renders this directly, so every
// failure has a message a user can act on rather than a stack trace.
sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpToDate(val currentVersionName: String) : UpdateState()
    data class Available(val release: ReleaseInfo) : UpdateState()
    data class Downloading(val release: ReleaseInfo) : UpdateState()
    data class Installing(val release: ReleaseInfo) : UpdateState()
    data object NeedsInstallPermission : UpdateState()
    data class Installed(val release: ReleaseInfo) : UpdateState()
    data class Failed(val message: String) : UpdateState()
}

// Pure decisions about releases. Kept free of Android and networking so the
// "is there an update?" question is unit-tested rather than trusted.
object UpdatePolicy {

    // Release tags carry the version code as the last segment: v0.1.42 -> 42.
    // The code, not the name, decides whether an update exists, because that is
    // what Android compares when installing over an existing app.
    fun parseTag(tag: String): Pair<Int, String>? {
        val trimmed = tag.trim().removePrefix("v").removePrefix("V")
        val parts = trimmed.split(".")
        if (parts.size < 2) return null
        val code = parts.last().toIntOrNull() ?: return null
        if (code <= 0) return null
        return code to trimmed
    }

    fun decide(currentVersionCode: Int, releases: List<ReleaseInfo>): UpdateDecision {
        val newest = releases
            .filter { it.versionCode > currentVersionCode }
            .maxByOrNull { it.versionCode }
            ?: return UpdateDecision.UpToDate
        return UpdateDecision.Available(newest)
    }

    // Auto-checks are throttled: the GitHub API is rate limited to 60 requests an
    // hour per address, and a check on every cold start would burn through that.
    fun shouldCheck(nowMillis: Long, lastCheckMillis: Long?, minIntervalMillis: Long = MIN_CHECK_INTERVAL_MILLIS): Boolean {
        if (lastCheckMillis == null) return true
        if (nowMillis < lastCheckMillis) return true // clock moved backwards
        return nowMillis - lastCheckMillis >= minIntervalMillis
    }

    const val MIN_CHECK_INTERVAL_MILLIS: Long = 6L * 60L * 60L * 1000L
}
