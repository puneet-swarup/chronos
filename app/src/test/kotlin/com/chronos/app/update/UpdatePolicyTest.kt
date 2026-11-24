package com.chronos.app.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePolicyTest {

    private fun release(code: Int, name: String = "0.1." + code) = ReleaseInfo(
        versionCode = code,
        versionName = name,
        downloadUrl = "https://example.invalid/chronos-" + name + ".apk",
    )

    @Test
    fun `tag carries the version code in its last segment`() {
        assertEquals(42 to "0.1.42", UpdatePolicy.parseTag("v0.1.42"))
        assertEquals(42 to "0.1.42", UpdatePolicy.parseTag("0.1.42"))
        assertEquals(1 to "0.1", UpdatePolicy.parseTag("v0.1"))
    }

    @Test
    fun `a tag without a numeric last segment is not a release`() {
        assertNull(UpdatePolicy.parseTag("latest"))
        assertNull(UpdatePolicy.parseTag("v0.1.x"))
        assertNull(UpdatePolicy.parseTag("v"))
        assertNull(UpdatePolicy.parseTag("v0.0"))
    }

    @Test
    fun `a newer release is offered`() {
        val decision = UpdatePolicy.decide(currentVersionCode = 41, releases = listOf(release(42)))
        assertTrue(decision is UpdateDecision.Available)
        assertEquals(42, (decision as UpdateDecision.Available).release.versionCode)
    }

    @Test
    fun `the newest of several releases wins`() {
        val decision = UpdatePolicy.decide(
            currentVersionCode = 40,
            releases = listOf(release(41), release(45), release(43)),
        )
        assertEquals(45, (decision as UpdateDecision.Available).release.versionCode)
    }

    @Test
    fun `the same version is not an update`() {
        val decision = UpdatePolicy.decide(currentVersionCode = 42, releases = listOf(release(42)))
        assertEquals(UpdateDecision.UpToDate, decision)
    }

    @Test
    fun `older releases are ignored`() {
        val decision = UpdatePolicy.decide(currentVersionCode = 50, releases = listOf(release(42), release(49)))
        assertEquals(UpdateDecision.UpToDate, decision)
    }

    @Test
    fun `no releases means up to date`() {
        assertEquals(UpdateDecision.UpToDate, UpdatePolicy.decide(1, emptyList()))
    }

    @Test
    fun `the first check always runs`() {
        assertTrue(UpdatePolicy.shouldCheck(nowMillis = 1_000L, lastCheckMillis = null))
    }

    @Test
    fun `a check inside the throttle window is skipped`() {
        val now = 10_000_000L
        assertFalse(UpdatePolicy.shouldCheck(now, now - 60_000L))
    }

    @Test
    fun `a check after the throttle window runs`() {
        val now = 10_000_000L
        assertTrue(UpdatePolicy.shouldCheck(now, now - UpdatePolicy.MIN_CHECK_INTERVAL_MILLIS - 1))
    }

    @Test
    fun `a clock that moved backwards does not block updates forever`() {
        val now = 1_000L
        assertTrue(UpdatePolicy.shouldCheck(now, lastCheckMillis = 9_999_999L))
    }
}
