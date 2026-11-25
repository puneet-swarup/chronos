package com.chronos.core.workflow

// Static analysis of a workflow's control flow.
//
// Nodes execute in list order, except that a Condition jumps to one of two
// targets. That is enough to build a directed graph, which is enough to answer the
// two questions a validator must answer before anything runs: can this loop
// forever, and is anything in here unreachable?
//
// Pure and dependency-free, so it is unit-tested without Android.
object WorkflowGraph {

    // Node id -> the ids it can transfer control to.
    fun edges(workflow: Workflow): Map<String, List<String>> {
        val known = workflow.nodes.map { it.id }.toSet()
        val result = LinkedHashMap<String, List<String>>(workflow.nodes.size)
        workflow.nodes.forEachIndexed { index, node ->
            val targets = mutableListOf<String>()
            if (node !is WorkflowNode.Stop) {
                workflow.nodes.getOrNull(index + 1)?.let { targets.add(it.id) }
            }
            if (node is WorkflowNode.Condition) {
                node.ifTrueNodeId?.takeIf { it in known }?.let { targets.add(it) }
                node.ifFalseNodeId?.takeIf { it in known }?.let { targets.add(it) }
            }
            result[node.id] = targets.distinct()
        }
        return result
    }

    // Returns the first cycle as a path that starts and ends at the same node, or
    // null when the graph is acyclic. Naming the path matters: "there is a cycle"
    // is not actionable, "cond -> a1 -> cond" is.
    fun firstCycle(workflow: Workflow): List<String>? {
        val graph = edges(workflow)
        val state = HashMap<String, Int>(graph.size) // 1 = on the stack, 2 = finished
        val stack = ArrayDeque<String>()

        fun visit(node: String): List<String>? {
            when (state[node]) {
                1 -> {
                    val from = stack.indexOf(node)
                    val cycle = stack.toList().drop(if (from < 0) 0 else from)
                    return cycle + node
                }

                2 -> return null
            }
            state[node] = 1
            stack.addLast(node)
            for (next in graph[node].orEmpty()) {
                visit(next)?.let { return it }
            }
            stack.removeLast()
            state[node] = 2
            return null
        }

        for (node in workflow.nodes) {
            visit(node.id)?.let { return it }
        }
        return null
    }

    // Nodes that cannot be reached from the first node. Usually dead code after a
    // Stop, which is worth saying out loud rather than silently keeping.
    fun unreachable(workflow: Workflow): List<String> {
        val first = workflow.nodes.firstOrNull() ?: return emptyList()
        val graph = edges(workflow)
        val seen = HashSet<String>()
        val queue = ArrayDeque<String>()
        queue.addLast(first.id)
        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (!seen.add(node)) continue
            for (next in graph[node].orEmpty()) {
                queue.addLast(next)
            }
        }
        return workflow.nodes.map { it.id }.filterNot { it in seen }
    }
}
