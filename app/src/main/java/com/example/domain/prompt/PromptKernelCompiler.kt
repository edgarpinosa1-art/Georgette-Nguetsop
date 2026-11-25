package com.example.domain.prompt

import com.example.domain.model.AnthologyLedger
import com.example.domain.model.EndingType
import com.example.domain.model.GeneratedStory
import com.example.domain.model.HorrorMechanism
import com.example.domain.model.OpeningHookType
import com.example.domain.model.StoryCategory
import com.example.domain.model.StoryConfig

/**
 * Compiles THE MISSING PAGE GOD PROMPT v5.3.4-US into a dense ~1,400-token system kernel
 * plus Stage A (Hidden 5-Beat Spine), Stage B (2-Half Seamless Prose / Expand), and Stage C
 * (Horror Engine & Anthology Memory Rotator) prompts.
 */
object PromptKernelCompiler {

    const val SIGNATURE_OPENING = "Turn the page. Find what was left behind."
    const val SIGNATURE_CLOSING = "The page is closed. The questions remain."
    const val SILENCE_MARKER = "[DEAD SILENCE DROP: 1.5s]"

    fun assignStoryBlueprint(
        storyIndex: Int,
        ledger: AnthologyLedger
    ): Triple<HorrorMechanism, OpeningHookType, EndingType> {
        val mechanisms = HorrorMechanism.entries
        val availableMechanisms = mechanisms.filterNot { it in ledger.usedMechanisms.takeLast(mechanisms.size - 1) }
        val mechanism = (availableMechanisms.ifEmpty { mechanisms })[(storyIndex - 1) % (availableMechanisms.ifEmpty { mechanisms }).size]
        val hook = OpeningHookType.entries[(storyIndex - 1) % OpeningHookType.entries.size]
        val ending = EndingType.entries[(storyIndex - 1) % EndingType.entries.size]
        return Triple(mechanism, hook, ending)
    }

    fun compileSystemKernel(
        config: StoryConfig,
        vaultNotesExcerpt: String? = null
    ): String {
        val category = config.category
        val povDirective = when (category) {
            StoryCategory.GENERAL_FICTION, StoryCategory.FICTIONALIZED_ADAPTATION ->
                "POV LOCK: Strict 1st-person confessional ('I', 'me', 'my'). Intimate spoken cadence."
            StoryCategory.DOCUMENTED_FOLKLORE ->
                "POV LOCK: 1st-person cultural researcher ('I'). Grounded, respectful tone toward oral traditions."
            StoryCategory.TRUE_CRIME ->
                "POV LOCK: Neutral chronological Forensic Documentary / Case Analysis. Strictly 1 verified case pattern per Story ID. Objective 3rd-person investigative narration."
            StoryCategory.HISTORICAL ->
                "POV LOCK: 1st-person immersive historical researcher-guide walking the listener through verified period atmosphere."
            StoryCategory.ALLEGED_PARANORMAL ->
                "POV LOCK: 1st-person permissioned witness account (1 account per Story ID) told as reported."
        }

        val sfxDirective = if (category.allowsSfx) {
            "SFX RULE: Close-mic [SFX: concise English sound description] cues ALLOWED sparingly (2 to 4 per story). NEVER use weak modifiers like 'distant', 'faint', or 'soft' inside [SFX: ...]. Use tactile close-mic acoustics such as 'dry wood latch snapping shut' or 'heavy ceramic mug set on Formica'."
        } else {
            "SFX RULE: STRICTLY ZERO [SFX: ...] tags allowed for ${category.displayName}. Do not output any [SFX: ...] bracket cues."
        }

        val vaultLine = if (!vaultNotesExcerpt.isNullOrBlank()) {
            "\nCUSTOM VAULT DIRECTIVE ACTIVE: Honor v5.3.4-US archival fidelity."
        } else ""

        return """
/no_think
You are the narrative engine for THE MISSING PAGE — ENGLISH STORIES (@TheMissingPage-US), producing S-tier, earphone-first American English audio-drama scripts.

[IDENTITY & STYLE]
- Voice: Natural, grounded American English oral storytelling built for late-night headphone listening. Short-to-medium tactile sentences, concrete physical details, rhythmic pacing.
- Tone: Editorial noir restraint. Quiet dread earned through specificity, never melodrama.
$povDirective
$sfxDirective

[ADMIN CONTROL PANEL]
- Category: ${category.displayName}
- Story Count: ${config.storyCount}
- Runtime Budget Rule: 1,000 spoken characters (including spaces & punctuation, excluding bracket markers) equals 1 minute.

[MANDATORY OPENING & CLOSING SIGNATURES]
- Block 1 Opening: Start with a crisp story-specific hook (2-3 sentences), then output on its own line:
$SIGNATURE_OPENING
$SILENCE_MARKER
Then continue immediately into the story narration.
- Final Block Closing: Resolve the story, ask ONE ${category.finalQuestionStyle}, then output on its own lines:
$SIGNATURE_CLOSING
$SILENCE_MARKER
- ZERO like, comment, subscribe, notification bell, or channel-promotion CTAs anywhere.

[STRICT BANS — ZERO TOLERANCE]
1. ZERO numerical digits (0-9) anywhere in spoken text. Write ALL numbers, years, clock times, ages, highway numbers, and dollar amounts in conversational American English words (e.g., 'nineteen twenty-four', 'three fifteen', 'five hundred dollars').
2. ZERO spoken out-of-universe disclaimers (NEVER write 'This is a work of fiction', 'Fictional adaptation', 'Disclaimer', 'Note:', or 'AI generated').
3. ZERO AI clichés: NEVER use 'I froze', 'I couldn't explain it', 'hair on the back of my neck', 'my blood ran cold', 'chills ran down my spine', 'little did I know', or 'time seemed to stand still'.
4. ZERO filler small-talk dialogue ('Hey, how are you?', 'Let's get out of here!'). Every spoken line must turn the plot or reveal pressure.
5. ZERO B-roll, camera, lighting, image, or video generation prompts. Output pure script text only.$vaultLine
""".trimIndent()
    }

