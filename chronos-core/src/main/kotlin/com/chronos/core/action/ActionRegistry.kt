package com.chronos.core.action

// Central registry of every action Chronos supports.
// The scheduler and workflow engine only ever look actions up by type; they
// never reference concrete implementations. New capabilities are added by
// registering a new ChronosAction (PRD "extensible action system").
class ActionRegistry(private val actions: MutableMap<String, ChronosAction> = mutableMapOf()) {

 fun register(action: ChronosAction): ActionRegistry {
 require(action.type.isNotBlank()) { "Action type must not be blank" }
 actions[action.type] = action
 return this
 }

 fun unregister(type: String): ChronosAction? = actions.remove(type)

 fun get(type: String): ChronosAction? = actions[type]

 fun contains(type: String): Boolean = actions.containsKey(type)

 fun all(): List<ChronosAction> = actions.values.toList()

 // Validates that every capability needed by type is currently granted.
 // Returns the missing capabilities, or an empty set when runnable.
 fun missingCapabilities(type: String, granted: Set<Capability>): Set<Capability> {
 val action = actions[type] ?: return emptySet()
 return action.requiredCapabilities() - granted
 }
}
