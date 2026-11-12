package com.chronos.core.ai

import com.chronos.core.workflow.WORKFLOW_SCHEMA_VERSION
import com.chronos.core.workflow.Workflow
import com.chronos.core.workflow.WorkflowNode
import com.chronos.core.workflow.WorkflowTrigger

// Converts a ParsedIntent into a draft Workflow. The WorkflowValidator must
// still validate the result; AI/parser output is never executed directly.
object IntentCompiler {

 fun compile(intent: ParsedIntent, id: String): Workflow? {
 if (intent.kind == IntentKind.UNKNOWN) return null
 val nodes = mutableListOf<WorkflowNode>()
 val triggerParams = mutableMapOf<String, String>()
 intent.time?.let { triggerParams["time"] = it.toString() }

 when (intent.kind) {
 IntentKind.CREATE_ALARM -> nodes.add(
 WorkflowNode.Action(
 id = "a1",
 actionType = "notification",
 params = mapOf("message" to intent.message),
 ),
 )
 IntentKind.CREATE_REMINDER -> nodes.add(
 WorkflowNode.Action(
 id = "a1",
 actionType = "notification",
 params = mapOf("message" to intent.message),
 ),
 )
 IntentKind.OPEN_URL -> {
 val url = intent.url
 if (url == null) return null
 nodes.add(
 WorkflowNode.Action(
 id = "a1",
 actionType = "open_url",
 params = mapOf("url" to url),
 ),
 )
 }
 IntentKind.NOTIFY -> nodes.add(
 WorkflowNode.Action(id = "a1", actionType = "notification", params = mapOf("message" to intent.message)),
 )
 IntentKind.UNKNOWN -> return null
 }

 return Workflow(
 id = id,
 name = intent.message.ifBlank { intent.kind.name },
 schemaVersion = WORKFLOW_SCHEMA_VERSION,
 trigger = WorkflowTrigger(type = "schedule", params = triggerParams),
 nodes = nodes,
 )
 }
}
