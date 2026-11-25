package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import com.example.ui.components.StudioIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.EngineStatus
import com.example.domain.model.GenerationPhase
import com.example.domain.model.PartAllocationMode
import com.example.domain.model.RuntimeAllocationMode
import com.example.domain.model.StoryCategory
import com.example.ui.components.AuditScorecardStrip
import com.example.ui.components.SegmentedPillToggle
import com.example.ui.components.SpokenBudgetBar
import com.example.ui.components.SyntaxHighlightedScriptConsole
import com.example.ui.components.TactileStepper
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonMuted
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MonoBadgeStyle
import com.example.ui.theme.MutedParchment
import com.example.ui.theme.ObsidianBackground
import com.example.viewmodel.StudioUiState

@Composable
fun StudioScreen(
    uiState: StudioUiState,
    engineStatus: EngineStatus,
    onCategorySelected: (StoryCategory) -> Unit,
    onStoryCountChange: (Int) -> Unit,
    onPartsChange: (Int) -> Unit,
    onPartModeChange: (PartAllocationMode) -> Unit,
    onPerStoryPartChange: (Int, Int) -> Unit,
    onRuntimeMinutesChange: (Int) -> Unit,
    onRuntimeModeChange: (RuntimeAllocationMode) -> Unit,
    onPremiseChange: (String) -> Unit,
    onGenerateClick: () -> Unit,
    onStopClick: () -> Unit,
    onExpandStoryClick: () -> Unit,
    onStoryTabSelected: (Int) -> Unit,
    onOpenEngineTab: () -> Unit,
    onCopyText: (String, String) -> Unit,
    onSaveTxtFile: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val config = uiState.config
    val isGenerating = uiState.generationPhase !is GenerationPhase.Idle &&
        uiState.generationPhase !is GenerationPhase.Completed &&
        uiState.generationPhase !is GenerationPhase.Failed

    val activeEpisode = uiState.activeEpisode
    val activeStory = activeEpisode?.stories?.getOrNull(
        uiState.activeStoryTabIndex.coerceIn(0, (activeEpisode.stories.size - 1).coerceAtLeast(0))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Master Control Panel Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.studio_control_panel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedParchment
                    )
                    Text(
                        text = "GOD PROMPT v5.3.4-US",
                        style = MonoBadgeStyle.copy(fontSize = 10.sp),
                        color = CrimsonPrimary
                    )
                }

                // Category Horizontal Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StoryCategory.entries.forEach { category ->
                        val selected = category == config.category
                        Surface(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("category_chip_${category.name}")
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(enabled = !isGenerating) { onCategorySelected(category) },
                            shape = RoundedCornerShape(100.dp),
                            color = if (selected) CrimsonMuted else ElevatedSlate,
                            border = BorderStroke(
                                1.dp,
                                if (selected) CrimsonPrimary else HairlineBorder
                            )
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 11.5.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = if (selected) BoneWhite else MutedParchment,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Category Lock Summary Strip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBackground,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "POV: ${config.category.povRuleLabel}",
                            style = MonoBadgeStyle.copy(fontSize = 10.sp),
                            color = BoneWhite
                        )
                        Text(
                            text = "AUDIO: ${config.category.sfxRuleLabel} • ${config.category.finalQuestionStyle}",
                            style = MonoBadgeStyle.copy(fontSize = 10.sp),
                            color = MutedParchment
                        )
                    }
                }

                // Steppers Row: Story Count & Parts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TactileStepper(
                        label = "Story Count",
                        value = config.storyCount,
                        min = 1,
                        max = 15,
                        onValueChange = { if (!isGenerating) onStoryCountChange(it) },
                        testTagPrefix = "story_count",
                        modifier = Modifier.weight(1f)
                    )
                    TactileStepper(
                        label = "Parts / Story",
                        value = config.partsPerStory,
                        min = 1,
                        max = 6,
                        onValueChange = { if (!isGenerating) onPartsChange(it) },
                        testTagPrefix = "parts_count",
                        modifier = Modifier.weight(1f)
                    )
                }

                // Part Allocation Mode Toggle
                SegmentedPillToggle(
                    options = PartAllocationMode.entries,
                    selectedOption = config.partAllocationMode,
                    labelProvider = { it.label },
                    onSelect = { if (!isGenerating) onPartModeChange(it) },
                    testTagPrefix = "part_mode"
                )

                // Per-Story Part Map if enabled and multi-story
                AnimatedVisibility(
                    visible = config.partAllocationMode == PartAllocationMode.PER_STORY_MAP && config.storyCount > 1
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (idx in 1..config.storyCount) {
                            TactileStepper(
                                label = "S%02d Parts".format(idx),
                                value = config.partsForStory(idx),
                                min = 1,
                                max = 6,
                                onValueChange = { if (!isGenerating) onPerStoryPartChange(idx, it) },
                                testTagPrefix = "s${idx}_parts",
                                modifier = Modifier.width(138.dp)
                            )
                        }
                    }
                }

                // Runtime Stepper & Mode Toggle
                TactileStepper(
                    label = "Target Runtime",
                    value = config.runtimeMinutes,
                    min = 1,
                    max = 30,
                    suffix = "MIN",
                    onValueChange = { if (!isGenerating) onRuntimeMinutesChange(it) },
                    testTagPrefix = "runtime_mins",
                    modifier = Modifier.fillMaxWidth()
                )

                SegmentedPillToggle(
                    options = RuntimeAllocationMode.entries,
                    selectedOption = config.runtimeAllocationMode,
                    labelProvider = { it.label },
                    onSelect = { if (!isGenerating) onRuntimeModeChange(it) },
                    testTagPrefix = "runtime_mode"
                )

                // Optional Premise Input
                OutlinedTextField(
                    value = config.storyPremise,
                    onValueChange = { if (!isGenerating) onPremiseChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("premise_input"),
                    label = {
                        Text(
                            text = "CASE SEED / PREMISE (OPTIONAL)",
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    placeholder = {
                        Text(
                            text = "e.g., Night-shift bridge tender hears a second bootstep on the catwalk...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedParchment.copy(alpha = 0.7f)
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ElevatedSlate,
                        unfocusedContainerColor = ElevatedSlate,
                        focusedBorderColor = CrimsonPrimary,
                        unfocusedBorderColor = HairlineBorder,
                        focusedTextColor = BoneWhite,
                        unfocusedTextColor = BoneWhite,
                        focusedLabelColor = CrimsonPrimary,
                        unfocusedLabelColor = MutedParchment,
                        cursorColor = CrimsonPrimary
                    )
                )

                // Spoken-Character Budget Bar (Minutes x 1,000)
                SpokenBudgetBar(
                    spokenChars = if (isGenerating || activeEpisode != null) {
                        uiState.liveSpokenChars
                    } else {
                        0
                    },
                    targetChars = uiState.liveTargetChars,
                    isStreaming = isGenerating
                )
            }
        }

        // Primary CTA: Generate or Stop Generation
        if (engineStatus !is EngineStatus.Ready) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenEngineTab),
                shape = RoundedCornerShape(12.dp),
                color = ElevatedSlate,
                border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.65f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "OFFLINE GGUF ENGINE STANDBY",
                            style = MaterialTheme.typography.labelSmall,
                            color = CrimsonPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap to load Qwen3.5-9B or another local .gguf model",
                            style = MaterialTheme.typography.bodyMedium,
                            color = BoneWhite
                        )
                    }
                    Text(
                        text = "ENGINE →",
                        style = MonoBadgeStyle.copy(fontWeight = FontWeight.Bold),
                        color = CrimsonPrimary
                    )
                }
            }
        }

        if (isGenerating) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onStopClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("stop_generation_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonMuted,
                    contentColor = BoneWhite
                ),
                border = BorderStroke(1.dp, CrimsonPrimary)
            ) {
                Icon(
                    imageVector = StudioIcons.Stop,
                    contentDescription = "Stop Generation",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.studio_stop_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        } else {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onGenerateClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .shadow(
                        elevation = 14.dp,
                        shape = RoundedCornerShape(12.dp),
                        ambientColor = CrimsonPrimary,
                        spotColor = CrimsonPrimary
                    )
                    .testTag("generate_script_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = BoneWhite
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Generate Script",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.studio_generate_cta),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Phase Telemetry Strip
        GenerationPhaseBanner(phase = uiState.generationPhase)

        // Audit Scorecard & Auto-Continue Strip (when active episode exists)
        if (activeEpisode != null && activeStory != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.studio_audit_title),
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedParchment
                        )
                        Text(
                            text = activeStory.storyId,
                            style = MonoBadgeStyle,
                            color = EmeraldPass
                        )
                    }

                    if (activeEpisode.stories.size > 1) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeEpisode.stories.forEachIndexed { idx, story ->
                                val selected = idx == uiState.activeStoryTabIndex
                                Surface(
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .clip(RoundedCornerShape(100.dp))
                                        .clickable { onStoryTabSelected(idx) },
                                    shape = RoundedCornerShape(100.dp),
                                    color = if (selected) CrimsonMuted else ElevatedSlate,
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) CrimsonPrimary else HairlineBorder
                                    )
                                ) {
                                    Text(
                                        text = "${story.storyId}: ${story.chapterTitle}",
                                        style = MonoBadgeStyle.copy(fontSize = 10.5.sp),
                                        color = if (selected) BoneWhite else MutedParchment,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    AuditScorecardStrip(report = activeStory.auditReport)

                    if (activeStory.auditReport.spokenCharCount < (activeStory.targetChars * 0.90).toInt()) {
                        OutlinedButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onExpandStoryClick()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("continue_expand_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CrimsonPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CrimsonMuted.copy(alpha = 0.55f),
                                contentColor = BoneWhite
                            )
                        ) {
                            Icon(
                                imageVector = StudioIcons.AutoFixHigh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.studio_continue_cta),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    HorizontalDivider(thickness = 1.dp, color = HairlineBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCopyText("TTS-Ready Script", activeEpisode.ttsReadyExport)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HairlineBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ElevatedSlate,
                                contentColor = BoneWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(StudioIcons.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Copy TTS", style = MonoBadgeStyle.copy(fontSize = 10.5.sp))
                        }

                        OutlinedButton(
                            onClick = {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onSaveTxtFile("missing_page_tts_ready.txt", activeEpisode.ttsReadyExport)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, HairlineBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = ElevatedSlate,
                                contentColor = BoneWhite
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                        ) {
                            Icon(StudioIcons.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(5.dp))
                            Text("Save TTS .txt", style = MonoBadgeStyle.copy(fontSize = 10.5.sp))
                        }
                    }
                }
            }
        }

        // Live Streaming Syntax-Highlighted Console
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.studio_console_title),
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedParchment
                    )
                    if (isGenerating) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(CrimsonPrimary)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "STREAMING",
                                style = MonoBadgeStyle.copy(fontSize = 9.5.sp),
                                color = CrimsonPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 1.dp, color = HairlineBorder)
                Spacer(modifier = Modifier.height(14.dp))

                val displayText = when {
                    activeStory != null && !isGenerating -> activeStory.ttsReadyText
                    uiState.liveConsoleText.isNotBlank() -> uiState.liveConsoleText
                    else -> null
                }

                if (displayText != null) {
                    SyntaxHighlightedScriptConsole(scriptText = displayText)
                } else {
                    Text(
                        text = "Configure your Category, Story Count, and Runtime above. Every script passes through the 3-Stage Story Spine, Anthology Memory Rotator, and God Prompt v5.3.4-US Deterministic Auditor completely offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedParchment
                    )
                }
            }
        }
    }
}

