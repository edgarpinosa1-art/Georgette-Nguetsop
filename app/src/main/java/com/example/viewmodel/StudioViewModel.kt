package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.engine.LocalGgufEngine
import com.example.data.engine.RamTelemetryProvider
import com.example.data.export.TxtExporter
import com.example.data.local.AppDatabase
import com.example.data.preferences.PersistedModelRef
import com.example.data.preferences.StudioPreferencesRepository
import com.example.data.repository.EpisodeRepository
import com.example.data.vault.PromptVaultManager
import com.example.data.vault.PromptVaultState
import com.example.domain.auditor.GodPromptAuditor
import com.example.domain.model.AnthologyLedger
import com.example.domain.model.AuditReport
import com.example.domain.model.EngineStatus
import com.example.domain.model.EpisodeResult
import com.example.domain.model.GeneratedStory
import com.example.domain.model.GenerationPhase
import com.example.domain.model.InferenceSettings
import com.example.domain.model.PartAllocationMode
import com.example.domain.model.RamTelemetry
import com.example.domain.model.RuntimeAllocationMode
import com.example.domain.model.StoryCategory
import com.example.domain.model.StoryConfig
import com.example.domain.prompt.PromptKernelCompiler
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class StudioUiState(
    val config: StoryConfig = StoryConfig(),
    val generationPhase: GenerationPhase = GenerationPhase.Idle,
    val liveConsoleText: String = "",
    val liveSpokenChars: Int = 0,
    val liveTargetChars: Int = 6000,
    val activeEpisode: EpisodeResult? = null,
    val activeStoryTabIndex: Int = 0,
    val ledger: AnthologyLedger = AnthologyLedger(),
    val ramTelemetry: RamTelemetry = RamTelemetry(),
    val vaultState: PromptVaultState = PromptVaultState(),
    val persistedModelRef: PersistedModelRef = PersistedModelRef(),
    val isPreparingModel: Boolean = false
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val prefsRepo = StudioPreferencesRepository(application)
    private val episodeRepo = EpisodeRepository(AppDatabase.getInstance(application).episodeDao())
    private val vaultManager = PromptVaultManager(application)
    private val ggufEngine = LocalGgufEngine(application)

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    val engineStatus: StateFlow<EngineStatus> = ggufEngine.engineStatus
    val stagingProgress: StateFlow<Float?> = ggufEngine.stagingProgress

    val inferenceSettings: StateFlow<InferenceSettings> = prefsRepo.inferenceSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, InferenceSettings())

    val savedEpisodes: StateFlow<List<EpisodeResult>> = episodeRepo.allEpisodes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _toastEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    private var generationJob: Job? = null

    init {
        viewModelScope.launch {
            val vault = vaultManager.loadVaultState()
            _uiState.update { it.copy(vaultState = vault) }
        }
        viewModelScope.launch {
            prefsRepo.modelRefFlow.collect { ref ->
                _uiState.update { it.copy(persistedModelRef = ref) }
            }
        }
        viewModelScope.launch {
            while (isActive) {
                val telemetry = RamTelemetryProvider.readTelemetry(getApplication())
                _uiState.update { it.copy(ramTelemetry = telemetry) }
                delay(2500L)
            }
        }
    }

    // --- Admin Control Panel Updates ---

    fun updateCategory(category: StoryCategory) {
        _uiState.update { state ->
            val updatedConfig = state.config.copy(category = category)
            state.copy(
                config = updatedConfig,
                liveTargetChars = updatedConfig.totalTargetChars()
            )
        }
    }

    fun updateStoryCount(count: Int) {
        val clamped = count.coerceIn(1, 15)
        _uiState.update { state ->
            val updatedConfig = state.config.copy(storyCount = clamped)
            state.copy(
                config = updatedConfig,
                liveTargetChars = updatedConfig.totalTargetChars()
            )
        }
    }

    fun updatePartsPerStory(parts: Int) {
        val clamped = parts.coerceIn(1, 6)
        _uiState.update { state ->
            state.copy(config = state.config.copy(partsPerStory = clamped))
        }
    }

    fun updatePartAllocationMode(mode: PartAllocationMode) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(partAllocationMode = mode))
        }
    }

    fun updatePerStoryPart(storyIndex: Int, parts: Int) {
        val clamped = parts.coerceIn(1, 6)
        _uiState.update { state ->
            val newMap = state.config.perStoryPartMap.toMutableMap().apply {
                put(storyIndex, clamped)
            }
            state.copy(config = state.config.copy(perStoryPartMap = newMap))
        }
    }

    fun updateRuntimeMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 30)
        _uiState.update { state ->
            val updatedConfig = state.config.copy(runtimeMinutes = clamped)
            state.copy(
                config = updatedConfig,
                liveTargetChars = updatedConfig.totalTargetChars()
            )
        }
    }

    fun updateRuntimeAllocationMode(mode: RuntimeAllocationMode) {
        _uiState.update { state ->
            val updatedConfig = state.config.copy(runtimeAllocationMode = mode)
            state.copy(
                config = updatedConfig,
                liveTargetChars = updatedConfig.totalTargetChars()
            )
        }
    }

    fun updatePremise(premise: String) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(storyPremise = premise))
        }
    }

    fun selectActiveStoryTab(index: Int) {
        _uiState.update { it.copy(activeStoryTabIndex = index) }
    }

    // --- 3-Stage Offline Generation Pipeline ---

    fun generateEpisode() {
        if (generationJob?.isActive == true) return
        val status = engineStatus.value
        if (status !is EngineStatus.Ready) {
            _toastEvents.tryEmit("Load a local .gguf model in Engine & Vault before generating.")
            return
        }

        val config = _uiState.value.config
        val settings = inferenceSettings.value
        val vaultExcerpt = if (_uiState.value.vaultState.isCustomImported) {
            _uiState.value.vaultState.content.take(400)
        } else null

        val systemKernel = PromptKernelCompiler.compileSystemKernel(config, vaultExcerpt)

        generationJob = viewModelScope.launch {
            val generatedStories = mutableListOf<GeneratedStory>()
            var workingLedger = AnthologyLedger()
            val consoleAggregator = StringBuilder()

            _uiState.update {
                it.copy(
                    liveConsoleText = "",
                    liveSpokenChars = 0,
                    liveTargetChars = config.totalTargetChars(),
                    activeEpisode = null,
                    activeStoryTabIndex = 0,
                    ledger = workingLedger
                )
            }

            try {
                for (index in 1..config.storyCount) {
                    val storyId = "S%02d".format(index)
                    val targetMins = config.targetMinutesForStory(index)
                    val targetChars = config.targetCharsForStory(index)

                    // Stage C: Assign distinct Horror Mechanism, Hook Type & Ending Type
                    val (mechanism, hookType, endingType) = PromptKernelCompiler.assignStoryBlueprint(
                        storyIndex = index,
                        ledger = workingLedger
                    )

                    // Stage A: Hidden 5-Beat Story Spine (kept out of spoken output)
                    _uiState.update {
                        it.copy(generationPhase = GenerationPhase.PlanningSpine(storyId, index, config.storyCount))
                    }
                    val spinePrompt = PromptKernelCompiler.compileSpinePrompt(
                        storyId = storyId,
                        storyIndex = index,
                        config = config,
                        mechanism = mechanism,
                        hookType = hookType,
                        endingType = endingType,
                        ledger = workingLedger
                    )

                    val spineBuffer = StringBuilder()
                    ggufEngine.streamWithContext(
                        systemPrompt = systemKernel,
                        contextBlock = "STAGE A INTERNAL OUTLINE",
                        userPrompt = spinePrompt,
                        settings = settings,
                        maxTokensOverride = 360
                    ).collect { delta ->
                        spineBuffer.append(delta)
                    }

                    val cleanSpine = spineBuffer.toString()
                        .replace(Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE), "")
                        .trim()
                    val chapterTitle = PromptKernelCompiler.extractChapterTitle(cleanSpine, index)

                    // Stage B: Prose Generation (2-Half Seamless for >= 4 mins, Single-Pass for < 4 mins)
                    val rawProse: String = if (targetMins >= 4) {
                        _uiState.update {
                            it.copy(generationPhase = GenerationPhase.WritingPartA(storyId, index, config.storyCount))
                        }
                        val partABuffer = StringBuilder()
                        val partAPrompt = PromptKernelCompiler.compilePartAPrompt(
                            storyId = storyId,
                            storyIndex = index,
                            config = config,
                            spine = cleanSpine,
                            hookType = hookType,
                            mechanism = mechanism
                        )

                        ggufEngine.streamWithContext(
                            systemPrompt = systemKernel,
                            contextBlock = "STORY $storyId — PART A",
                            userPrompt = partAPrompt,
                            settings = settings
                        ).collect { delta ->
                            partABuffer.append(delta)
                            updateLiveStreamingPreview(
                                priorText = consoleAggregator.toString(),
                                currentDeltaText = partABuffer.toString()
                            )
                        }

                        val sanitizedPartA = partABuffer.toString()
                            .replace(Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE), "")
                            .trim()
                        val tailAnchor = sanitizedPartA.takeLast(400)

                        _uiState.update {
                            it.copy(generationPhase = GenerationPhase.WritingPartB(storyId, index, config.storyCount))
                        }
                        val partBBuffer = StringBuilder()
                        val partBPrompt = PromptKernelCompiler.compilePartBPrompt(
                            storyId = storyId,
                            storyIndex = index,
                            config = config,
                            spine = cleanSpine,
                            endingType = endingType,
                            partATail = tailAnchor
                        )

                        ggufEngine.streamWithContext(
                            systemPrompt = systemKernel,
                            contextBlock = "STORY $storyId — PART B CONTINUATION",
                            userPrompt = partBPrompt,
                            settings = settings
                        ).collect { delta ->
                            partBBuffer.append(delta)
                            updateLiveStreamingPreview(
                                priorText = consoleAggregator.toString(),
                                currentDeltaText = "$sanitizedPartA\n\n${partBBuffer}"
                            )
                        }

                        "$sanitizedPartA\n\n${partBBuffer}"
                    } else {
                        _uiState.update {
                            it.copy(generationPhase = GenerationPhase.WritingSinglePass(storyId, index, config.storyCount))
                        }
                        val singleBuffer = StringBuilder()
                        val singlePrompt = PromptKernelCompiler.compileSinglePassProsePrompt(
                            storyId = storyId,
                            storyIndex = index,
                            config = config,
                            spine = cleanSpine,
                            mechanism = mechanism,
                            hookType = hookType,
                            endingType = endingType
                        )
                        ggufEngine.streamWithContext(
                            systemPrompt = systemKernel,
                            contextBlock = "STORY $storyId — FULL PASS",
                            userPrompt = singlePrompt,
                            settings = settings
                        ).collect { delta ->
                            singleBuffer.append(delta)
                            updateLiveStreamingPreview(
                                priorText = consoleAggregator.toString(),
                                currentDeltaText = singleBuffer.toString()
                            )
                        }
                        singleBuffer.toString()
                    }

                    // Deterministic GodPromptAuditor Pass
                    _uiState.update { it.copy(generationPhase = GenerationPhase.Auditing(storyId)) }
                    val auditOutcome = GodPromptAuditor.auditAndSanitize(
                        rawText = rawProse,
                        category = config.category,
                        targetCharCount = targetChars,
                        existingLedger = workingLedger
                    )

                    // Update Anthology Memory Ledger
                    val (newNames, newVehicles, newProps) = GodPromptAuditor.extractNewEntities(auditOutcome.cleanedText)
                    workingLedger = workingLedger.copy(
                        usedNames = workingLedger.usedNames + newNames,
                        usedVehicles = workingLedger.usedVehicles + newVehicles,
                        usedProps = workingLedger.usedProps + newProps,
                        usedMechanisms = workingLedger.usedMechanisms + mechanism
                    )

                    val story = GeneratedStory(
                        storyId = storyId,
                        chapterTitle = chapterTitle,
                        category = config.category,
                        mechanism = mechanism,
                        openingHookType = hookType,
                        endingType = endingType,
                        hiddenSpine = cleanSpine,
                        ttsReadyText = auditOutcome.cleanedText,
                        targetChars = targetChars,
                        auditReport = auditOutcome.report
                    )
                    generatedStories.add(story)

                    if (consoleAggregator.isNotEmpty()) consoleAggregator.append("\n\n")
                    consoleAggregator.append(auditOutcome.cleanedText)
                }

                val episodeTitle = if (generatedStories.size == 1) {
                    generatedStories.first().chapterTitle
                } else {
                    "${config.category.shortLabel} Anthology (${generatedStories.size} Stories)"
                }

                val ttsExport = PromptKernelCompiler.buildTtsReadyExport(generatedStories)
                val masterExport = PromptKernelCompiler.buildFullMasterExport(
                    episodeTitle = episodeTitle,
                    stories = generatedStories,
                    config = config
                )
                val totalSpoken = generatedStories.sumOf { it.auditReport.spokenCharCount }
                val totalTarget = generatedStories.sumOf { it.targetChars }

                val unsavedEpisode = EpisodeResult(
                    title = episodeTitle,
                    category = config.category,
                    createdAtMillis = System.currentTimeMillis(),
                    stories = generatedStories,
                    ttsReadyExport = ttsExport,
                    fullMasterExport = masterExport,
                    totalSpokenChars = totalSpoken,
                    totalTargetChars = totalTarget,
                    modelName = status.modelName
                )

                val savedId = episodeRepo.saveEpisode(unsavedEpisode)
                val savedEpisode = unsavedEpisode.copy(id = savedId)

                _uiState.update {
                    it.copy(
                        generationPhase = GenerationPhase.Completed,
                        liveConsoleText = ttsExport,
                        liveSpokenChars = totalSpoken,
                        liveTargetChars = totalTarget,
                        activeEpisode = savedEpisode,
                        ledger = workingLedger
                    )
                }
                _toastEvents.tryEmit("Episode audited & archived locally.")
            } catch (t: Throwable) {
                val isCancelled = t is kotlinx.coroutines.CancellationException
                _uiState.update {
                    it.copy(
                        generationPhase = if (isCancelled) GenerationPhase.Idle
                        else GenerationPhase.Failed(t.message ?: "Generation failed.")
                    )
                }
                if (!isCancelled) {
                    _toastEvents.tryEmit(t.message ?: "Generation interrupted.")
                }
            }
        }
    }

    fun stopGeneration() {
        ggufEngine.cancelGeneration()
        generationJob?.cancel()
        generationJob = null
        _uiState.update { it.copy(generationPhase = GenerationPhase.Idle) }
        _toastEvents.tryEmit("Generation stopped.")
    }

    fun expandActiveStory() {
        val episode = _uiState.value.activeEpisode ?: return
        val storyIdx = _uiState.value.activeStoryTabIndex.coerceIn(0, episode.stories.lastIndex)
        val story = episode.stories[storyIdx]
        val missing = (story.targetChars - story.auditReport.spokenCharCount).coerceAtLeast(600)

        val status = engineStatus.value
        if (status !is EngineStatus.Ready) {
            _toastEvents.tryEmit("Load a local .gguf model to expand this story.")
            return
        }
        if (generationJob?.isActive == true) return

        val config = _uiState.value.config.copy(category = story.category)
        val systemKernel = PromptKernelCompiler.compileSystemKernel(config)
        val expandPrompt = PromptKernelCompiler.compileExpandStoryPrompt(story, missing)

        generationJob = viewModelScope.launch {
            _uiState.update { it.copy(generationPhase = GenerationPhase.ExpandingStory(story.storyId)) }
            try {
                val buffer = StringBuilder()
                ggufEngine.streamWithContext(
                    systemPrompt = systemKernel,
                    contextBlock = "EXPAND ${story.storyId}",
                    userPrompt = expandPrompt,
                    settings = inferenceSettings.value
                ).collect { delta ->
                    buffer.append(delta)
                    val preview = buffer.toString()
                        .replace(Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE), "")
                    _uiState.update {
                        it.copy(
                            liveConsoleText = preview,
                            liveSpokenChars = GodPromptAuditor.countSpokenCharacters(preview)
                        )
                    }
                }

                _uiState.update { it.copy(generationPhase = GenerationPhase.Auditing(story.storyId)) }
                val auditOutcome = GodPromptAuditor.auditAndSanitize(
                    rawText = buffer.toString(),
                    category = story.category,
                    targetCharCount = story.targetChars,
                    existingLedger = AnthologyLedger()
                )

                val updatedStory = story.copy(
                    ttsReadyText = auditOutcome.cleanedText,
                    auditReport = auditOutcome.report
                )
                val updatedStories = episode.stories.toMutableList().apply { set(storyIdx, updatedStory) }
                val ttsExport = PromptKernelCompiler.buildTtsReadyExport(updatedStories)
                val masterExport = PromptKernelCompiler.buildFullMasterExport(
                    episodeTitle = episode.title,
                    stories = updatedStories,
                    config = config
                )
                val updatedEpisode = episode.copy(
                    stories = updatedStories,
                    ttsReadyExport = ttsExport,
                    fullMasterExport = masterExport,
                    totalSpokenChars = updatedStories.sumOf { it.auditReport.spokenCharCount }
                )
                episodeRepo.saveEpisode(updatedEpisode)

                _uiState.update {
                    it.copy(
                        generationPhase = GenerationPhase.Completed,
                        activeEpisode = updatedEpisode,
                        liveConsoleText = updatedStory.ttsReadyText,
                        liveSpokenChars = updatedStory.auditReport.spokenCharCount,
                        liveTargetChars = updatedStory.targetChars
                    )
                }
                _toastEvents.tryEmit("${story.storyId} expanded & re-audited.")
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(generationPhase = GenerationPhase.Failed(t.message ?: "Expansion failed."))
                }
            }
        }
    }

    private fun updateLiveStreamingPreview(priorText: String, currentDeltaText: String) {
        val cleanedLive = currentDeltaText
            .replace(Regex("""<think>[\s\S]*?</think>""", RegexOption.IGNORE_CASE), "")
            .replace(Regex("""<think>[\s\S]*$""", RegexOption.IGNORE_CASE), "")
            .trimStart()
        val combined = if (priorText.isBlank()) cleanedLive else "$priorText\n\n$cleanedLive"
        _uiState.update {
            it.copy(
                liveConsoleText = combined,
                liveSpokenChars = GodPromptAuditor.countSpokenCharacters(combined)
            )
        }
    }

    // --- Archive & Export ---

    fun copyToClipboard(label: String, text: String) {
        TxtExporter.copyToClipboard(getApplication(), label, text)
        _toastEvents.tryEmit("$label copied to clipboard.")
    }

    fun exportTxtToUri(uri: Uri, text: String) {
        viewModelScope.launch {
            TxtExporter.writeTextToUri(getApplication(), uri, text)
                .onSuccess { _toastEvents.tryEmit("Saved .txt export successfully.") }
                .onFailure { _toastEvents.tryEmit(it.message ?: "Failed to save .txt file.") }
        }
    }

    fun deleteEpisode(id: Long) {
        viewModelScope.launch {
            episodeRepo.deleteEpisode(id)
            _toastEvents.tryEmit("Episode removed from local archive.")
        }
    }

    // --- GGUF Model & Inference Settings ---

    fun pickAndPrepareModel(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isPreparingModel = true) }
            ggufEngine.prepareModelFromUri(uri)
                .onSuccess { prepared ->
                    prefsRepo.saveModelRef(
                        path = prepared.path,
                        displayName = prepared.displayName,
                        sizeBytes = prepared.sizeBytes
                    )
                    _uiState.update { it.copy(isPreparingModel = false) }
                    loadPersistedModel(prepared.path, prepared.displayName)
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isPreparingModel = false) }
                    _toastEvents.tryEmit(err.message ?: "Failed to prepare .gguf model.")
                }
        }
    }

    fun loadPersistedModel(path: String? = null, displayName: String? = null) {
        val ref = _uiState.value.persistedModelRef
        val targetPath = path ?: ref.path
        val targetName = displayName ?: ref.displayName
        if (targetPath.isBlank()) {
            _toastEvents.tryEmit("Select a local .gguf file first.")
            return
        }
        viewModelScope.launch {
            ggufEngine.loadModel(targetPath, targetName, inferenceSettings.value)
                .onSuccess { _toastEvents.tryEmit("Loaded $targetName into memory.") }
                .onFailure { err -> _toastEvents.tryEmit(err.message ?: "Model load failed.") }
        }
    }

    fun unloadModel() {
        viewModelScope.launch {
            ggufEngine.unloadModel()
            _toastEvents.tryEmit("Model unloaded from memory.")
        }
    }

    fun updateInferenceSettings(transform: (InferenceSettings) -> InferenceSettings) {
        viewModelScope.launch {
            val updated = transform(inferenceSettings.value)
            prefsRepo.updateInferenceSettings(updated)
        }
    }

    // --- God Prompt Vault ---

    fun importGodPrompt(uri: Uri) {
        viewModelScope.launch {
            runCatching { vaultManager.importFromUri(uri) }
                .onSuccess { vault ->
                    _uiState.update { it.copy(vaultState = vault) }
                    _toastEvents.tryEmit("God Prompt v5.3.4-US imported to Vault.")
                }
                .onFailure { err ->
                    _toastEvents.tryEmit(err.message ?: "Failed to import God Prompt .txt.")
                }
        }
    }

    fun resetGodPromptToBuiltIn() {
        viewModelScope.launch {
            val vault = vaultManager.resetToBuiltIn()
            _uiState.update { it.copy(vaultState = vault) }
            _toastEvents.tryEmit("Restored built-in v5.3.4-US kernel spec.")
        }
    }

    override fun onCleared() {
        super.onCleared()
        ggufEngine.cancelGeneration()
    }
}