    fun compileSpinePrompt(
        storyId: String,
        storyIndex: Int,
        config: StoryConfig,
        mechanism: HorrorMechanism,
        hookType: OpeningHookType,
        endingType: EndingType,
        ledger: AnthologyLedger
    ): String {
        val targetMins = config.targetMinutesForStory(storyIndex)
        val targetChars = config.targetCharsForStory(storyIndex)
        val premiseBlock = if (config.storyPremise.isNotBlank()) {
            "User Premise / Seed: ${config.storyPremise.trim()}"
        } else {
            "User Premise / Seed: Original grounded American setting (no generic abandoned asylum tropes)."
        }

        val doNotReuse = buildString {
            if (ledger.usedNames.isNotEmpty()) append("\n- DO NOT REUSE NAMES: ${ledger.usedNames.take(18).joinToString(", ")}")
            if (ledger.usedVehicles.isNotEmpty()) append("\n- DO NOT REUSE VEHICLES: ${ledger.usedVehicles.take(10).joinToString(", ")}")
            if (ledger.usedProps.isNotEmpty()) append("\n- DO NOT REUSE KEY PROPS: ${ledger.usedProps.take(12).joinToString(", ")}")
        }

        return """
/no_think
STAGE A — INTERNAL 5-BEAT STORY SPINE for $storyId (Category: ${config.category.displayName}, Target: $targetMins minutes / $targetChars spoken chars).
$premiseBlock

Assigned Stage C Constraints:
- Horror / Tension Mechanism: ${mechanism.label} — ${mechanism.guidance}
- Opening Hook Type: ${hookType.label} — ${hookType.guidance}
- Ending Type: ${endingType.label} — ${endingType.guidance}$doNotReuse

Output ONLY a crisp 6-line outline with NO prose and NO digits (0-9):
TITLE: <evocative 3-to-6 word chapter title>
1. PROTAGONIST & FLAW: <grounded occupation, believable flaw>
2. SETTING & ROUTINE: <specific American location, ordinary sensory routine>
3. THREAT RULE & TRIGGER: <concrete anomaly, rule, or forensic anomaly>
4. MIDPOINT COMPLICATION: <failed escape or worsening constraint>
5. EARNED CLIMAX & RESIDUE: <${endingType.label} payoff + final question seed>
""".trimIndent()
    }

