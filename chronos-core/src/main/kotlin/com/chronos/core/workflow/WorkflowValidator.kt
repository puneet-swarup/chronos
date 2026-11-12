package com.chronos.core.workflow

import com.chronos.core.action.ActionRegistry
import com.chronos.core.action.Capability

// Deterministic validator. Produces structured errors instead of throwing, so
// the UI can always show what is wrong with an AI-generated proposal.
class WorkflowValidator(private val registry: ActionRegistry) {

 fun validate(workflow: Workflow, grantedCapabilities: Set<Capability> = emptySet()): ValidationOutcome {
 val errors = mutableListOf<String>()
 val warnings = mutableListOf<String>()

 if (workflow.id.isBlank()) errors.add("Workflow id is blank")
 if (workflow.name.isBlank()) errors.add("Workflow name is blank")
 if (workflow.schemaVersion != WORKFLOW_SCHEMA_VERSION) {
 errors.add("Unsupported schema version " + workflow.schemaVersion)
 }
 if (workflow.nodes.isEmpty()) errors.add("Workflow has no nodes")

 val ids = mutableSetOf<String>()
 for (node in workflow.nodes) {
 if (!ids.add(node.id)) errors.add("Duplicate node id '" + node.id + "'")
 }

 for (node in workflow.nodes) {
 when (node) {
 is WorkflowNode.Action -> {
 if (!registry.contains(node.actionType)) {
 errors.add("Unknown action type '" + node.actionType + "' in node '" + node.id + "'")
 } else {
 val missing = registry.missingCapabilities(node.actionType, grantedCapabilities)
 if (missing.isNotEmpty()) {
 warnings.add("Node '" + node.id + "' needs capabilities: " + missing.map { it.id })
 }
 }
 }
 is WorkflowNode.Delay -> if (node.seconds < 0) {
 errors.add("Negative delay in node '" + node.id + "'")
 }
 is WorkflowNode.Approval -> if (node.message.isBlank()) {
 errors.add("Empty approval message in node '" + node.id + "'")
 }
 is WorkflowNode.Condition -> {
 if (node.expression.isBlank()) errors.add("Empty condition in node '" + node.id + "'")
 node.ifTrueNodeId?.let { if (it !in ids) errors.add("Unknown ifTrueNodeId '" + it + "'") }
 node.ifFalseNodeId?.let { if (it !in ids) errors.add("Unknown ifFalseNodeId '" + it + "'") }
 }
 is WorkflowNode.Stop -> { }
 }
 }

 if (workflow.limits.maxDurationSeconds <= 0) errors.add("maxDurationSeconds must be positive")
 if (workflow.limits.maxLoopCount <= 0) errors.add("maxLoopCount must be positive")

 return if (errors.isEmpty()) {
 ValidationOutcome.Valid(warnings)
 } else {
 ValidationOutcome.Invalid(errors, warnings)
 }
 }
}

sealed class ValidationOutcome {
 data class Valid(val warnings: List<String> = emptyList()) : ValidationOutcome()
 data class Invalid(val errors: List<String>, val warnings: List<String> = emptyList()) : ValidationOutcome()
 val isValid: Boolean get() = this is Valid
}
