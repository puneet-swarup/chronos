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

// Shows a user-visible notification. Low risk: no extra approval required
// after initial setup (see PRD risk tiers).
class NotificationAction : ChronosAction {
 override val type: String = "notification"
 override val displayName: String = "Show notification"
 override val category: ActionCategory = ActionCategory.NOTIFICATION

 override fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult {
 val message = params.string("message")
 return if (message.isNullOrBlank()) {
 ValidationResult.Invalid(listOf("notification requires a non-blank 'message'"))
 } else {
 ValidationResult.Valid
 }
 }

 override suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult {
 val message = params.string("message")
 if (message.isNullOrBlank()) {
 return ActionResult.Failure("INVALID_PARAMS", "message is required")
 }
 return ActionResult.Success(mapOf("message" to message))
 }

 override fun requiredCapabilities(): Set<Capability> =
 setOf(Capability("post_notifications", CapabilityLevel.PERMISSION))

 override fun executionPolicy(): ExecutionPolicy = ExecutionPolicy(timeoutMillis = 5_000)
}
