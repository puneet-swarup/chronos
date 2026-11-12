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

// Opens a URL / deep link. Medium risk: may launch another app.
class OpenUrlAction : ChronosAction {
 override val type: String = "open_url"
 override val displayName: String = "Open URL"
 override val category: ActionCategory = ActionCategory.APP

 override fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult {
 val url = params.string("url")
 val errors = mutableListOf<String>()
 if (url.isNullOrBlank()) {
 errors.add("open_url requires a non-blank 'url'")
 } else if (!url.startsWith("http://") && !url.startsWith("https://")) {
 errors.add("url must start with http:// or https://")
 }
 return if (errors.isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(errors)
 }

 override suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult {
 val url = params.string("url")
 if (url.isNullOrBlank()) {
 return ActionResult.Failure("INVALID_PARAMS", "url is required")
 }
 if (!url.startsWith("http://") && !url.startsWith("https://")) {
 return ActionResult.Failure("INVALID_URL", "url must be http(s)")
 }
 return ActionResult.Success(mapOf("url" to url))
 }

 override fun requiredCapabilities(): Set<Capability> =
 setOf(Capability("internet", CapabilityLevel.PERMISSION))

 override fun executionPolicy(): ExecutionPolicy =
 ExecutionPolicy(timeoutMillis = 10_000, requiresNetwork = true)
}
