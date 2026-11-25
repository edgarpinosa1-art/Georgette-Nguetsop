package com.example

import com.example.domain.auditor.GodPromptAuditor
import com.example.domain.model.AnthologyLedger
import com.example.domain.model.PartAllocationMode
import com.example.domain.model.RuntimeAllocationMode
import com.example.domain.model.StoryCategory
import com.example.domain.model.StoryConfig
import com.example.domain.prompt.PromptKernelCompiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GodPromptAuditorTest {

    @Test
    fun auditor_stripsThinkAndDisclaimers_andConvertsDigitsToWords() {
        val raw = """
            <think>Internal reasoning monologue that must never survive.</think>
            I walked into unit 4B at 3:15 carrying $500 from 1924.
            [SFX: distant wooden door creak]
            The latch clicked behind me and I knew I would not leave before morning.
            Subscribe to our channel and hit the bell!
            This is a work of fiction.
        """.trimIndent()

        val outcome = GodPromptAuditor.auditAndSanitize(
            rawText = raw,
            category = StoryCategory.GENERAL_FICTION,
            targetCharCount = 1000,
            existingLedger = AnthologyLedger()
        )

        val cleaned = outcome.cleanedText
        assertFalse(cleaned.contains("<think>"))
        assertFalse(cleaned.contains("Internal reasoning"))
        assertFalse(cleaned.contains("This is a work of fiction"))
        assertFalse(cleaned.contains("Subscribe"))
        assertTrue(cleaned.contains("three fifteen"))
        assertTrue(cleaned.contains("five hundred dollars"))
        assertTrue(cleaned.contains("nineteen twenty-four"))
        assertTrue(cleaned.contains("[SFX: muffled wooden door creak]"))
        assertTrue(cleaned.contains(PromptKernelCompiler.SIGNATURE_OPENING))
        assertTrue(cleaned.contains(PromptKernelCompiler.SIGNATURE_CLOSING))
        assertTrue(cleaned.contains(PromptKernelCompiler.SILENCE_MARKER))
        assertTrue(outcome.report.zeroDigitsPassed)
        assertTrue(outcome.report.brandLockPassed)
        assertTrue(outcome.report.sfxValidPassed)
    }

    @Test
    fun auditor_stripsAllSfx_forTrueCrimeCategory() {
        val raw = """
            The detective logged the evidence locker entry at 11:05.
            [SFX: heavy metal locker slamming]
            Turn the page. Find what was left behind.
            [DEAD SILENCE DROP: 1.5s]
            Case records confirm two witnesses arrived before dawn.
            Who signed the missing ledger sheet?
            The page is closed. The questions remain.
            [DEAD SILENCE DROP: 1.5s]
        """.trimIndent()

        val outcome = GodPromptAuditor.auditAndSanitize(
            rawText = raw,
            category = StoryCategory.TRUE_CRIME,
            targetCharCount = 2000,
            existingLedger = AnthologyLedger()
        )

        assertFalse(outcome.cleanedText.contains("[SFX:"))
        assertTrue(outcome.report.sfxValidPassed)
        assertTrue(outcome.report.zeroDigitsPassed)
        assertTrue(outcome.report.brandLockPassed)
    }

    @Test
    fun storyConfig_calculatesRuntimeBudgetsCorrectly() {
        val perStory = StoryConfig(
            storyCount = 3,
            runtimeMinutes = 5,
            runtimeAllocationMode = RuntimeAllocationMode.PER_STORY
        )
        assertEquals(5000, perStory.targetCharsForStory(1))
        assertEquals(15000, perStory.totalTargetChars())

        val totalEpisode = StoryConfig(
            storyCount = 3,
            runtimeMinutes = 10,
            runtimeAllocationMode = RuntimeAllocationMode.TOTAL_EPISODE,
            partAllocationMode = PartAllocationMode.PER_STORY_MAP,
            perStoryPartMap = mapOf(2 to 4)
        )
        assertEquals(4, totalEpisode.targetMinutesForStory(1))
        assertEquals(3, totalEpisode.targetMinutesForStory(2))
        assertEquals(3, totalEpisode.targetMinutesForStory(3))
        assertEquals(10000, totalEpisode.totalTargetChars())
        assertEquals(4, totalEpisode.partsForStory(2))
    }

    @Test
    fun auditor_rewritesCliches_andDetectsLedgerCollisions() {
        val raw = """
            I froze when my blood ran cold inside the pickup.
            Turn the page. Find what was left behind.
            [DEAD SILENCE DROP: 1.5s]
            I checked the brass padlock on the cellar hatch and waited until dawn.
            Why did the latch rattle from the inside?
            The page is closed. The questions remain.
            [DEAD SILENCE DROP: 1.5s]
        """.trimIndent()

        val outcome = GodPromptAuditor.auditAndSanitize(
            rawText = raw,
            category = StoryCategory.GENERAL_FICTION,
            targetCharCount = 1000,
            existingLedger = AnthologyLedger(
                usedVehicles = setOf("pickup"),
                usedProps = setOf("padlock")
            )
        )

        assertFalse(outcome.cleanedText.lowercase().contains("i froze"))
        assertFalse(outcome.cleanedText.lowercase().contains("my blood ran cold"))
        assertTrue(outcome.report.detectedCliches.isEmpty())
        assertFalse(outcome.report.engineUniquePassed)
    }
}