@Composable
private fun GenerationPhaseBanner(phase: GenerationPhase) {
    val label = when (phase) {
        GenerationPhase.Idle -> return
        is GenerationPhase.PlanningSpine -> "STAGE A — ${phase.storyId} (${phase.storyIndex}/${phase.totalStories}): Crafting Hidden 5-Beat Story Spine..."
        is GenerationPhase.WritingPartA -> "STAGE B (1/2) — ${phase.storyId} (${phase.storyIndex}/${phase.totalStories}): Streaming Setup & Escalation..."
        is GenerationPhase.WritingPartB -> "STAGE B (2/2) — ${phase.storyId} (${phase.storyIndex}/${phase.totalStories}): Streaming Climax & Outro..."
        is GenerationPhase.WritingSinglePass -> "STAGE B — ${phase.storyId} (${phase.storyIndex}/${phase.totalStories}): Streaming Narrative..."
        is GenerationPhase.ExpandingStory -> "EXPANDING ${phase.storyId}: Deepening sensory tension to hit runtime budget..."
        is GenerationPhase.Auditing -> "AUDITING ${phase.storyId}: Running v5.3.4-US Digit-to-Word & Signature Enforcer..."
        GenerationPhase.Completed -> "COMPLETE — Script audited, sanitized, and saved to local Archive."
        is GenerationPhase.Failed -> "ERROR — ${phase.message}"
    }

    val borderColor = when (phase) {
        is GenerationPhase.Failed -> CrimsonPrimary
        GenerationPhase.Completed -> EmeraldPass.copy(alpha = 0.6f)
        else -> HairlineBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ElevatedSlate,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (phase !is GenerationPhase.Completed && phase !is GenerationPhase.Failed) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = CrimsonPrimary
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = label,
                style = MonoBadgeStyle.copy(fontSize = 10.5.sp),
                color = if (phase is GenerationPhase.Completed) EmeraldPass else BoneWhite
            )
        }
    }
}
