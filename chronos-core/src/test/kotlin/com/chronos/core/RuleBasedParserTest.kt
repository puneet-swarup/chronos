package com.chronos.core

import com.chronos.core.ai.IntentCompiler
import com.chronos.core.ai.IntentKind
import com.chronos.core.ai.RuleBasedParser
import com.chronos.core.action.DefaultActions
import com.chronos.core.workflow.WorkflowValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class RuleBasedParserTest {

 private val parser = RuleBasedParser()

 @Test
 fun parsesEnglishAlarmWithColonTime() {
 val i = parser.parse("set alarm at 07:30")
 assertEquals(IntentKind.CREATE_ALARM, i.kind)
 assertEquals(LocalTime.of(7, 30), i.time)
 }

 @Test
 fun parsesPmTime() {
 val i = parser.parse("wake me at 7 pm")
 assertEquals(IntentKind.CREATE_ALARM, i.kind)
 assertEquals(LocalTime.of(19, 0), i.time)
 }

 @Test
 fun parsesHinglishBaje() {
 val i = parser.parse("roz subah 6 baje alarm lagao")
 assertEquals(IntentKind.CREATE_ALARM, i.kind)
 assertEquals(LocalTime.of(6, 0), i.time)
 }

 @Test
 fun parsesHindiReminderKeyword() {
 val i = parser.parse("mujhe 9 baje yaad dilana")
 assertEquals(IntentKind.CREATE_REMINDER, i.kind)
 assertEquals(LocalTime.of(9, 0), i.time)
 }

 @Test
 fun parsesUrlIntent() {
 val i = parser.parse("open https://example.com")
 assertEquals(IntentKind.OPEN_URL, i.kind)
 assertEquals("https://example.com", i.url)
 }

 @Test
 fun unknownTextIsUnknown() {
 assertEquals(IntentKind.UNKNOWN, parser.parse("blah blah nothing").kind)
 assertEquals(IntentKind.UNKNOWN, parser.parse("").kind)
 }

 @Test
 fun compilesAlarmIntentToValidWorkflow() {
 val intent = parser.parse("set alarm at 07:30")
 val wf = IntentCompiler.compile(intent, "wf-1")
 assertNotNull(wf)
 val outcome = WorkflowValidator(DefaultActions.registry()).validate(wf!!)
 assertTrue(outcome.isValid)
 }

 @Test
 fun compilesUrlIntentToValidWorkflow() {
 val intent = parser.parse("open https://example.com")
 val wf = IntentCompiler.compile(intent, "wf-2")
 assertNotNull(wf)
 assertTrue(WorkflowValidator(DefaultActions.registry()).validate(wf!!).isValid)
 }
 @Test
 fun unknownIntentDoesNotCompile() {
 assertNull(IntentCompiler.compile(parser.parse("random words"), "wf-3"))
 }
}
