package com.example.domain.model

enum class StoryCategory(
    val displayName: String,
    val shortLabel: String,
    val requiresFirstPerson: Boolean,
    val allowsSfx: Boolean,
    val povRuleLabel: String,
    val sfxRuleLabel: String,
    val finalQuestionStyle: String,
    val publicDisclosure: String
) {
    GENERAL_FICTION(
        displayName = "General Fiction",
        shortLabel = "Fiction",
        requiresFirstPerson = true,
        allowsSfx = true,
        povRuleLabel = "1st-Person Confessional (I/me/my)",
        sfxRuleLabel = "Close-Mic [SFX: ...] Allowed",
        finalQuestionStyle = "Physical Paranoia final question",
        publicDisclosure = "This episode presents original fictional audio drama created solely for narrative entertainment."
    ),
    FICTIONALIZED_ADAPTATION(
        displayName = "Fictionalized Adaptation",
        shortLabel = "Adaptation",
        requiresFirstPerson = true,
        allowsSfx = true,
        povRuleLabel = "1st-Person Confessional (I/me/my)",
        sfxRuleLabel = "Close-Mic [SFX: ...] Allowed",
        finalQuestionStyle = "Physical Paranoia final question",
        publicDisclosure = "This episode is a fictionalized dramatic adaptation inspired by real-world themes and atmospheric lore."
    ),
    DOCUMENTED_FOLKLORE(
        displayName = "Documented Folklore Research",
        shortLabel = "Folklore",
        requiresFirstPerson = true,
        allowsSfx = false,
        povRuleLabel = "1st-Person Cultural Researcher",
        sfxRuleLabel = "ZERO [SFX: ...] Tags",
        finalQuestionStyle = "Respectful cultural reflection final question",
        publicDisclosure = "This episode explores documented cultural folklore and regional oral traditions from a research perspective."
    ),
    TRUE_CRIME(
        displayName = "True Crime",
        shortLabel = "True Crime",
        requiresFirstPerson = false,
        allowsSfx = false,
        povRuleLabel = "Neutral Forensic Documentary (1 case/ID)",
        sfxRuleLabel = "ZERO [SFX: ...] Tags",
        finalQuestionStyle = "Evidence-based inquiry final question",
        publicDisclosure = "This episode examines documented case records in a neutral, chronological forensic documentary format."
    ),
    HISTORICAL(
        displayName = "Historical",
        shortLabel = "Historical",
        requiresFirstPerson = true,
        allowsSfx = false,
        povRuleLabel = "1st-Person Immersive Researcher-Guide",
        sfxRuleLabel = "ZERO [SFX: ...] Tags",
        finalQuestionStyle = "Historical reflection final question",
        publicDisclosure = "This episode presents an immersive historical reconstruction grounded in archival chronology."
    ),
    ALLEGED_PARANORMAL(
        displayName = "Alleged Paranormal Experience",
        shortLabel = "Paranormal",
        requiresFirstPerson = true,
        allowsSfx = false,
        povRuleLabel = "1 Permissioned Witness Account/ID",
        sfxRuleLabel = "ZERO [SFX: ...] Tags",
        finalQuestionStyle = "Open observational inquiry final question",
        publicDisclosure = "This episode recounts an alleged first-hand account shared as reported without independent verification."
    )
}

enum class RuntimeAllocationMode(val label: String) {
    PER_STORY("Per-Story"),
    TOTAL_EPISODE("Total Episode")
}

enum class PartAllocationMode(val label: String) {
    SHARED_PARTS("Shared Parts"),
    PER_STORY_MAP("Per-Story Map")
}

enum class HorrorMechanism(val label: String, val guidance: String) {
    PSYCHOLOGICAL_IDENTITY_DOUBT(
        "Psychological / Identity Doubt",
        "The narrator's own memory, records, or perception contradicts physical evidence left behind."
    ),
    PHYSICAL_ENTRAPMENT(
        "Physical Entrapment",
        "Architectural confinement, sealed routes, failed exits, and rising physical pressure."
    ),
    CURSED_OBJECT_RULE_VIOLATION(
        "Cursed Object / Rule Violation",
        "A concrete, irreversible rule or inherited item whose breach triggers escalating consequences."
    ),
    RELATIONAL_FAMILY_BETRAYAL(
        "Relational / Family Betrayal",
        "Someone trusted or related concealed a deliberate bargain, substitution, or cover-up."
    ),
    ENVIRONMENTAL_SPATIAL_ANOMALY(
        "Environmental / Spatial Anomaly",
        "Subtle, measurable shifts in room dimensions, acoustics, weather, or mapped geography."
    ),
    BODY_SENSORY_DISTORTION(
        "Body / Sensory Distortion",
        "Hyper-specific physical sensations—temperature drop, metallic taste, cadence mismatch, phantom weight."
    )
}

enum class OpeningHookType(val label: String, val guidance: String) {
    CONFESSIONAL_ADMISSION(
        "Confessional Admission",
        "Open immediately with a blunt admission of what the narrator hid, signed, or failed to report."
    ),
    IN_MEDIA_RES_CONSEQUENCE(
        "In-Media-Res Consequence",
        "Open inside the immediate physical aftermath of an irreversible choice before rewinding to the trigger."
    ),
    DIRECT_WARNING(
        "Direct Warning",
        "Open by addressing the listener directly with a concrete, verifiable detail they can check right now."
    )
}

