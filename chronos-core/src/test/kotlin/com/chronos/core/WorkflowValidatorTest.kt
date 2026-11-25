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
import com.chronos.core.workflow.WorkflowGraph
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

    @Test
    fun `a condition that branches back to itself is rejected`() {
        val wf = workflow(
            WorkflowNode.Condition(id = "c1", expression = "battery < 20", ifTrueNodeId = "c1"),
        )
        val outcome = WorkflowValidator(registry()).validate(wf)
        assertFalse(outcome.isValid)
        assertTrue((outcome as com.chronos.core.workflow.ValidationOutcome.Invalid).errors.any { it.contains("loop forever") })
    }

    @Test
    fun `a two node loop names the cycle`() {
        val wf = workflow(
            WorkflowNode.Condition(id = "c1", expression = "x", ifTrueNodeId = "a1"),
            WorkflowNode.Action(id = "a1", actionType = "notification"),
            WorkflowNode.Condition(id = "c2", expression = "y", ifTrueNodeId = "c1"),
        )
        val outcome = WorkflowValidator(registry()).validate(wf)
        assertFalse(outcome.isValid)
        val message = (outcome as com.chronos.core.workflow.ValidationOutcome.Invalid)
            .errors.first { it.contains("loop forever") }
        assertTrue(message.contains("c1"))
        assertTrue(message.contains("->"))
    }

    @Test
    fun `a forward branch is not a cycle`() {
        val wf = workflow(
            WorkflowNode.Condition(id = "c1", expression = "charging", ifTrueNodeId = "a2", ifFalseNodeId = "a1"),
            WorkflowNode.Action(id = "a1", actionType = "notification"),
            WorkflowNode.Action(id = "a2", actionType = "notification"),
        )
        assertTrue(WorkflowValidator(registry()).validate(wf).isValid)
    }

    @Test
    fun `a diamond is not a cycle`() {
        val wf = workflow(
            WorkflowNode.Condition(id = "c1", expression = "x", ifTrueNodeId = "a2", ifFalseNodeId = "a3"),
            WorkflowNode.Action(id = "a1", actionType = "notification"),
            WorkflowNode.Action(id = "a2", actionType = "notification"),
            WorkflowNode.Action(id = "a3", actionType = "notification"),
            WorkflowNode.Stop(id = "end"),
        )
        assertTrue(WorkflowValidator(registry()).validate(wf).isValid)
    }

    @Test
    fun `nodes after a stop are reported as unreachable rather than rejected`() {
        val wf = workflow(
            WorkflowNode.Action(id = "a1", actionType = "notification"),
            WorkflowNode.Stop(id = "end"),
            WorkflowNode.Action(id = "a2", actionType = "notification"),
        )
        val outcome = WorkflowValidator(registry()).validate(wf)
        assertTrue(outcome.isValid)
        val warnings = (outcome as com.chronos.core.workflow.ValidationOutcome.Valid).warnings
        assertTrue(warnings.any { it.contains("a2") })
    }

    @Test
    fun `the graph exposes edges in execution order`() {
        val wf = workflow(
            WorkflowNode.Condition(id = "c1", expression = "x", ifTrueNodeId = "a2"),
            WorkflowNode.Action(id = "a1", actionType = "notification"),
            WorkflowNode.Action(id = "a2", actionType = "notification"),
            WorkflowNode.Stop(id = "end"),
        )
        val edges = WorkflowGraph.edges(wf)
        assertEquals(listOf("a1", "a2"), edges["c1"])
        assertEquals(listOf("end"), edges["a2"])
        assertEquals(emptyList<String>(), edges["end"])
        assertNull(WorkflowGraph.firstCycle(wf))
        assertTrue(WorkflowGraph.unreachable(wf).isEmpty())
    }
}