    fun compileSinglePassProsePrompt(
        storyId: String,
        storyIndex: Int,
        config: StoryConfig,
        spine: String,
        mechanism: HorrorMechanism,
        hookType: OpeningHookType,
        endingType: EndingType
    ): String {
        val parts = config.partsForStory(storyIndex)
        val targetMins = config.targetMinutesForStory(storyIndex)
        val targetChars = config.targetCharsForStory(storyIndex)
        val sfxNote = if (config.category.allowsSfx) {
            "Include 2 close-mic [SFX: ...] markers where physically earned (no 'distant', 'faint', or 'soft')."
        } else {
            "Include ZERO [SFX: ...] markers."
        }

        return """
/no_think
STAGE B — FULL SCRIPT GENERATION for $storyId (${config.category.displayName}, $parts structured beats, target ~$targetChars spoken characters / $targetMins min).
Follow this 5-Beat Story Spine strictly:
$spine

Requirements:
- Hook Style: ${hookType.label}
- Mechanism: ${mechanism.label}
- Ending Style: ${endingType.label}
- $sfxNote
- Start with the 2-3 sentence hook, then on its own line:
$SIGNATURE_OPENING
$SILENCE_MARKER
- Deliver the complete narrative across $parts escalating movements without markdown headers or bold labels.
- End with resolution + ONE ${config.category.finalQuestionStyle}, then on its own lines:
$SIGNATURE_CLOSING
$SILENCE_MARKER
- Write ALL numbers as English words. Output ONLY the spoken script and valid markers.
""".trimIndent()
    }

    fun compilePartAPrompt(
        storyId: String,
        storyIndex: Int,
        config: StoryConfig,
        spine: String,
        hookType: OpeningHookType,
        mechanism: HorrorMechanism
    ): String {
        val totalTarget = config.targetCharsForStory(storyIndex)
        val halfTarget = (totalTarget * 0.52).toInt()
        val sfxNote = if (config.category.allowsSfx) {
            "Use 1 to 2 close-mic [SFX: ...] cues max (no 'distant', 'faint', or 'soft')."
        } else {
            "Use ZERO [SFX: ...] cues."
        }

        return """
/no_think
STAGE B (PART A OF 2) for $storyId — SETUP & ESCALATION (Target ~$halfTarget spoken characters).
Follow Beats 1, 2, 3, and the start of Beat 4 from this Story Spine:
$spine

Rules for Part A:
- Open with a ${hookType.label} hook (2-3 crisp sentences), then output:
$SIGNATURE_OPENING
$SILENCE_MARKER
- Develop the setting, routine, and ${mechanism.label} rule violation/trigger up to the midpoint complication.
- $sfxNote
- DO NOT resolve the story and DO NOT write the closing signature yet. Stop cleanly mid-tension right after the midpoint complication lands.
- ZERO digits (0-9). ZERO markdown headers. ZERO disclaimers.
""".trimIndent()
    }

    fun compilePartBPrompt(
        storyId: String,
        storyIndex: Int,
        config: StoryConfig,
        spine: String,
        endingType: EndingType,
        partATail: String
    ): String {
        val totalTarget = config.targetCharsForStory(storyIndex)
        val remainingTarget = (totalTarget * 0.50).toInt()
        val sfxNote = if (config.category.allowsSfx) {
            "Use 1 to 2 close-mic [SFX: ...] cues max (no 'distant', 'faint', or 'soft')."
        } else {
            "Use ZERO [SFX: ...] cues."
        }

        return """
/no_think
STAGE B (PART B OF 2) for $storyId — CLIMAX, RESOLUTION & OUTRO (Target ~$remainingTarget spoken characters).
Follow Beats 4 and 5 of the Story Spine:
$spine

Continue SEAMLESSLY from the exact final sentence of Part A below without repeating it or adding greetings:
"...$partATail"

Rules for Part B:
- Escalate the failed escape into an earned ${endingType.label} climax and quiet aftermath.
- $sfxNote
- End with ONE ${config.category.finalQuestionStyle}, then output on its own lines:
$SIGNATURE_CLOSING
$SILENCE_MARKER
- ZERO digits (0-9). ZERO markdown headers. ZERO subscribe/like CTAs. ZERO disclaimers.
""".trimIndent()
    }

