package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.components.StudioIcons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.preferences.PersistedModelRef
import com.example.data.vault.PromptVaultState
import com.example.domain.model.EngineStatus
import com.example.domain.model.InferenceSettings
import com.example.domain.model.RamTelemetry
import com.example.ui.components.TactileStepper
import com.example.ui.theme.AmberWarn
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonMuted
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.ElevatedSlate
import com.example.ui.theme.EmeraldPass
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MonoBadgeStyle
import com.example.ui.theme.MonoScriptStyle
import com.example.ui.theme.MutedParchment
import com.example.ui.theme.ObsidianBackground
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun EngineVaultScreen(
    engineStatus: EngineStatus,
    persistedModelRef: PersistedModelRef,
    stagingProgress: Float?,
    isPreparingModel: Boolean,
    ramTelemetry: RamTelemetry,
    inferenceSettings: InferenceSettings,
    vaultState: PromptVaultState,
    onPickGgufModel: () -> Unit,
    onLoadModel: () -> Unit,
    onUnloadModel: () -> Unit,
    onUpdateSettings: ((InferenceSettings) -> InferenceSettings) -> Unit,
    onImportGodPrompt: () -> Unit,
    onResetGodPrompt: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showVaultDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.engine_title),
            style = MaterialTheme.typography.labelSmall,
            color = MutedParchment
        )

        // RAM Telemetry Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = StudioIcons.Memory,
                            contentDescription = "Device RAM Telemetry",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEVICE MEMORY TELEMETRY",
                            style = MaterialTheme.typography.labelSmall,
                            color = MutedParchment
                        )
                    }
                    Text(
                        text = "${ramTelemetry.availableMb} MB FREE / ${ramTelemetry.totalMb} MB",
                        style = MonoBadgeStyle,
                        color = if (ramTelemetry.lowMemory) AmberWarn else EmeraldPass
                    )
                }

                val usedFraction = if (ramTelemetry.totalMb > 0L) {
                    (1f - ramTelemetry.availableMb.toFloat() / ramTelemetry.totalMb.toFloat()).coerceIn(0f, 1f)
                } else 0f

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ElevatedSlate)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(usedFraction)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (ramTelemetry.lowMemory) AmberWarn else CrimsonPrimary)
                    )
                }

                Text(
                    text = "largeHeap enabled • Memory-mapped loading (mmap) recommended for 5.6 GB Qwen3.5-9B Q4_K_M weights.",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                    color = MutedParchment
                )
            }
        }

        // Local GGUF Model Manager Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LOCAL GGUF MODEL LOADER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedParchment
                    )
                    val statusTag = when (engineStatus) {
                        is EngineStatus.Ready -> "LOADED"
                        is EngineStatus.Loading -> "LOADING"
                        is EngineStatus.Error -> "ERROR"
                        EngineStatus.Unloaded -> "UNLOADED"
                    }
                    Text(
                        text = statusTag,
                        style = MonoBadgeStyle,
                        color = when (engineStatus) {
                            is EngineStatus.Ready -> EmeraldPass
                            is EngineStatus.Loading -> AmberWarn
                            is EngineStatus.Error -> CrimsonPrimary
                            EngineStatus.Unloaded -> MutedParchment
                        }
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBackground,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = persistedModelRef.displayName.ifBlank { "No .gguf selected" },
                            style = MaterialTheme.typography.titleMedium,
                            color = BoneWhite,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val sizeGb = if (persistedModelRef.sizeBytes > 0L) {
                            String.format(Locale.US, "%.2f GB", persistedModelRef.sizeBytes / (1024.0 * 1024.0 * 1024.0))
                        } else {
                            "Recommended: Qwen3.5-9B-Uncensored-HauhauCS-Aggressive-Q4_K_M.gguf"
                        }
                        Text(
                            text = sizeGb,
                            style = MonoBadgeStyle.copy(fontSize = 10.sp),
                            color = MutedParchment
                        )
                        if (engineStatus is EngineStatus.Error) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = engineStatus.message,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                                color = CrimsonPrimary
                            )
                        }
                    }
                }

                if (isPreparingModel || stagingProgress != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = if (stagingProgress != null) {
                                "Staging GGUF via SAF: ${(stagingProgress * 100).roundToInt()}%"
                            } else {
                                "Preparing model stream..."
                            },
                            style = MonoBadgeStyle,
                            color = AmberWarn
                        )
                        if (stagingProgress != null) {
                            LinearProgressIndicator(
                                progress = { stagingProgress },
                                modifier = Modifier.fillMaxWidth(),
                                color = CrimsonPrimary,
                                trackColor = ElevatedSlate
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = CrimsonPrimary,
                                trackColor = ElevatedSlate
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onPickGgufModel,
                        enabled = !isPreparingModel && engineStatus !is EngineStatus.Loading,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("pick_gguf_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElevatedSlate,
                            contentColor = BoneWhite
                        )
                    ) {
                        Icon(StudioIcons.FolderOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.engine_pick_gguf),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                            maxLines = 1
                        )
                    }

                    if (engineStatus is EngineStatus.Ready) {
                        OutlinedButton(
                            onClick = onUnloadModel,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("unload_model_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, CrimsonPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CrimsonMuted,
                                contentColor = BoneWhite
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.engine_unload_model),
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                maxLines = 1
                            )
                        }
                    } else {
                        Button(
                            onClick = onLoadModel,
                            enabled = persistedModelRef.path.isNotBlank() && engineStatus !is EngineStatus.Loading && !isPreparingModel,
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("load_model_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CrimsonPrimary,
                                contentColor = BoneWhite
                            )
                        ) {
                            if (engineStatus is EngineStatus.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = BoneWhite
                                )
                            } else {
                                Text(
                                    text = stringResource(R.string.engine_load_model),
                                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }

        // Pre-Calibrated Inference Sliders Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QWEN3.5-9B INFERENCE CALIBRATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedParchment
                    )
                    IconButton(
                        onClick = { onUpdateSettings { InferenceSettings() } },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset optimal inference defaults",
                            tint = MutedParchment
                        )
                    }
                }

                // Context Window Selector (4096 / 6144 / 8192)
                Text(
                    text = "CONTEXT SIZE (n_ctx)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedParchment
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4096, 6144, 8192).forEach { ctx ->
                        val selected = inferenceSettings.contextLength == ctx
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onUpdateSettings { it.copy(contextLength = ctx) } },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) CrimsonMuted else ElevatedSlate,
                            border = BorderStroke(1.dp, if (selected) CrimsonPrimary else HairlineBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$ctx",
                                    style = MonoBadgeStyle.copy(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
                                    color = if (selected) BoneWhite else MutedParchment
                                )
                            }
                        }
                    }
                }

                SliderParamRow(
                    label = "TEMPERATURE",
                    valueText = String.format(Locale.US, "%.2f", inferenceSettings.temperature),
                    value = inferenceSettings.temperature,
                    range = 0.10f..1.20f,
                    onValueChange = { v ->
                        val rounded = (v * 20f).roundToInt() / 20f
                        onUpdateSettings { it.copy(temperature = rounded) }
                    }
                )

                SliderParamRow(
                    label = "TOP-P",
                    valueText = String.format(Locale.US, "%.2f", inferenceSettings.topP),
                    value = inferenceSettings.topP,
                    range = 0.50f..1.00f,
                    onValueChange = { v ->
                        val rounded = (v * 20f).roundToInt() / 20f
                        onUpdateSettings { it.copy(topP = rounded) }
                    }
                )

                SliderParamRow(
                    label = "TOP-K",
                    valueText = "${inferenceSettings.topK}",
                    value = inferenceSettings.topK.toFloat(),
                    range = 5f..80f,
                    onValueChange = { v ->
                        onUpdateSettings { it.copy(topK = v.roundToInt()) }
                    }
                )

                SliderParamRow(
                    label = "REPEAT PENALTY",
                    valueText = String.format(Locale.US, "%.2f", inferenceSettings.repeatPenalty),
                    value = inferenceSettings.repeatPenalty,
                    range = 1.00f..1.40f,
                    onValueChange = { v ->
                        val rounded = (v * 20f).roundToInt() / 20f
                        onUpdateSettings { it.copy(repeatPenalty = rounded) }
                    }
                )

                TactileStepper(
                    label = "CPU Threads",
                    value = inferenceSettings.threads,
                    min = 1,
                    max = 8,
                    onValueChange = { threads ->
                        onUpdateSettings { it.copy(threads = threads) }
                    },
                    testTagPrefix = "cpu_threads"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Memory-Mapped Loading (mmap)",
                            style = MaterialTheme.typography.titleMedium,
                            color = BoneWhite
                        )
                        Text(
                            text = "Streams tensor pages directly without duplicating full weights in heap.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.5.sp),
                            color = MutedParchment
                        )
                    }
                    Switch(
                        checked = inferenceSettings.useMmap,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(useMmap = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BoneWhite,
                            checkedTrackColor = CrimsonPrimary,
                            uncheckedThumbColor = MutedParchment,
                            uncheckedTrackColor = ElevatedSlate
                        )
                    )
                }
            }
        }

        // God Prompt v5.3.4-US Vault Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            color = CharcoalSurface,
            border = BorderStroke(1.dp, HairlineBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GOD PROMPT v5.3.4-US VAULT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MutedParchment
                    )
                    Text(
                        text = if (vaultState.isCustomImported) "IMPORTED" else "KERNEL BUILT-IN",
                        style = MonoBadgeStyle,
                        color = if (vaultState.isCustomImported) EmeraldPass else BoneWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianBackground,
                    border = BorderStroke(1.dp, HairlineBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = StudioIcons.Description,
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = vaultState.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = BoneWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${vaultState.charCount} characters stored in private filesDir/prompts/",
                                style = MonoBadgeStyle.copy(fontSize = 10.sp),
                                color = MutedParchment
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onImportGodPrompt,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("import_vault_prompt_btn"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElevatedSlate,
                            contentColor = BoneWhite
                        )
                    ) {
                        Icon(StudioIcons.FolderOpen, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.vault_import_prompt),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = { showVaultDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("view_vault_prompt_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CrimsonMuted,
                            contentColor = BoneWhite
                        ),
                        border = BorderStroke(1.dp, CrimsonPrimary.copy(alpha = 0.65f))
                    ) {
                        Icon(StudioIcons.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.vault_view_prompt),
                            style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                            maxLines = 1
                        )
                    }
                }

                if (vaultState.isCustomImported) {
                    OutlinedButton(
                        onClick = onResetGodPrompt,
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize(),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, HairlineBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MutedParchment)
                    ) {
                        Text(
                            text = "Restore Built-In Kernel Reference",
                            style = MonoBadgeStyle
                        )
                    }
                }
            }
        }
    }

    if (showVaultDialog) {
        Dialog(
            onDismissRequest = { showVaultDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = CharcoalSurface,
                border = BorderStroke(1.dp, HairlineBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PROMPT VAULT VIEWER",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrimsonPrimary
                            )
                            Text(
                                text = vaultState.fileName,
                                style = MaterialTheme.typography.titleMedium,
                                color = BoneWhite,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = { showVaultDialog = false }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Vault Viewer",
                                tint = BoneWhite
                            )
                        }
                    }
                    HorizontalDivider(thickness = 1.dp, color = HairlineBorder)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp)
                    ) {
                        Text(
                            text = vaultState.content,
                            style = MonoScriptStyle,
                            color = BoneWhite
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderParamRow(
    label: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MutedParchment
            )
            Text(
                text = valueText,
                style = MonoBadgeStyle,
                color = BoneWhite
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = BoneWhite,
                activeTrackColor = CrimsonPrimary,
                inactiveTrackColor = ElevatedSlate
            )
        )
    }
}
