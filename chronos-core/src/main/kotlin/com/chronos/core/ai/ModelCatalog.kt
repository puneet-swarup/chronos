package com.chronos.core.ai

// Catalogue of supported on-device models (PRD AI tiers). Sizes are the
// published deployment package sizes, used for storage checks and UI display.
enum class ModelTier { LITE, STANDARD, PRO }

data class ModelDescriptor(
 val id: String,
 val displayName: String,
 val tier: ModelTier,
 val sizeBytes: Long,
 val sha256: String,
 val downloadUrl: String,
)

object ModelCatalog {
 val FunctionGemma270M = ModelDescriptor(
 id = "functiongemma-270m",
 displayName = "Chronos Lite (FunctionGemma 270M)",
 tier = ModelTier.LITE,
 sizeBytes = 288L * 1024 * 1024,
 sha256 = "0000000000000000000000000000000000000000000000000000000000000000",
 downloadUrl = "https://models.example.invalid/functiongemma-270m.litertlm",
 )

 val Gemma3_1B = ModelDescriptor(
 id = "gemma3-1b-it",
 displayName = "Chronos Standard (Gemma 3 1B IT)",
 tier = ModelTier.STANDARD,
 sizeBytes = 1005L * 1024 * 1024,
 sha256 = "0000000000000000000000000000000000000000000000000000000000000000",
 downloadUrl = "https://models.example.invalid/gemma3-1b-it.litertlm",
 )

 val all: List<ModelDescriptor> = listOf(FunctionGemma270M, Gemma3_1B)

 fun byTier(tier: ModelTier): ModelDescriptor? = all.firstOrNull { it.tier == tier }
}
