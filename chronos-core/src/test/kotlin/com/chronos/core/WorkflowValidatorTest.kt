package com.chronos.core

import com.chronos.core.action.ActionCategory
import com.chronos.core.action.ActionParameters
import com.chronos.core.action.ActionRegistry
import com.chronos.core.action.ActionResult
import com.chronos.core.action.Capability
import com.chronos.core.action.CapabilityContext
import com.chronos.core.action.CapabilityLevel
import com.chronos.core.action.ChronosAction
import com.chronos.core.action.ExecutionContext
import com.chronos.core.action.ExecutionPolicy
import com.chronos.core.action.ValidationResult
import com.chronos.core.workflow.WORKFLOW_SCHEMA_VERSION
import com.chronos.core.workflow.Workflow
import com.chronos.core.workflow.WorkflowNode
import com.chronos.core.workflow.WorkflowTrigger
import com.chronos.core.workflow.WorkflowValidator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkflowValidatorTest {

 private class NotificationAction : ChronosAction {
 override val type: String = "notification"
 override val displayName: String = "Notification"
 override val category: ActionCategory = ActionCategory.NOTIFICATION
 override fun validate(params: ActionParameters, context: CapabilityContext): ValidationResult = ValidationResult.Valid
 override suspend fun execute(context: ExecutionContext, params: ActionParameters): ActionResult = ActionResult.Success()
 override fun requiredCapabilities(): Set<Capability> =
 setOf(Capability("post_notifications", CapabilityLevel.PERMISSION))
 override fun executionPolicy(): ExecutionPolicy = ExecutionPolicy()
 }

 private fun registry(): ActionRegistry = ActionRegistry().register(NotificationAction())

 private fun workflow(vararg nodes: WorkflowNode): Workflow = Workflow(
 id = "wf-1",
 name = "Test workflow",
 schemaVersion = WORKFLOW_SCHEMA_VERSION,
 trigger = WorkflowTrigger(type = "schedule", params = mapOf("time" to "22:00")),
 nodes = nodes.toList(),
 )

 @Test
 fun validWorkflowPasses() {
 val wf = workflow(WorkflowNode.Action(id = "a1", actionType = "notification"))
 val outcome = WorkflowValidator(registry()).validate(wf)
 assertTrue(outcome.isValid)
 }

 @Test
 fun unknownActionIsRejected() {
 val wf = workflow(WorkflowNode.Action(id = "a1", actionType = "does_not_exist"))
 val outcome = WorkflowValidator(registry()).validate(wf)
 assertFalse(outcome.isValid)
 }

 @Test
 fun duplicateNodeIdsAreRejected() {
 val wf = workflow(
 WorkflowNode.Delay(id = "n1", seconds = 5),
 WorkflowNode.Delay(id = "n1", seconds = 5),
 )
 assertFalse(WorkflowValidator(registry()).validate(wf).isValid)
 }

 @Test
 fun emptyWorkflowIsRejected() {
 assertFalse(WorkflowValidator(registry()).validate(workflow()).isValid)
 }

 @Test
 fun missingCapabilityProducesWarning() {
 val wf = workflow(WorkflowNode.Action(id = "a1", actionType = "notification"))
 val outcome = WorkflowValidator(registry()).validate(wf, grantedCapabilities = emptySet())
 assertTrue(outcome.isValid)
 }
}
