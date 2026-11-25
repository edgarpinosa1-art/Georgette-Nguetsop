package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.model.AuditReport
import com.example.domain.model.EngineStatus
import com.example.domain.model.RamTelemetry
import com.example.ui.theme.AmberMuted
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonMuted
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldMuted
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MonoBadgeStyle
import com.example.ui.theme.MutedParchment
import com.example.ui.theme.ObsidianBackground
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MissingPageTopBar(
    engineStatus: EngineStatus,
    ramTelemetry: RamTelemetry,
    onTelemetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            MissingPageLogo(size = 38.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = stringResource(R.string.brand_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 17.sp,
                        letterSpacing = 0.6.sp
                    ),
                    color = BoneWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stringResource(R.string.brand_subtitle),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 1.6.sp
                    ),
                    color = MutedParchment,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        EngineTelemetryPill(
            engineStatus = engineStatus,
            ramTelemetry = ramTelemetry,
            onClick = onTelemetryClick
        )
    }
}

@Composable
fun EngineTelemetryPill(
    engineStatus: EngineStatus,
    ramTelemetry: RamTelemetry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = when (engineStatus) {
            is EngineStatus.Ready -> EmeraldPass
            is EngineStatus.Loading -> AmberWarn
            is EngineStatus.Error -> CrimsonPrimary
            EngineStatus.Unloaded -> MutedParchment
        },
        label = "telemetryDot"
    )

    val modelLabel = when (engineStatus) {
        is EngineStatus.Ready -> engineStatus.modelName
            .removeSuffix(".gguf")
            .take(14)
        is EngineStatus.Loading -> "LOADING..."
        is EngineStatus.Error -> "ENGINE ERR"
        EngineStatus.Unloaded -> "NO GGUF"
    }

    val ramText = if (ramTelemetry.availableMb > 0) {
        val gb = ramTelemetry.availableMb / 1024f
        String.format(Locale.US, "%.1fG FREE", gb)
    } else {
        "-- FREE"
    }

    Surface(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .testTag("engine_telemetry_pill")
            .clip(RoundedCornerShape(100.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(100.dp),
        color = ElevatedSlate,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = modelLabel,
                    style = MonoBadgeStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = BoneWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = ramText,
                    style = MonoBadgeStyle.copy(fontSize = 9.sp),
                    color = if (ramTelemetry.lowMemory) AmberWarn else MutedParchment,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun TactileStepper(
    label: String,
    value: Int,
    min: Int,
    max: Int,
    suffix: String = "",
    onValueChange: (Int) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = ElevatedSlate,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MutedParchment
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_minus")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = value > min) {
                            onValueChange((value - 1).coerceAtLeast(min))
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = StudioIcons.Remove,
                            contentDescription = "Decrease $label",
                            tint = if (value > min) BoneWhite else MutedParchment.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Text(
                    text = if (suffix.isEmpty()) "$value" else "$value $suffix",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 15.sp
                    ),
                    color = BoneWhite
                )

                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_plus")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = value < max) {
                            onValueChange((value + 1).coerceAtMost(max))
                        },
                    shape = RoundedCornerShape(8.dp),
                    color = CharcoalSurface,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase $label",
                            tint = if (value < max) BoneWhite else MutedParchment.copy(alpha = 0.35f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun <T> SegmentedPillToggle(
    options: List<T>,
    selectedOption: T,
    labelProvider: (T) -> String,
    onSelect: (T) -> Unit,
    testTagPrefix: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = ObsidianBackground,
        border = BorderStroke(1.dp, HairlineBorder)
    ) {
        Row(
            modifier = Modifier.padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEachIndexed { index, option ->
                val isSelected = option == selectedOption
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .testTag("${testTagPrefix}_$index")
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onSelect(option) },
                    shape = RoundedCornerShape(7.dp),
                    color = if (isSelected) CrimsonMuted else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.7f)) else null
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = labelProvider(option),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 11.sp,
                                letterSpacing = 0.4.sp
                            ),
                            color = if (isSelected) BoneWhite else MutedParchment,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpokenBudgetBar(
    spokenChars: Int,
    targetChars: Int,
    isStreaming: Boolean,
    modifier: Modifier = Modifier
) {
    val safeTarget = targetChars.coerceAtLeast(1)
    val rawProgress = (spokenChars.toFloat() / safeTarget.toFloat()).coerceIn(0f, 1.15f)
    val animatedProgress by animateFloatAsState(
        targetValue = rawProgress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "budgetProgress"
    )
    val formatter = NumberFormat.getIntegerInstance(Locale.US)

    val barColor by animateColorAsState(
        targetValue = when {
            isStreaming -> CrimsonPrimary
            spokenChars >= (safeTarget * 0.88).toInt() -> EmeraldPass
            spokenChars > 0 -> AmberWarn
            else -> MutedParchment
        },
        label = "budgetBarColor"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SPOKEN CHAR BUDGET",
                style = MaterialTheme.typography.labelSmall,
                color = MutedParchment
            )
            Text(
                text = "${formatter.format(spokenChars)} / ${formatter.format(safeTarget)} CHARS",
                style = MonoBadgeStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                color = if (spokenChars >= (safeTarget * 0.88).toInt() && !isStreaming) EmeraldPass else BoneWhite
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ElevatedSlate)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun SyntaxHighlightedScriptConsole(
    scriptText: String,
    modifier: Modifier = Modifier
) {
    val lines = scriptText.lines()
    val sfxPattern = Regex("""(\[SFX:[^\]]+])""", RegexOption.IGNORE_CASE)
    val silencePattern = Regex("""^\[DEAD SILENCE DROP:\s*1\.5s\]$""", RegexOption.IGNORE_CASE)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        lines.forEach { rawLine ->
            val trimmed = rawLine.trim()
            if (trimmed.isEmpty()) {
                return@forEach
            }
            when {
                silencePattern.matches(trimmed) -> {
                    SilenceDividerPill(label = trimmed)
                }
                trimmed.startsWith("[SFX:", ignoreCase = true) && trimmed.endsWith("]") && !trimmed.drop(5).contains("[SFX:") -> {
                    SfxChip(label = trimmed)
                }
                trimmed.startsWith("[CHAPTER TITLE:", ignoreCase = true) ||
                    trimmed.startsWith("[PRODUCTION NOTES]", ignoreCase = true) ||
                    trimmed.startsWith("[PUBLIC DISCLOSURE", ignoreCase = true) ||
                    trimmed.startsWith("[INTERNAL PRODUCTION", ignoreCase = true) -> {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElevatedSlate,
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Text(
                            text = trimmed,
                            style = MonoBadgeStyle.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = MutedParchment,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
                trimmed == "Turn the page. Find what was left behind." ||
                    trimmed == "The page is closed. The questions remain." -> {
                    Text(
                        text = trimmed,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.2.sp
                        ),
                        color = CrimsonPrimary
                    )
                }
                else -> {
                    val annotated = buildAnnotatedString {
                        var lastIndex = 0
                        for (match in sfxPattern.findAll(trimmed)) {
                            if (match.range.first > lastIndex) {
                                append(trimmed.substring(lastIndex, match.range.first))
                            }
                            withStyle(
                                SpanStyle(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = BoneWhite,
                                    background = CrimsonMuted
                                )
                            ) {
                                append(" ${match.value} ")
                            }
                            lastIndex = match.range.last + 1
                        }
                        if (lastIndex < trimmed.length) {
                            append(trimmed.substring(lastIndex))
                        }
                    }
                    Text(
                        text = annotated,
                        style = MaterialTheme.typography.bodyLarge,
                        color = BoneWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SfxChip(label: String) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = CrimsonMuted,
        border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.55f))
    ) {
        Text(
            text = label,
            style = MonoBadgeStyle.copy(fontSize = 11.5.sp),
            color = BoneWhite,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun SilenceDividerPill(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = HairlineBorder
        )
        Surface(
            modifier = Modifier.padding(horizontal = 8.dp),
            shape = RoundedCornerShape(100.dp),
            color = ObsidianBackground,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Text(
                text = label.uppercase(),
                style = MonoBadgeStyle.copy(fontSize = 10.sp, letterSpacing = 0.8.sp),
                color = MutedParchment,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = 1.dp,
            color = HairlineBorder
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AuditScorecardStrip(
    report: AuditReport,
    modifier: Modifier = Modifier
) {
    val formatter = NumberFormat.getIntegerInstance(Locale.US)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AuditBadgePill("ZERO DIGITS", report.zeroDigitsPassed)
            AuditBadgePill("BRAND LOCK", report.brandLockPassed)
            AuditBadgePill("POV LOCK", report.povLockPassed)
            AuditBadgePill("SFX VALID", report.sfxValidPassed)
            AuditBadgePill("ENGINE UNIQUE", report.engineUniquePassed)
            Surface(
                shape = RoundedCornerShape(100.dp),
                color = ElevatedSlate,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Text(
                    text = "CHARS: ${formatter.format(report.spokenCharCount)} / ${formatter.format(report.targetCharCount)}",
                    style = MonoBadgeStyle,
                    color = BoneWhite,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }
        }

        if (report.detectedCliches.isNotEmpty() || report.repairsApplied.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                report.repairsApplied.forEach { repair ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElevatedSlate,
                        border = BorderStroke(1.dp, HairlineBorder)
                    ) {
                        Text(
                            text = "AUTO-FIXED: $repair",
                            style = MonoBadgeStyle.copy(fontSize = 9.5.sp),
                            color = MutedParchment,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                if (report.detectedCliches.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AmberMuted,
                        border = BorderStroke(1.dp, AmberWarn.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Cliché warning",
                                tint = AmberWarn,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "CLICHÉ FLAGGED: ${report.detectedCliches.joinToString(", ")}",
                                style = MonoBadgeStyle.copy(fontSize = 9.5.sp),
                                color = AmberWarn
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditBadgePill(label: String, passed: Boolean) {
    val bg = if (passed) EmeraldMuted else CrimsonMuted
    val border = if (passed) EmeraldPass.copy(alpha = 0.5f) else CrimsonPrimary.copy(alpha = 0.7f)
    val fg = if (passed) EmeraldPass else BoneWhite
    val mark = if (passed) "✓" else "✕"

    Surface(
        shape = RoundedCornerShape(100.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (passed) Icons.Default.CheckCircle else StudioIcons.Memory,
                contentDescription = null,
                tint = fg,
                modifier = Modifier.size(11.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$label $mark",
                style = MonoBadgeStyle,
                color = fg
            )
        }
    }
}
