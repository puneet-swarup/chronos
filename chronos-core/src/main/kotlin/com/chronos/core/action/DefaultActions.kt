package com.chronos.core.action

import com.chronos.core.action.impl.DelayAction
import com.chronos.core.action.impl.LaunchAppAction
import com.chronos.core.action.impl.NotificationAction
import com.chronos.core.action.impl.OpenUrlAction

// Builds the default ActionRegistry with the built-in core actions. New actions
// are added here (or registered at runtime) without touching the scheduler.
object DefaultActions {

 fun registry(): ActionRegistry =
 ActionRegistry()
 .register(NotificationAction())
 .register(OpenUrlAction())
 .register(LaunchAppAction())
 .register(DelayAction())
}
