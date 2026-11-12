package com.chronos.core.action.impl

import com.chronos.core.action.ActionCategory
import com.chronos.core.action.ActionParameters
import com.chronos.core.action.ActionResult
import com.chronos.core.action.Capability
import com.chronos.core.action.CapabilityContext
import com.chronos.core.action.CapabilityLevel
import com.chronos.core.action.ChronosAction
import com.chronos.core.action.ExecutionContext
import com.chronos.core.action.ExecutionPolicy
import com.chronos.core.action.ValidationResult

// Launches an installed application by package name. Medium risk: launching
// other apps is subject to Android background-activity-launch limits, so the
// Android adapter falls back to a notification with a one-tap launch action.
class LaunchAppAction : ChronosAction {
 override val type: String = "launch_app"
 override val displayName: String = "Launch app"
 override val category: ActionCategory = ActionCategory.APP

 override fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult {
 val pkg = params.string("package")
 return if (pkg.isNullOrBlank()) {
 ValidationResult.Invalid(listOf("launch_app requires a non-blank 'package'"))
 } else if (!isValidPackageName(pkg)) {
 ValidationResult.Invalid(listOf("invalid Android package name: " + pkg))
 } else {
 ValidationResult.Valid
 }
 }

 override suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult {
 val pkg = params.string("package")
 if (pkg.isNullOrBlank()) {
 return ActionResult.Failure("INVALID_PARAMS", "package is required")
 }
 if (!isValidPackageName(pkg)) {
 return ActionResult.Failure("INVALID_PACKAGE", "invalid package name")
 }
 return ActionResult.Success(mapOf("package" to pkg))
 }

 override fun requiredCapabilities(): Set<Capability> =
 setOf(Capability("package_visibility", CapabilityLevel.PERMISSION))

 override fun executionPolicy(): ExecutionPolicy =
 ExecutionPolicy(timeoutMillis = 10_000, requiresUserApproval = false)

 private fun isValidPackageName(pkg: String): Boolean {
 if (pkg.isBlank()) return false
 val parts = pkg.split('.')
 if (parts.size < 2) return false
 return parts.all { part -> part.isNotEmpty() && part.all { c -> c.isLetterOrDigit() || c == '_' } }
 }
}
