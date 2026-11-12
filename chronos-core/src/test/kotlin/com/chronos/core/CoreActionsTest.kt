package com.chronos.core

import com.chronos.core.action.ActionParameters
import com.chronos.core.action.ActionResult
import com.chronos.core.action.Capability
import com.chronos.core.action.CapabilityContext
import com.chronos.core.action.CapabilityLevel
import com.chronos.core.action.DefaultActions
import com.chronos.core.action.ExecutionContext
import com.chronos.core.action.impl.DelayAction
import com.chronos.core.action.impl.LaunchAppAction
import com.chronos.core.action.impl.NotificationAction
import com.chronos.core.action.impl.OpenUrlAction
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CoreActionsTest {

 private val ctx = CapabilityContext()

 @Test
 fun notificationValidateRejectsBlankMessage() {
 val r = NotificationAction().validate(ActionParameters(mapOf("message" to " ")), ctx)
 assertFalse(r.isValid)
 }

 @Test
 fun notificationValidateAcceptsMessage() {
 val r = NotificationAction().validate(ActionParameters(mapOf("message" to "hi")), ctx)
 assertTrue(r.isValid)
 }

 @Test
 fun openUrlRejectsNonHttp() {
 val a = OpenUrlAction()
 assertFalse(a.validate(ActionParameters(mapOf("url" to "ftp://x")), ctx).isValid)
 assertTrue(a.validate(ActionParameters(mapOf("url" to "https://example.com")), ctx).isValid)
 }

 @Test
 fun launchAppValidatesPackageName() {
 val a = LaunchAppAction()
 assertFalse(a.validate(ActionParameters(mapOf("package" to "nodot")), ctx).isValid)
 assertTrue(a.validate(ActionParameters(mapOf("package" to "com.example.app")), ctx).isValid)
 }

 @Test
 fun delayRejectsNegative() {
 val a = DelayAction()
 assertFalse(a.validate(ActionParameters(mapOf("seconds" to -1)), ctx).isValid)
 assertTrue(a.validate(ActionParameters(mapOf("seconds" to 5)), ctx).isValid)
 }

 @Test
 fun executeNotificationReturnsSuccess() = runBlocking {
 val result = NotificationAction().execute(ExecutionContext(), ActionParameters(mapOf("message" to "hello")))
 assertTrue(result is ActionResult.Success)
 }

 @Test
 fun defaultRegistryHasAllBuiltInActions() {
 val reg = DefaultActions.registry()
 assertTrue(reg.contains("notification"))
 assertTrue(reg.contains("open_url"))
 assertTrue(reg.contains("launch_app"))
 assertTrue(reg.contains("delay"))
 assertEquals(4, reg.all().size)
 }

 @Test
 fun registryReportsMissingCapabilities() {
 val reg = DefaultActions.registry()
 assertEquals(1, reg.missingCapabilities("notification", emptySet()).size)
 val granted = setOf(Capability("post_notifications", CapabilityLevel.PERMISSION))
 assertTrue(reg.missingCapabilities("notification", granted).isEmpty())
 }
}
