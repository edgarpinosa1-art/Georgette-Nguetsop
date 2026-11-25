package com.example.data.vault

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class PromptVaultState(
    val isCustomImported: Boolean = false,
    val fileName: String = "Built-In Kernel v5.3.4-US",
    val charCount: Int = 0,
    val content: String = ""
)

class PromptVaultManager(private val context: Context) {

    private val promptFile: File
        get() {
            val dir = File(context.filesDir, "prompts")
            if (!dir.exists()) dir.mkdirs()
            return File(dir, "god_prompt_v5_3_4.txt")
        }

    suspend fun loadVaultState(): PromptVaultState = withContext(Dispatchers.IO) {
        val file = promptFile
        if (file.exists() && file.length() > 0L) {
            val text = file.readText()
            PromptVaultState(
                isCustomImported = true,
                fileName = "god_prompt_v5_3_4.txt",
                charCount = text.length,
                content = text
            )
        } else {
            val builtIn = BUILT_IN_REFERENCE_SPEC
            PromptVaultState(
                isCustomImported = false,
                fileName = "Built-In v5.3.4-US Kernel Spec",
                charCount = builtIn.length,
                content = builtIn
            )
        }
    }

    suspend fun importFromUri(uri: Uri): PromptVaultState = withContext(Dispatchers.IO) {
        val text = context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader().readText()
        }?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("Selected prompt file is empty or unreadable.")

        promptFile.writeText(text)
        PromptVaultState(
            isCustomImported = true,
            fileName = "god_prompt_v5_3_4.txt",
            charCount = text.length,
            content = text
        )
    }

    suspend fun resetToBuiltIn(): PromptVaultState = withContext(Dispatchers.IO) {
        if (promptFile.exists()) promptFile.delete()
        loadVaultState()
    }

    companion object {
        val BUILT_IN_REFERENCE_SPEC = """
THE MISSING PAGE GOD PROMPT v5.3.4-US (Script-Only S-Tier Engine)
Channel Identity: THE MISSING PAGE — ENGLISH STORIES (@TheMissingPage-US)

1. VOICE & RUNTIME RULE:
- Natural American English oral storytelling, earphone-first clarity.
- 1,000 spoken characters (including spaces & punctuation, excluding bracket markers) = 1 minute.

2. CATEGORY & POV MATRIX:
- General Fiction & Fictionalized Adaptation: 1st-person confessional POV (I/me/my), close-mic [SFX: ...] allowed (NO 'distant', 'faint', 'soft'), Physical Paranoia final question.
- Documented Folklore Research: 1st-person cultural researcher POV, ZERO [SFX: ...], respectful cultural reflection final question.
- True Crime: Neutral chronological Documentary / Forensic Case Analysis, 1 case per Story ID, ZERO [SFX: ...], evidence-based final question.
- Historical: 1st-person immersive historical researcher-guide, ZERO [SFX: ...].
- Alleged Paranormal Experience: 1 permissioned account per Story ID, ZERO [SFX: ...].

3. MANDATORY SIGNATURES & SILENCE:
- Opening Block 1: Hook -> "Turn the page. Find what was left behind." -> [DEAD SILENCE DROP: 1.5s] -> narration.
- Closing Final Block: Resolution -> 1 category-appropriate final question -> "The page is closed. The questions remain." -> [DEAD SILENCE DROP: 1.5s].
- ZERO like/comment/subscribe CTAs.

4. STRICT BANS & 3-STAGE STORY-CRAFT:
- ZERO digits (0-9) in spoken text.
- ZERO spoken out-of-universe disclaimers.
- ZERO AI clichés ("I froze", "I couldn't explain it", "hair on the back of my neck", "my blood ran cold").
- Stage A: Hidden 5-Beat Story Spine.
- Stage B: 2-Half Seamless Prose Generation (for stories >= 4 mins).
- Stage C: Horror Engine & Anthology Memory Rotator with DO NOT REUSE ledger.
        """.trimIndent()
    }
}
