package com.chronos.core.workflow

// Versioned workflow model. The AI (or the UI) produces a proposal; the
// WorkflowCompiler validates it into a Workflow. AI never executes directly.
const val WORKFLOW_SCHEMA_VERSION = 1

data class Workflow(
 val id: String,
 val name: String,
 val schemaVersion: Int = WORKFLOW_SCHEMA_VERSION,
 val trigger: WorkflowTrigger,
 val nodes: List<WorkflowNode>,
 val conditions: ConditionGroup? = null,
 val limits: WorkflowLimits = WorkflowLimits(),
)

data class WorkflowTrigger(
 // e.g. "schedule", "battery", "location", "app_event", "webhook"
 val type: String,
 val params: Map<String, String> = emptyMap(),
)

// Sealed hierarchy of node types from the PRD (Module 6).
sealed class WorkflowNode {
 abstract val id: String

 data class Action(
 override val id: String,
 val actionType: String,
 val params: Map<String, Any?> = emptyMap(),
 ) : WorkflowNode()

 data class Condition(
 override val id: String,
 val expression: String,
 val ifTrueNodeId: String? = null,
 val ifFalseNodeId: String? = null,
 ) : WorkflowNode()

 data class Delay(
 override val id: String,
 val seconds: Long,
 ) : WorkflowNode()

 data class Approval(
 override val id: String,
 val message: String,
 ) : WorkflowNode()

 data class Stop(
 override val id: String,
 val reason: String = "",
 ) : WorkflowNode()
}

data class ConditionGroup(
 val operator: Operator = Operator.AND,
 val rules: List<ConditionRule> = emptyList(),
) {
 enum class Operator { AND, OR }
}

data class ConditionRule(
 // e.g. "device_charging", "wifi_connected", "battery_below"
 val type: String,
 val value: String,
)

data class WorkflowLimits(
 val maxDurationSeconds: Long = 3600,
 val maxLoopCount: Int = 100,
 val allowRebootRecovery: Boolean = true,
)
