package com.chronos.core.action

import java.time.LocalDateTime

// The single unified action contract (PRD "one unified action contract").
// Every schedulable operation implements this interface and is registered in an
// ActionRegistry. The scheduler never knows about concrete actions.
interface ChronosAction {
 // Stable machine identifier, e.g. "notification", "launch_app".
 val type: String
 // Human-readable name for the UI.
 val displayName: String
 val category: ActionCategory
 // Static (non-executing) validation. Runs before scheduling and again before
 // execution. Must never perform side effects.
 fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult
 // Executes the action. Implementations must be idempotent where possible.
 suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult
 // Capabilities (permissions/special access/integrations) this action needs.
 fun requiredCapabilities(): Set<Capability>
 // Timeout, retry, approval and background-execution policy.
 fun executionPolicy(): ExecutionPolicy
}

data class ActionParameters(val values: Map<String, Any?> = emptyMap()) {
 fun string(key: String): String? = values[key] as? String
 fun int(key: String): Int? = (values[key] as? Number)?.toInt()
 fun bool(key: String): Boolean? = values[key] as? Boolean
}

data class CapabilityContext(
 val grantedCapabilities: Set<Capability> = emptySet(),
 val androidSdkInt: Int = 0,
)

data class ExecutionContext(
 val now: LocalDateTime = LocalDateTime.now(),
 val variables: Map<String, Any?> = emptyMap(),
)

sealed class ValidationResult {
 object Valid : ValidationResult()
 data class Invalid(val errors: List<String>) : ValidationResult()
 val isValid: Boolean get() = this is Valid
}

sealed class ActionResult {
 data class Success(val outputs: Map<String, Any?> = emptyMap()) : ActionResult()
 data class Failure(val errorCode: String, val message: String, val retryable: Boolean = false) : ActionResult()
 data class Skipped(val reason: String) : ActionResult()
}

enum class ActionCategory { ALARM, NOTIFICATION, APP, DEVICE, NETWORK, FILE, SYSTEM, INTEGRATION }

// A capability the device may or may not grant (see PRD capability levels).
data class Capability(
 val id: String,
 val level: CapabilityLevel,
)

enum class CapabilityLevel { NATIVE, PERMISSION, SPECIAL_ACCESS, INTEGRATION, ADVANCED, UNSUPPORTED }

data class ExecutionPolicy(
 val timeoutMillis: Long = 30_000,
 val maxRetries: Int = 0,
 val requiresUserApproval: Boolean = false,
 val canRunInBackground: Boolean = true,
 val requiresNetwork: Boolean = false,
)
