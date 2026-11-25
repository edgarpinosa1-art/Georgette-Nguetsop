package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import com.example.ui.components.StudioIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.EpisodeResult
import com.example.ui.components.AuditScorecardStrip
import com.example.ui.components.MissingPageLogo
import com.example.ui.components.SyntaxHighlightedScriptConsole
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonMuted
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldMuted
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MonoBadgeStyle
import com.example.ui.theme.MutedParchment
import com.example.ui.theme.ObsidianBackground
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ArchiveScreen(
    episodes: List<EpisodeResult>,
    onCopyText: (label: String, text: String) -> Unit,
    onSaveTxtFile: (suggestedFileName: String, text: String) -> Unit,
    onDeleteEpisode: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedEpisode by remember { mutableStateOf<EpisodeResult?>(null) }

    val filteredEpisodes = remember(episodes, searchQuery) {
        if (searchQuery.isBlank()) {
            episodes
        } else {
            val q = searchQuery.trim().lowercase()
            episodes.filter { ep ->
                ep.title.lowercase().contains(q) ||
                    ep.category.displayName.lowercase().contains(q) ||
                    ep.stories.any {
                        it.chapterTitle.lowercase().contains(q) ||
                            it.ttsReadyText.lowercase().contains(q)
                    }
            }
        }
    }

    val activeDetail = selectedEpisode?.let { current ->
        episodes.firstOrNull { it.id == current.id } ?: current
    }

    if (activeDetail != null) {
        EpisodeDetailView(
            episode = activeDetail,
            onBack = { selectedEpisode = null },
            onCopyText = onCopyText,
            onSaveTxtFile = onSaveTxtFile,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.archive_title),
                style = MaterialTheme.typography.labelSmall,
                color = MutedParchment
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("archive_search_input"),
                placeholder = {
                    Text(
                        text = "Search titles, categories, or narrative cues...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedParchment
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search archive",
                        tint = MutedParchment
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear search",
                                tint = MutedParchment
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = ElevatedSlate,
                    unfocusedContainerColor = ElevatedSlate,
                    focusedBorderColor = CrimsonPrimary,
                    unfocusedBorderColor = HairlineBorder,
                    focusedTextColor = BoneWhite,
                    unfocusedTextColor = BoneWhite,
                    cursorColor = CrimsonPrimary
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (filteredEpisodes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        MissingPageLogo(size = 56.dp)
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (searchQuery.isBlank()) {
                                stringResource(R.string.archive_empty_title)
                            } else {
                                "No Matching Episodes"
                            },
                            style = MaterialTheme.typography.headlineMedium,
                            color = BoneWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(R.string.archive_empty_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedParchment
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredEpisodes, key = { it.id }) { episode ->
                        EpisodeArchiveCard(
                            episode = episode,
                            onClick = { selectedEpisode = episode },
                            onDelete = { onDeleteEpisode(episode.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EpisodeArchiveCard(
    episode: EpisodeResult,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US) }
    val formatter = remember { NumberFormat.getIntegerInstance(Locale.US) }
    val allPassed = episode.stories.all { it.auditReport.allCoreChecksPassed }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("archive_card_${episode.id}")
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = CharcoalSurface,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CrimsonMuted,
                            border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = episode.category.shortLabel.uppercase(),
                                style = MonoBadgeStyle.copy(fontSize = 9.5.sp),
                                color = BoneWhite,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (allPassed) EmeraldMuted else ElevatedSlate,
                            border = BorderStroke(
                                1.dp,
                                if (allPassed) EmeraldPass.copy(alpha = 0.5f) else HairlineBorder
                            )
                        ) {
                            Text(
                                text = if (allPassed) "S-TIER PASS ✓" else "REVIEW AUDIT",
                                style = MonoBadgeStyle.copy(fontSize = 9.5.sp),
                                color = if (allPassed) EmeraldPass else MutedParchment,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = episode.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = BoneWhite,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete episode",
                        tint = MutedParchment
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 1.dp, color = HairlineBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${episode.stories.size} ${if (episode.stories.size == 1) "STORY" else "STORIES"} • ${formatter.format(episode.totalSpokenChars)} / ${formatter.format(episode.totalTargetChars)} CHARS",
                    style = MonoBadgeStyle.copy(fontSize = 10.sp),
                    color = MutedParchment
                )
                Text(
                    text = dateFormat.format(Date(episode.createdAtMillis)),
                    style = MonoBadgeStyle.copy(fontSize = 10.sp),
                    color = MutedParchment
                )
            }
        }
    }
}

@Composable
private fun EpisodeDetailView(
    episode: EpisodeResult,
    onBack: () -> Unit,
    onCopyText: (label: String, text: String) -> Unit,
    onSaveTxtFile: (suggestedFileName: String, text: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    var selectedStoryIdx by remember(episode.id) { mutableIntStateOf(0) }
    var showProductionNotes by remember { mutableStateOf(false) }

    val safeIndex = selectedStoryIdx.coerceIn(0, (episode.stories.size - 1).coerceAtLeast(0))
    val activeStory = episode.stories.getOrNull(safeIndex)
    val sanitizedSlug = episode.title.lowercase().replace(Regex("""[^a-z0-9]+"""), "_").trim('_').ifEmpty { "episode" }

    androidx.activity.compose.BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("archive_detail_back")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Archive",
                    tint = BoneWhite
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = episode.category.displayName.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = CrimsonPrimary
                )
                Text(
                    text = episode.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = BoneWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Export action strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCopyText("TTS-Ready Script", episode.ttsReadyExport)
                },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("copy_tts_ready_btn"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CrimsonPrimary,
                    contentColor = BoneWhite
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = StudioIcons.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy TTS (.txt)",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveTxtFile("${sanitizedSlug}_tts_ready.txt", episode.ttsReadyExport)
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("save_tts_ready_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, CrimsonPrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = BoneWhite),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = StudioIcons.Download,
                    contentDescription = "Save TTS-Ready .txt",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCopyText("Full Master Script", episode.fullMasterExport)
                },
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("copy_full_master_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HairlineBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElevatedSlate,
                    contentColor = BoneWhite
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = StudioIcons.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Copy Full Master (.txt)",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onSaveTxtFile("${sanitizedSlug}_full_master.txt", episode.fullMasterExport)
                },
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("save_full_master_btn"),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, HairlineBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = ElevatedSlate,
                    contentColor = BoneWhite
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = StudioIcons.Download,
                    contentDescription = "Save Full Master .txt",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Story-by-story selector tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            episode.stories.forEachIndexed { idx, story ->
                val isSelected = idx == safeIndex && !showProductionNotes
                Surface(
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(100.dp))
                        .clickable {
                            showProductionNotes = false
                            selectedStoryIdx = idx
                        },
                    shape = RoundedCornerShape(100.dp),
                    color = if (isSelected) CrimsonMuted else ElevatedSlate,
                    border = BorderStroke(1.dp, if (isSelected) CrimsonPrimary else HairlineBorder)
                ) {
                    Text(
                        text = "${story.storyId}: ${story.chapterTitle}",
                        style = MonoBadgeStyle.copy(fontSize = 11.sp),
                        color = if (isSelected) BoneWhite else MutedParchment,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        maxLines = 1
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .clip(RoundedCornerShape(100.dp))
                    .clickable { showProductionNotes = !showProductionNotes },
                shape = RoundedCornerShape(100.dp),
                color = if (showProductionNotes) CrimsonMuted else ElevatedSlate,
                border = BorderStroke(1.dp, if (showProductionNotes) CrimsonPrimary else HairlineBorder)
            ) {
                Text(
                    text = "MASTER NOTES",
                    style = MonoBadgeStyle.copy(fontSize = 11.sp),
                    color = if (showProductionNotes) BoneWhite else MutedParchment,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (showProductionNotes) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    SyntaxHighlightedScriptConsole(scriptText = episode.fullMasterExport)
                }
            }
        } else if (activeStory != null) {
            AuditScorecardStrip(report = activeStory.auditReport)
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(visible = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBackground,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Text(
                        text = "${activeStory.mechanism.label} • ${activeStory.openingHookType.label} • ${activeStory.endingType.label}",
                        style = MonoBadgeStyle.copy(fontSize = 10.sp),
                        color = MutedParchment,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(14.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    SyntaxHighlightedScriptConsole(scriptText = activeStory.ttsReadyText)
                }
            }
        }
    }
}
