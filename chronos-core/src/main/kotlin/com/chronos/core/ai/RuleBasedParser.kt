package com.chronos.core.ai

import java.time.LocalTime

// Deterministic, offline natural-language parser used as the AI fallback.
// It recognizes a small, well-defined command grammar (English + common Hindi
// keywords) and produces a ParsedIntent. The full NL->workflow compiler then
// validates the intent before anything is scheduled.

enum class IntentKind { CREATE_ALARM, CREATE_REMINDER, OPEN_URL, NOTIFY, UNKNOWN }

data class ParsedIntent(
 val kind: IntentKind,
 val time: LocalTime? = null,
 val message: String = "",
 val url: String? = null,
 val confident: Boolean = false,
)

class RuleBasedParser {

 fun parse(input: String): ParsedIntent {
 val text = input.lowercase().trim()
 if (text.isEmpty()) return ParsedIntent(IntentKind.UNKNOWN)

 val time = extractTime(text)
 val url = extractUrl(input)

 if (url != null) {
 return ParsedIntent(IntentKind.OPEN_URL, time = time, url = url, confident = true)
 }

 val isAlarm = text.contains("alarm") || text.contains("wake me") ||
 text.contains("jaga") || text.contains("utha")
 val isReminder = text.contains("remind") || text.contains("yaad dilan") ||
 text.contains("yaad dila") || text.contains("reminder")

 return when {
 isAlarm -> ParsedIntent(IntentKind.CREATE_ALARM, time = time, message = input.trim(), confident = time != null)
 isReminder -> ParsedIntent(IntentKind.CREATE_REMINDER, time = time, message = input.trim(), confident = time != null)
 else -> ParsedIntent(IntentKind.UNKNOWN)
 }
 }

 // Extracts a HH:MM or "N am/pm" or "N baje" time from text.
 fun extractTime(text: String): LocalTime? {
 val colon = Regex("([01]?[0-9]|2[0-3]):([0-5][0-9])").find(text)
 if (colon != null) {
 val h = colon.groupValues[1].toIntOrNull() ?: return null
 val m = colon.groupValues[2].toIntOrNull() ?: return null
 return LocalTime.of(h, m)
 }
 val ampm = Regex("(\\d{1,2})\\s*(am|pm)").find(text)
 if (ampm != null) {
 var h = ampm.groupValues[1].toIntOrNull() ?: return null
 val pm = ampm.groupValues[2] == "pm"
 if (pm && h < 12) h += 12
 if (!pm && h == 12) h = 0
 return if (h in 0..23) LocalTime.of(h, 0) else null
 }
 val baje = Regex("(\\d{1,2})\\s*baje").find(text)
 if (baje != null) {
 val h = baje.groupValues[1].toIntOrNull() ?: return null
 return if (h in 0..23) LocalTime.of(h, 0) else null
 }
 return null
 }

 private fun extractUrl(raw: String): String? {
 val m = Regex("https?://\\S+").find(raw)
 return m?.value
 }
}
