package com.chronos.core.ai

// Replaceable on-device model runtime seam. LiteRT-LM / FunctionGemma can be
// swapped in later without touching the scheduler. Inference is never required
// for core scheduling; when no model is present the rules parser is used.
interface LocalModelProvider {
 val name: String

 suspend fun generate(prompt: String, maxTokens: Int = 512): String

 fun isAvailable(): Boolean
}

class NoOpModelProvider : LocalModelProvider {
 override val name: String = "none"
 override suspend fun generate(prompt: String, maxTokens: Int): String {
 throw IllegalStateException("No on-device model is available")
 }
 override fun isAvailable(): Boolean = false
}
