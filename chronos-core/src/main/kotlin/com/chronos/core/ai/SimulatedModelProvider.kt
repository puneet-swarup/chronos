package com.chronos.core.ai

// A deterministic stand-in for a real on-device model. It lets the AI path
// (intent parsing -> workflow compilation -> validation) be exercised end to
// end in tests and on the emulator without downloading a multi-hundred-MB
// model or requiring a device with an NPU. It simply echoes a canned response
// that the parser/compiler understand.
class SimulatedModelProvider(
 private val cannedResponse: String = "alarm at 07:00",
 private val available: Boolean = true,
) : LocalModelProvider {

 override val name: String = "simulated"

 override suspend fun generate(prompt: String, maxTokens: Int): String {
 if (!available) throw IllegalStateException("Simulated model disabled")
 return cannedResponse
 }

 override fun isAvailable(): Boolean = available
}
