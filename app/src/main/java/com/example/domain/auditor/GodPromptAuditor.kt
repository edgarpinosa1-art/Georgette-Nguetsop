package com.example.domain.auditor

import com.example.domain.model.AnthologyLedger
import com.example.domain.model.AuditReport
import com.example.domain.model.StoryCategory
import com.example.domain.prompt.PromptKernelCompiler

/**
 * Deterministic Post-Processor & Unlocked-Model Sanitizer for THE MISSING PAGE GOD PROMPT v5.3.4-US.
 */
object GodPromptAuditor {

    private val thinkBlockRegex = Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE)
    private val unclosedThinkRegex = Regex("""<think>[\s\S]*$""", RegexOption.IGNORE_CASE)

    private val disclaimerLineRegex = Regex(
        """(?im)^.*(this is a work of fiction|fictionalized adaptation|fictional adaptation|this is general information|disclaimer:|author'?s note:|note:\s*the following|for entertainment purposes only|ai[- ]generated).*$"""
    )

    private val ctaLineRegex = Regex(
        """(?im)^.*(like and subscribe|subscribe to (the|our) channel|hit the (bell|like)|smash that like|leave a comment below|don'?t forget to subscribe|follow us on).*$"""
    )

    private val markdownHeaderRegex = Regex("""(?m)^#{1,6}\s+.*$""")
    private val sfxRegex = Regex("""\[SFX:\s*([^\]]+)]""", RegexOption.IGNORE_CASE)
    private val silenceRegex = Regex("""\[DEAD SILENCE DROP:\s*1\.5s]""", RegexOption.IGNORE_CASE)

    private val clicheReplacements = linkedMapOf(
        "i couldn't explain it" to "nothing in the room accounted for it",
        "i could not explain it" to "nothing in the room accounted for it",
        "hair on the back of my neck" to "skin across my collarbones",
        "hairs on the back of my neck" to "skin across my collarbones",
        "my blood ran cold" to "the warmth drained out of my hands",
        "chills ran down my spine" to "a cold draft settled along my ribs",
        "shiver down my spine" to "cold tight knot between my shoulders",
        "little did i know" to "I only pieced it together afterward",
        "time seemed to stop" to "the room went completely still",
        "time seemed to stand still" to "the room went completely still",
        "i froze" to "I held my breath"
    )

    data class AuditOutcome(
        val cleanedText: String,
        val report: AuditReport
    )

    fun auditAndSanitize(
        rawText: String,
        category: StoryCategory,
        targetCharCount: Int,
        existingLedger: AnthologyLedger
    ): AuditOutcome {
        val repairs = mutableListOf<String>()

        // 1. Strip <think> blocks, markdown artifacts, and baked-in disclaimers
        var text = stripThinkingAndDisclaimers(rawText, repairs)

        // 2. Strip accidental YouTube like/subscribe/comment CTAs
        if (ctaLineRegex.containsMatchIn(text)) {
            text = ctaLineRegex.replace(text, "")
            repairs.add("Stripped CTA violation")
        }

        // 3. Category SFX enforcement & close-mic word repair
        text = enforceCategorySfx(text, category, repairs)

        // 4. Deterministic Digit-to-Words Auto-Converter (Section 29.1)
        val beforeDigits = text
        text = convertDigitsToWords(text)
        if (beforeDigits != text) {
            repairs.add("Converted digits to English words")
        }

        // 5. Enforce exact Opening/Closing Signatures & Dead Silence Drops (Sections 17 & 34)
        text = enforceSignaturesAndSilence(text, repairs)

        // 6. Auto-rewrite banned AI clichés before final checks
        val lowerBeforeCliche = stripBracketCues(text).lowercase()
        val foundCliches = clicheReplacements.keys.filter { lowerBeforeCliche.contains(it) }
        if (foundCliches.isNotEmpty()) {
            clicheReplacements.forEach { (cliche, replacement) ->
                text = Regex("""\b${Regex.escape(cliche)}\b""", RegexOption.IGNORE_CASE)
                    .replace(text, replacement)
            }
            repairs.add("Rewrote ${foundCliches.size} banned AI cliché(s)")
        }

        // Normalize whitespace
        text = text.replace(Regex("""\n{3,}"""), "\n\n").trim()

        // Evaluate checks
        val spokenTextOnly = stripBracketCues(text)
        val zeroDigitsPassed = !spokenTextOnly.any { it.isDigit() }
        val brandLockPassed =
            text.contains(PromptKernelCompiler.SIGNATURE_OPENING) &&
                text.contains(PromptKernelCompiler.SIGNATURE_CLOSING) &&
                text.contains(PromptKernelCompiler.SILENCE_MARKER)

        val povLockPassed = evaluatePovLock(spokenTextOnly, category)
        val sfxValidPassed = if (!category.allowsSfx) {
            !sfxRegex.containsMatchIn(text)
        } else {
            val sfxMatches = sfxRegex.findAll(text).map { it.groupValues[1].lowercase() }.toList()
            sfxMatches.none { desc ->
                desc.contains("distant") || desc.contains("faint") || desc.contains("soft")
            }
        }

        val remainingCliches = clicheReplacements.keys.filter { spokenTextOnly.lowercase().contains(it) }
        val engineUniquePassed = checkLedgerUniqueness(spokenTextOnly, existingLedger)
        val spokenChars = countSpokenCharacters(text)

        return AuditOutcome(
            cleanedText = text,
            report = AuditReport(
                zeroDigitsPassed = zeroDigitsPassed,
                brandLockPassed = brandLockPassed,
                povLockPassed = povLockPassed,
                sfxValidPassed = sfxValidPassed,
                engineUniquePassed = engineUniquePassed,
                spokenCharCount = spokenChars,
                targetCharCount = targetCharCount,
                detectedCliches = remainingCliches,
                repairsApplied = repairs
            )
        )
    }

    private fun stripThinkingAndDisclaimers(raw: String, repairs: MutableList<String>): String {
        var result = raw
        if (thinkBlockRegex.containsMatchIn(result) || unclosedThinkRegex.containsMatchIn(result)) {
            result = thinkBlockRegex.replace(result, "")
            result = unclosedThinkRegex.replace(result, "")
            repairs.add("Stripped <think> monologue")
        }
        if (disclaimerLineRegex.containsMatchIn(result)) {
            result = disclaimerLineRegex.replace(result, "")
            repairs.add("Stripped out-of-universe disclaimer")
        }
        result = markdownHeaderRegex.replace(result, "")
        result = result.replace("**", "").replace("__", "")
        result = result.replace(Regex("""(?im)^\[(PART\s+[AB]|END\s+PART\s+[AB]|CHAPTER\s+TITLE:[^\]]*)\]\s*$"""), "")
        return result.trim()
    }

    private fun enforceCategorySfx(
        input: String,
        category: StoryCategory,
        repairs: MutableList<String>
    ): String {
        if (!category.allowsSfx) {
            if (sfxRegex.containsMatchIn(input)) {
                repairs.add("Removed SFX cues (${category.displayName})")
                return sfxRegex.replace(input, "")
            }
            return input
        }

        var modified = false
        val cleaned = sfxRegex.replace(input) { match ->
            var desc = match.groupValues[1].trim()
            val updated = desc
                .replace(Regex("""\bdistant\b""", RegexOption.IGNORE_CASE), "muffled")
                .replace(Regex("""\bfaint\b""", RegexOption.IGNORE_CASE), "close-mic")
                .replace(Regex("""\bsoft\b""", RegexOption.IGNORE_CASE), "subtle")
            if (updated != desc) modified = true
            "[SFX: $updated]"
        }
        if (modified) repairs.add("Hardened close-mic SFX phrasing")
        return cleaned
    }

    private fun enforceSignaturesAndSilence(input: String, repairs: MutableList<String>): String {
        var text = input.trim()

        // Normalize near-miss silence tags
        text = silenceRegex.replace(text, PromptKernelCompiler.SILENCE_MARKER)

        // Deduplicate opening/closing signatures if 2-half stitching produced duplicates
        val openMatches = Regex(Regex.escape(PromptKernelCompiler.SIGNATURE_OPENING)).findAll(text).toList()
        if (openMatches.size > 1) {
            openMatches.drop(1).reversed().forEach { match ->
                text = text.removeRange(match.range)
            }
            repairs.add("Deduplicated opening signature")
        }
        val closeMatches = Regex(Regex.escape(PromptKernelCompiler.SIGNATURE_CLOSING)).findAll(text).toList()
        if (closeMatches.size > 1) {
            closeMatches.dropLast(1).reversed().forEach { match ->
                text = text.removeRange(match.range)
            }
            repairs.add("Deduplicated closing signature")
        }

        // Ensure opening signature exists cleanly
        if (!text.contains(PromptKernelCompiler.SIGNATURE_OPENING)) {
            val firstSentenceEnd = findFirstHookSplitIndex(text)
            val hook = text.substring(0, firstSentenceEnd).trim()
            val rest = text.substring(firstSentenceEnd).trim()
            text = buildString {
                if (hook.isNotEmpty()) appendLine(hook)
                appendLine(PromptKernelCompiler.SIGNATURE_OPENING)
                appendLine(PromptKernelCompiler.SILENCE_MARKER)
                if (rest.isNotEmpty()) append(rest)
            }.trim()
            repairs.add("Injected opening signature & silence drop")
        } else {
            // Guarantee dead silence drop right after opening signature
            val sigOpen = Regex.escape(PromptKernelCompiler.SIGNATURE_OPENING)
            val openPattern = Regex("""$sigOpen(?!\s*\n?\s*\[DEAD SILENCE DROP: 1\.5s])""")
            if (openPattern.containsMatchIn(text)) {
                text = openPattern.replace(text) {
                    "${PromptKernelCompiler.SIGNATURE_OPENING}\n${PromptKernelCompiler.SILENCE_MARKER}"
                }
                repairs.add("Enforced opening silence drop")
            }
        }

        // Ensure closing signature exists cleanly
        if (!text.contains(PromptKernelCompiler.SIGNATURE_CLOSING)) {
            text = buildString {
                appendLine(text)
                appendLine(PromptKernelCompiler.SIGNATURE_CLOSING)
                append(PromptKernelCompiler.SILENCE_MARKER)
            }
            repairs.add("Injected closing signature & silence drop")
        } else {
            val sigClose = Regex.escape(PromptKernelCompiler.SIGNATURE_CLOSING)
            val closePattern = Regex("""$sigClose(?!\s*\n?\s*\[DEAD SILENCE DROP: 1\.5s])""")
            if (closePattern.containsMatchIn(text)) {
                text = closePattern.replace(text) {
                    "${PromptKernelCompiler.SIGNATURE_CLOSING}\n${PromptKernelCompiler.SILENCE_MARKER}"
                }
                repairs.add("Enforced closing silence drop")
            }
        }

        return text
    }

    private fun findFirstHookSplitIndex(text: String): Int {
        val sentenceRegex = Regex("""[.!?](?:\s+|\n+)""")
        val matches = sentenceRegex.findAll(text).take(2).toList()
        return if (matches.isNotEmpty()) matches.last().range.last + 1 else 0
    }

    private fun evaluatePovLock(spokenText: String, category: StoryCategory): Boolean {
        if (!category.requiresFirstPerson) return true
        val firstPersonMatches = Regex("""\b(I|I'm|I've|I'd|I'll|me|my|mine)\b""").findAll(spokenText).count()
        return firstPersonMatches >= 3 || spokenText.length < 180
    }

    private fun checkLedgerUniqueness(spokenText: String, ledger: AnthologyLedger): Boolean {
        val lower = spokenText.lowercase()
        val repeatedName = ledger.usedNames.any { name ->
            name.length > 3 && Regex("""\b${Regex.escape(name.lowercase())}\b""").containsMatchIn(lower)
        }
        val repeatedVehicle = ledger.usedVehicles.any { vehicle ->
            Regex("""\b${Regex.escape(vehicle.lowercase())}\b""").containsMatchIn(lower)
        }
        val repeatedProp = ledger.usedProps.any { prop ->
            Regex("""\b${Regex.escape(prop.lowercase())}\b""").containsMatchIn(lower)
        }
        return !repeatedName && !repeatedVehicle && !repeatedProp
    }

    fun stripBracketCues(text: String): String {
        return text
            .replace(sfxRegex, "")
            .replace(silenceRegex, "")
            .replace(Regex("""\[[^\]]+]"""), "")
    }

    fun countSpokenCharacters(text: String): Int {
        return stripBracketCues(text)
            .replace(Regex("""\s+"""), " ")
            .trim()
            .length
    }

    fun extractNewEntities(text: String): Triple<Set<String>, Set<String>, Set<String>> {
        val spoken = stripBracketCues(text)
        val stopWords = setOf(
            "The", "Turn", "Find", "What", "When", "Where", "There", "Then", "This", "That",
            "After", "Before", "Inside", "Outside", "Because", "Every", "Someone", "Nothing",
            "Everything", "January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "American", "English"
        )
        val names = Regex("""(?<!^)(?<!\.\s)\b([A-Z][a-z]{2,14})\b""")
            .findAll(spoken)
            .map { it.groupValues[1] }
            .filter { it !in stopWords }
            .distinct()
            .take(8)
            .toSet()

        val vehicleKeywords = listOf(
            "sedan", "pickup", "truck", "station wagon", "subaru", "ford", "chevy",
            "honda", "toyota", "jeep", "buick", "van", "cruiser", "motorcycle"
        )
        val lower = spoken.lowercase()
        val vehicles = vehicleKeywords.filter { Regex("""\b$it\b""").containsMatchIn(lower) }.toSet()

        val propKeywords = listOf(
            "cassette", "polaroid", "lantern", "keychain", "ledger", "locket",
            "tape recorder", "padlock", "compass", "flashlight", "briefcase", "typewriter"
        )
        val props = propKeywords.filter { Regex("""\b$it\b""").containsMatchIn(lower) }.toSet()

        return Triple(names, vehicles, props)
    }

    fun convertDigitsToWords(input: String): String {
        val protectedTokens = mutableListOf<String>()
        var working = Regex("""\[[^\]]+]""").replace(input) { match ->
            val idx = protectedTokens.size
            protectedTokens.add(match.value)
            "__BRACKET_TOKEN_${idx}__"
        }

        // 1. Currency ($1,250.50 or $500)
        working = Regex("""\$(\d{1,3}(?:,\d{3})+|\d+)(?:\.(\d{2}))?""").replace(working) { match ->
            val whole = match.groupValues[1].replace(",", "").toLongOrNull() ?: return@replace match.value
            val cents = match.groupValues[2].toIntOrNull()
            val wholeWords = numberToWords(whole)
            val dollarUnit = if (whole == 1L) "dollar" else "dollars"
            if (cents != null && cents > 0) {
                val centUnit = if (cents == 1) "cent" else "cents"
                "$wholeWords $dollarUnit and ${numberToWords(cents.toLong())} $centUnit"
            } else {
                "$wholeWords $dollarUnit"
            }
        }

        // 2. Clock Times (3:15, 04:05, 11:30)
        working = Regex("""\b(\d{1,2}):(\d{2})\b""").replace(working) { match ->
            val hour = match.groupValues[1].toIntOrNull() ?: return@replace match.value
            val min = match.groupValues[2].toIntOrNull() ?: return@replace match.value
            if (hour > 24 || min > 59) return@replace match.value
            val hourWord = numberToWords(hour.toLong())
            val minWord = when {
                min == 0 -> "o'clock"
                min < 10 -> "oh ${numberToWords(min.toLong())}"
                else -> numberToWords(min.toLong())
            }
            "$hourWord $minWord"
        }

        // 3. Percentages (45%)
        working = Regex("""\b(\d+(?:\.\d+)?)%""").replace(working) { match ->
            val rawNum = match.groupValues[1]
            "${decimalOrIntToWords(rawNum)} percent"
        }

        // 4. Ordinals (1st, 2nd, 3rd, 24th)
        working = Regex("""\b(\d+)(st|nd|rd|th)\b""", RegexOption.IGNORE_CASE).replace(working) { match ->
            val num = match.groupValues[1].toLongOrNull() ?: return@replace match.value
            numberToOrdinal(num)
        }

        // 5. Decimals (3.5)
        working = Regex("""\b(\d+)\.(\d+)\b""").replace(working) { match ->
            val whole = match.groupValues[1].toLongOrNull() ?: return@replace match.value
            val fracDigits = match.groupValues[2].map { digitWord(it) }.joinToString(" ")
            "${numberToWords(whole)} point $fracDigits"
        }

        // 6. Four-digit years (1800..2099)
        working = Regex("""\b(1[89]\d{2}|20\d{2})\b""").replace(working) { match ->
            yearToWords(match.groupValues[1].toInt())
        }

        // 7. Comma-formatted or remaining standalone integers
        working = Regex("""\b\d{1,3}(?:,\d{3})+\b|\b\d+\b""").replace(working) { match ->
            val clean = match.value.replace(",", "")
            val value = clean.toLongOrNull()
            if (value != null && value <= 999_999_999L) {
                numberToWords(value)
            } else {
                clean.map { digitWord(it) }.joinToString(" ")
            }
        }

        // 8. Any remaining digits attached to letters (e.g., 4B -> four B, 12oz -> twelve oz)
        // Skip __BRACKET_TOKEN_N__ placeholders
        working = Regex("""(__BRACKET_TOKEN_\d+__)|(\d+)""").replace(working) { match ->
            if (match.groupValues[1].isNotEmpty()) {
                match.groupValues[1]
            } else {
                val clean = match.groupValues[2]
                val value = clean.toLongOrNull()
                if (value != null && value <= 999_999_999L) {
                    " ${numberToWords(value)} "
                } else {
                    " ${clean.map { digitWord(it) }.joinToString(" ")} "
                }
            }
        }.replace(Regex(""" {2,}"""), " ")

        // Restore protected bracket tokens
        protectedTokens.forEachIndexed { idx, original ->
            working = working.replace("__BRACKET_TOKEN_${idx}__", original)
        }

        return working
    }

    private fun decimalOrIntToWords(raw: String): String {
        return if (raw.contains(".")) {
            val parts = raw.split(".")
            val whole = parts[0].toLongOrNull()?.let { numberToWords(it) } ?: parts[0]
            val frac = parts[1].map { digitWord(it) }.joinToString(" ")
            "$whole point $frac"
        } else {
            raw.toLongOrNull()?.let { numberToWords(it) } ?: raw
        }
    }

    private fun yearToWords(year: Int): String {
        return when {
            year == 2000 -> "two thousand"
            year in 2001..2009 -> "two thousand ${numberToWords((year - 2000).toLong())}"
            year % 100 == 0 -> "${numberToWords((year / 100).toLong())} hundred"
            year % 100 < 10 -> "${numberToWords((year / 100).toLong())} oh ${numberToWords((year % 100).toLong())}"
            else -> "${numberToWords((year / 100).toLong())} ${numberToWords((year % 100).toLong())}"
        }
    }

    private fun numberToOrdinal(num: Long): String {
        val special = mapOf(
            1L to "first", 2L to "second", 3L to "third", 4L to "fourth", 5L to "fifth",
            6L to "sixth", 7L to "seventh", 8L to "eighth", 9L to "ninth", 10L to "tenth",
            11L to "eleventh", 12L to "twelfth", 13L to "thirteenth", 15L to "fifteenth",
            18L to "eighteenth", 19L to "nineteenth", 20L to "twentieth", 30L to "thirtieth",
            40L to "fortieth", 50L to "fiftieth", 60L to "sixtieth", 70L to "seventieth",
            80L to "eightieth", 90L to "ninetieth"
        )
        special[num]?.let { return it }
        if (num in 21..99) {
            val tens = (num / 10) * 10
            val ones = num % 10
            return "${numberToWords(tens)}-${special[ones] ?: numberToWords(ones)}"
        }
        val base = numberToWords(num)
        return when {
            base.endsWith("y") -> base.dropLast(1) + "ieth"
            base.endsWith("ve") -> base.dropLast(2) + "fth"
            base.endsWith("t") -> base + "h"
            base.endsWith("e") -> base.dropLast(1) + "th"
            else -> base + "th"
        }
    }

    private val ones = arrayOf(
        "", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
        "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
        "seventeen", "eighteen", "nineteen"
    )
    private val tens = arrayOf(
        "", "", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"
    )

    private fun digitWord(c: Char): String = when (c) {
        '0' -> "zero"
        '1' -> "one"
        '2' -> "two"
        '3' -> "three"
        '4' -> "four"
        '5' -> "five"
        '6' -> "six"
        '7' -> "seven"
        '8' -> "eight"
        '9' -> "nine"
        else -> c.toString()
    }

    fun numberToWords(n: Long): String {
        if (n == 0L) return "zero"
        if (n < 0L) return "negative ${numberToWords(-n)}"
        val parts = mutableListOf<String>()
        var rem = n
        if (rem >= 1_000_000L) {
            parts.add("${underThousandToWords((rem / 1_000_000L).toInt())} million")
            rem %= 1_000_000L
        }
        if (rem >= 1_000L) {
            parts.add("${underThousandToWords((rem / 1_000L).toInt())} thousand")
            rem %= 1_000L
        }
        if (rem > 0L) {
            parts.add(underThousandToWords(rem.toInt()))
        }
        return parts.joinToString(" ")
    }

    private fun underThousandToWords(num: Int): String {
        val parts = mutableListOf<String>()
        val h = num / 100
        val rem = num % 100
        if (h > 0) parts.add("${ones[h]} hundred")
        if (rem > 0) {
            if (rem < 20) {
                parts.add(ones[rem])
            } else {
                val t = tens[rem / 10]
                val o = ones[rem % 10]
                parts.add(if (o.isNotEmpty()) "$t-$o" else t)
            }
        }
        return parts.joinToString(" ")
    }
}
