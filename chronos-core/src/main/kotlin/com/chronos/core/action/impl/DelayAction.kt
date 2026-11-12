package com.chronos.core.action.impl

import com.chronos.core.action.ActionCategory
import com.chronos.core.action.ActionParameters
import com.chronos.core.action.ActionResult
import com.chronos.core.action.Capability
import com.chronos.core.action.CapabilityContext
import com.chronos.core.action.ChronosAction
import com.chronos.core.action.ExecutionContext
import com.chronos.core.action.ExecutionPolicy
import com.chronos.core.action.ValidationResult

// Waits for a fixed duration. The core action returns a delay descriptor; the
// Android workflow runner performs the actual (cancellable) wait so it does
// not block the action itself.
class DelayAction : ChronosAction {
 override val type: String = "delay"
 override val displayName: String = "Wait"
 override val category: ActionCategory = ActionCategory.SYSTEM

 override fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult {
 val seconds = params.int("seconds")
 return if (seconds == null || seconds < 0) {
 ValidationResult.Invalid(listOf("delay requires a non-negative integer 'seconds'"))
 } else {
 ValidationResult.Valid
 }
 }

 override suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult {
 val seconds = params.int("seconds")
 if (seconds == null || seconds < 0) {
 return ActionResult.Failure("INVALID_PARAMS", "seconds must be >= 0")
 }
 return ActionResult.Success(mapOf("delaySeconds" to seconds))
 }

 override fun requiredCapabilities(): Set<Capability> = emptySet()

 override fun executionPolicy(): ExecutionPolicy = ExecutionPolicy(timeoutMillis = 1_000)
}
