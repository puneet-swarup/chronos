package com.chronos.app.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

// Where releases come from. Implemented against GitHub Releases in
// GitHubReleaseSource, faked in tests.
interface ReleaseSource {
    suspend fun latestReleases(limit: Int): List<ReleaseInfo>
}

// Downloads, verifies and hands the APK to the system installer. Implemented in
// AndroidUpdateInstaller.
interface UpdateInstaller {
    suspend fun install(release: ReleaseInfo): Result<Unit>
}

// Remembers when we last asked, so a cold start does not always hit the network.
interface UpdateStore {
    fun lastCheckMillis(): Long?
    fun recordCheck(nowMillis: Long)
}

// Orchestrates the update flow and owns the state the UI renders. No Android
// types, so the whole state machine is unit-tested with fakes.
class UpdateManager(
    private val source: ReleaseSource,
    private val installer: UpdateInstaller,
    private val store: UpdateStore,
    private val currentVersionCode: Int,
    private val currentVersionName: String,
    private val scope: CoroutineScope,
    private val clock: () -> Long = { System.currentTimeMillis() },
) {
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    // One network round trip at a time; two concurrent checks would fight over
    // the same state.
    private val mutex = Mutex()

    // Called on app start. Respects the throttle unless forced by the user.
    fun checkOnStart() {
        if (!UpdatePolicy.shouldCheck(clock(), store.lastCheckMillis())) return
        checkNow()
    }

    fun checkNow() {
        scope.launch { performCheck() }
    }

    private suspend fun performCheck() = mutex.withLock {
        _state.value = UpdateState.Checking
        try {
            val releases = source.latestReleases(LIMIT)
            store.recordCheck(clock())
            _state.value = when (val decision = UpdatePolicy.decide(currentVersionCode, releases)) {
                is UpdateDecision.UpToDate -> UpdateState.UpToDate(currentVersionName)
                is UpdateDecision.Available -> UpdateState.Available(decision.release)
            }
        } catch (t: Throwable) {
            // Offline, rate limited, DNS blocked: all of it is "could not check",
            // which is not an error the user must act on.
            _state.value = UpdateState.Failed(
                "Could not check for updates: " + (t.message ?: t::class.java.simpleName),
            )
        }
    }

    fun install(release: ReleaseInfo) {
        scope.launch {
            _state.value = UpdateState.Downloading(release)
            _state.value = UpdateState.Installing(release)
            val result = runCatching { installer.install(release) }
                .getOrElse { Result.failure(it) }
            _state.value = result.fold(
                onSuccess = { UpdateState.Installed(release) },
                onFailure = { UpdateState.Failed(it.message ?: "Update failed") },
            )
        }
    }

    // Called by the install-result receiver once the system reports the outcome.
    fun onInstallResult(success: Boolean, message: String? = null) {
        _state.value = if (success) {
            UpdateState.Idle
        } else {
            UpdateState.Failed(message ?: "The system rejected the update")
        }
    }

    companion object {
        const val LIMIT = 5
    }
}
