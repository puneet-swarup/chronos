package com.chronos.app.update

import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

private class FakeSource(
    var releases: List<ReleaseInfo> = emptyList(),
    var error: Throwable? = null,
) : ReleaseSource {
    var calls = 0
    override suspend fun latestReleases(limit: Int): List<ReleaseInfo> {
        calls++
        error?.let { throw it }
        return releases
    }
}

private class FakeInstaller(
    var result: Result<Unit> = Result.success(Unit),
    var canInstall: Boolean = true,
) : UpdateInstaller {
    var installed: ReleaseInfo? = null
    var openedSettings = false
    override suspend fun install(release: ReleaseInfo): Result<Unit> {
        installed = release
        return result
    }
    override fun canInstallPackages(): Boolean = canInstall
    override fun openInstallPermissionSettings() {
        openedSettings = true
    }
}

private class FakeStore(var last: Long? = null) : UpdateStore {
    override fun lastCheckMillis(): Long? = last
    override fun recordCheck(nowMillis: Long) {
        last = nowMillis
    }
}

class UpdateManagerTest {

    private val release = ReleaseInfo(
        versionCode = 42,
        versionName = "0.1.42",
        downloadUrl = "https://example.invalid/chronos-0.1.42.apk",
    )

    private fun manager(
        source: FakeSource = FakeSource(listOf(release)),
        installer: FakeInstaller = FakeInstaller(),
        store: FakeStore = FakeStore(),
        currentCode: Int = 41,
        clock: () -> Long = { 1_000L },
    ): Triple<UpdateManager, FakeSource, FakeInstaller> {
        val vm = UpdateManager(
            source = source,
            installer = installer,
            store = store,
            currentVersionCode = currentCode,
            currentVersionName = "0.1.41",
            scope = CoroutineScope(Dispatchers.Unconfined),
            clock = clock,
        )
        return Triple(vm, source, installer)
    }

    @Test
    fun `a newer release becomes available`() {
        val (vm, _, _) = manager()
        vm.checkNow()

        val state = vm.state.value
        assertTrue(state is UpdateState.Available)
        assertEquals(42, (state as UpdateState.Available).release.versionCode)
    }

    @Test
    fun `being current reports the running version`() {
        val (vm, _, _) = manager(currentCode = 42)
        vm.checkNow()

        assertEquals(UpdateState.UpToDate("0.1.41"), vm.state.value)
    }

    @Test
    fun `a failed check is reported, not swallowed`() {
        val (vm, _, _) = manager(source = FakeSource(error = IOException("no network")))
        vm.checkNow()

        val state = vm.state.value
        assertTrue(state is UpdateState.Failed)
        assertTrue((state as UpdateState.Failed).message.contains("no network"))
    }

    @Test
    fun `a successful check records the time`() {
        val store = FakeStore()
        val (vm, _, _) = manager(store = store)
        vm.checkNow()

        assertEquals(1_000L, store.last)
    }

    @Test
    fun `a cold start inside the throttle window does not hit the network`() {
        val source = FakeSource(listOf(release))
        val store = FakeStore(last = 900L)
        val (vm, _, _) = manager(source = source, store = store, clock = { 1_000L })

        vm.checkOnStart()

        assertEquals(0, source.calls)
        assertEquals(UpdateState.Idle, vm.state.value)
    }

    @Test
    fun `a cold start outside the throttle window checks`() {
        val source = FakeSource(listOf(release))
        val store = FakeStore(last = 1L)
        val (vm, _, _) = manager(
            source = source,
            store = store,
            clock = { UpdatePolicy.MIN_CHECK_INTERVAL_MILLIS + 2 },
        )

        vm.checkOnStart()

        assertEquals(1, source.calls)
        assertTrue(vm.state.value is UpdateState.Available)
    }

    @Test
    fun `installing hands the release to the installer and reports success`() {
        val (vm, _, installer) = manager()
        vm.install(release)

        assertEquals(release, installer.installed)
        assertEquals(UpdateState.Installed(release), vm.state.value)
    }

    @Test
    fun `a failed install is reported`() {
        val installer = FakeInstaller(result = Result.failure(IOException("checksum mismatch")))
        val (vm, _, _) = manager(installer = installer)
        vm.install(release)

        val state = vm.state.value
        assertTrue(state is UpdateState.Failed)
        assertTrue((state as UpdateState.Failed).message.contains("checksum"))
    }

    @Test
    fun `a rejected install from the system is surfaced`() {
        val (vm, _, _) = manager()
        vm.onInstallResult(success = false, message = "INSTALL_FAILED_UPDATE_INCOMPATIBLE")

        val state = vm.state.value
        assertTrue(state is UpdateState.Failed)
        assertTrue((state as UpdateState.Failed).message.contains("UPDATE_INCOMPATIBLE"))
    }

    @Test
    fun `a successful install returns to idle`() {
        val (vm, _, _) = manager()
        vm.onInstallResult(success = true)
        assertEquals(UpdateState.Idle, vm.state.value)
    }

    @Test
    fun `a missing install permission is its own state, not a generic failure`() {
        val installer = FakeInstaller(result = Result.failure(InstallPermissionRequired()))
        val (vm, _, _) = manager(installer = installer)
        vm.install(release)

        assertEquals(UpdateState.NeedsInstallPermission, vm.state.value)
    }

    @Test
    fun `the install permission screen can be opened from the manager`() {
        val (vm, _, installer) = manager()
        vm.openInstallPermissionSettings()
        assertTrue(installer.openedSettings)
    }
}