enum class EndingType(val label: String, val guidance: String) {
    AMBIGUOUS_RESIDUE(
        "Ambiguous Residue",
        "The immediate danger passes, but one physical artifact remains out of place with no rational explanation."
    ),
    BITTERSWEET_SURVIVAL(
        "Bittersweet Survival",
        "The narrator escapes with proof or clarity, but loses a relationship, home, or normal certainty forever."
    ),
    TRAGIC_COST(
        "Tragic Cost",
        "The truth arrives too late to undo what was surrendered, leaving quiet dread."
    ),
    UNRESOLVED_DISCOVERY(
        "Unresolved Discovery",
        "A final newly uncovered log, receipt, or recording reframes everything that happened before."
    )
}

data class StoryConfig(
    val category: StoryCategory = StoryCategory.GENERAL_FICTION,
    val storyCount: Int = 1,
    val partsPerStory: Int = 2,
    val partAllocationMode: PartAllocationMode = PartAllocationMode.SHARED_PARTS,
    val perStoryPartMap: Map<Int, Int> = emptyMap(),
    val runtimeMinutes: Int = 6,
    val runtimeAllocationMode: RuntimeAllocationMode = RuntimeAllocationMode.PER_STORY,
    val storyPremise: String = ""
) {
    fun partsForStory(storyIndex: Int): Int {
        return if (partAllocationMode == PartAllocationMode.PER_STORY_MAP) {
            (perStoryPartMap[storyIndex] ?: partsPerStory).coerceIn(1, 6)
        } else {
            partsPerStory.coerceIn(1, 6)
        }
    }

    fun targetMinutesForStory(storyIndex: Int): Int {
        return if (runtimeAllocationMode == RuntimeAllocationMode.PER_STORY) {
            runtimeMinutes.coerceIn(1, 30)
        } else {
            val base = (runtimeMinutes / storyCount.coerceAtLeast(1)).coerceAtLeast(1)
            val remainder = if (storyCount > 0) runtimeMinutes % storyCount else 0
            if (storyIndex <= remainder && runtimeMinutes >= storyCount) base + 1 else base
        }
    }

    fun targetCharsForStory(storyIndex: Int): Int = targetMinutesForStory(storyIndex) * 1000

    fun totalTargetChars(): Int = (1..storyCount).sumOf { targetCharsForStory(it) }
}

data class InferenceSettings(
    val contextLength: Int = 6144,
    val temperature: Float = 0.70f,
    val topP: Float = 0.80f,
    val topK: Int = 20,
    val repeatPenalty: Float = 1.10f,
    val threads: Int = 4,
    val useMmap: Boolean = true
)

data class AuditReport(
    val zeroDigitsPassed: Boolean = false,
    val brandLockPassed: Boolean = false,
    val povLockPassed: Boolean = false,
    val sfxValidPassed: Boolean = false,
    val engineUniquePassed: Boolean = true,
    val spokenCharCount: Int = 0,
    val targetCharCount: Int = 0,
    val detectedCliches: List<String> = emptyList(),
    val repairsApplied: List<String> = emptyList()
) {
    val allCoreChecksPassed: Boolean
        get() = zeroDigitsPassed && brandLockPassed && povLockPassed && sfxValidPassed && engineUniquePassed
}

data class AnthologyLedger(
    val usedNames: Set<String> = emptySet(),
    val usedVehicles: Set<String> = emptySet(),
    val usedProps: Set<String> = emptySet(),
    val usedMechanisms: List<HorrorMechanism> = emptyList()
)

data class GeneratedStory(
    val storyId: String,
    val chapterTitle: String,
    val category: StoryCategory,
    val mechanism: HorrorMechanism,
    val openingHookType: OpeningHookType,
    val endingType: EndingType,
    val hiddenSpine: String,
    val ttsReadyText: String,
    val targetChars: Int,
    val auditReport: AuditReport
)

data class EpisodeResult(
    val id: Long = 0,
    val title: String,
    val category: StoryCategory,
    val createdAtMillis: Long,
    val stories: List<GeneratedStory>,
    val ttsReadyExport: String,
    val fullMasterExport: String,
    val totalSpokenChars: Int,
    val totalTargetChars: Int,
    val modelName: String
)

data class RamTelemetry(
    val availableMb: Long = 0L,
    val totalMb: Long = 0L,
    val lowMemory: Boolean = false
)

sealed interface EngineStatus {
    data object Unloaded : EngineStatus
    data class Loading(val modelName: String) : EngineStatus
    data class Ready(val modelName: String, val modelPath: String, val sizeBytes: Long) : EngineStatus
    data class Error(val message: String) : EngineStatus
}

sealed interface GenerationPhase {
    data object Idle : GenerationPhase
    data class PlanningSpine(val storyId: String, val storyIndex: Int, val totalStories: Int) : GenerationPhase
    data class WritingPartA(val storyId: String, val storyIndex: Int, val totalStories: Int) : GenerationPhase
    data class WritingPartB(val storyId: String, val storyIndex: Int, val totalStories: Int) : GenerationPhase
    data class WritingSinglePass(val storyId: String, val storyIndex: Int, val totalStories: Int) : GenerationPhase
    data class ExpandingStory(val storyId: String) : GenerationPhase
    data class Auditing(val storyId: String) : GenerationPhase
    data object Completed : GenerationPhase
    data class Failed(val message: String) : GenerationPhase
}