    fun compileExpandStoryPrompt(
        story: GeneratedStory,
        missingChars: Int
    ): String {
        val sfxNote = if (story.category.allowsSfx) {
            "You may include 1 close-mic [SFX: ...] cue (no 'distant', 'faint', or 'soft')."
        } else {
            "Include ZERO [SFX: ...] cues."
        }

        return """
/no_think
EXPANSION PASS for ${story.storyId} ("${story.chapterTitle}").
The current draft is $missingChars spoken characters short of its target runtime.
Expand and deepen the middle tension and sensory texture WITHOUT changing the plot spine or repeating the intro/outro signatures.
Story Spine:
${story.hiddenSpine}

Existing Script:
${story.ttsReadyText}

Rewrite and seamlessly expand the narrative body by roughly $missingChars characters with concrete physical details, keeping the exact opening signature ($SIGNATURE_OPENING) and exact closing signature ($SIGNATURE_CLOSING) intact.
$sfxNote
ZERO digits (0-9). ZERO clichés. ZERO disclaimers.
""".trimIndent()
    }

    fun extractChapterTitle(spine: String, fallbackIndex: Int): String {
        val line = spine.lineSequence()
            .firstOrNull { it.trim().startsWith("TITLE:", ignoreCase = true) }
            ?.substringAfter(":")?.trim()
            ?.replace(Regex("""[*_`"#]"""), "")
            ?.takeIf { it.isNotBlank() }
        return line ?: "Case File Number $fallbackIndex"
    }

    fun buildTtsReadyExport(stories: List<GeneratedStory>): String {
        return stories.joinToString(separator = "\n\n") { it.ttsReadyText.trim() }
    }

    fun buildFullMasterExport(
        episodeTitle: String,
        stories: List<GeneratedStory>,
        config: StoryConfig
    ): String {
        val disclosure = config.category.publicDisclosure
        val storyBlocks = stories.joinToString(separator = "\n\n") { story ->
            buildString {
                appendLine("[CHAPTER TITLE: ${story.storyId} — ${story.chapterTitle}]")
                appendLine("[ENGINE PROFILE: ${story.mechanism.label} | ${story.openingHookType.label} | ${story.endingType.label}]")
                appendLine()
                append(story.ttsReadyText.trim())
            }
        }

        return buildString {
            appendLine("================================================================================")
            appendLine("THE MISSING PAGE — ENGLISH STORIES (@TheMissingPage-US)")
            appendLine("MASTER SCRIPT ARCHIVE — ${episodeTitle.uppercase()}")
            appendLine("================================================================================")
            appendLine()
            appendLine("[PRODUCTION NOTES]")
            appendLine("[PUBLIC DISCLOSURE — COPY TO YOUTUBE DESCRIPTION]")
            appendLine(disclosure)
            appendLine()
            appendLine("[INTERNAL PRODUCTION INSTRUCTIONS — DO NOT PUBLISH]")
            appendLine("- Engine Compliance: THE MISSING PAGE GOD PROMPT v5.3.4-US (Script-Only S-Tier)")
            appendLine("- Category Lock: ${config.category.displayName} (${config.category.povRuleLabel})")
            appendLine("- Audio Direction: ${config.category.sfxRuleLabel}; preserve 1.5s dead silence drops strictly.")
            appendLine("- Story Count: ${stories.size} | Total Spoken Budget: ${stories.sumOf { it.auditReport.spokenCharCount }} / ${stories.sumOf { it.targetChars }} chars")
            appendLine()
            appendLine("--------------------------------------------------------------------------------")
            appendLine()
            append(storyBlocks)
        }.trimEnd()
    }
}
