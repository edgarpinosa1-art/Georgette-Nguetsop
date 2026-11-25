package com.example

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import com.example.ui.components.StudioIcons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ui.components.MissingPageTopBar
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.EngineVaultScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.theme.BoneWhite
import com.example.ui.theme.CharcoalSurface
import com.example.ui.theme.CrimsonMuted
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.theme.HairlineBorder
import com.example.ui.theme.MissingPageTheme
import com.example.ui.theme.MutedParchment
import com.example.ui.theme.ObsidianBackground
import com.example.viewmodel.StudioViewModel

private const val ROUTE_STUDIO = "studio"
private const val ROUTE_ARCHIVE = "archive"
private const val ROUTE_ENGINE = "engine"

private data class StudioTabItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#09090B")),
            navigationBarStyle = SystemBarStyle.dark(AndroidColor.parseColor("#09090B"))
        )
        setContent {
            MissingPageTheme {
                MissingPageStudioApp()
            }
        }
    }
}

@Composable
fun MissingPageStudioApp(
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val engineStatus by viewModel.engineStatus.collectAsStateWithLifecycle()
    val stagingProgress by viewModel.stagingProgress.collectAsStateWithLifecycle()
    val inferenceSettings by viewModel.inferenceSettings.collectAsStateWithLifecycle()
    val savedEpisodes by viewModel.savedEpisodes.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_STUDIO

    val snackbarHostState = remember { SnackbarHostState() }
    var pendingExportContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.toastEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val ggufPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.pickAndPrepareModel(uri)
        }
    }

    val promptPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importGodPrompt(uri)
        }
    }

    val txtExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val content = pendingExportContent
        pendingExportContent = null
        if (uri != null && content != null) {
            viewModel.exportTxtToUri(uri, content)
        }
    }

    val tabs = remember {
        listOf(
            StudioTabItem(
                route = ROUTE_STUDIO,
                labelRes = R.string.nav_studio,
                selectedIcon = StudioIcons.EditNote,
                unselectedIcon = StudioIcons.EditNote,
                testTag = "nav_tab_studio"
            ),
            StudioTabItem(
                route = ROUTE_ARCHIVE,
                labelRes = R.string.nav_archive,
                selectedIcon = StudioIcons.AutoStories,
                unselectedIcon = StudioIcons.AutoStories,
                testTag = "nav_tab_archive"
            ),
            StudioTabItem(
                route = ROUTE_ENGINE,
                labelRes = R.string.nav_engine,
                selectedIcon = StudioIcons.Tune,
                unselectedIcon = StudioIcons.Tune,
                testTag = "nav_tab_engine"
            )
        )
    }

    val navigateToTab: (String) -> Unit = { route ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(navController.graph.startDestinationId) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 720.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBackground,
            contentColor = BoneWhite,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                if (!useRail) {
                    Column {
                        HorizontalDivider(thickness = 1.dp, color = HairlineBorder)
                        NavigationBar(
                            containerColor = CharcoalSurface,
                            contentColor = BoneWhite,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            tabs.forEach { tab ->
                                val selected = currentRoute == tab.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTab(tab.route) },
                                    modifier = Modifier.testTag(tab.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = stringResource(tab.labelRes)
                                        )
                                    },
                                    label = {
                                        Text(text = stringResource(tab.labelRes))
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = BoneWhite,
                                        selectedTextColor = BoneWhite,
                                        indicatorColor = CrimsonMuted,
                                        unselectedIconColor = MutedParchment,
                                        unselectedTextColor = MutedParchment
                                    )
                                )
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useRail) {
                    NavigationRail(
                        containerColor = CharcoalSurface,
                        contentColor = BoneWhite,
                        modifier = Modifier
                            .fillMaxHeight()
                            .statusBarsPadding()
                    ) {
                        tabs.forEach { tab ->
                            val selected = currentRoute == tab.route
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(tab.route) },
                                modifier = Modifier.testTag(tab.testTag),
                                icon = {
                                    Icon(
                                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                        contentDescription = stringResource(tab.labelRes)
                                    )
                                },
                                label = {
                                    Text(text = stringResource(tab.labelRes))
                                },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = BoneWhite,
                                    selectedTextColor = BoneWhite,
                                    indicatorColor = CrimsonMuted,
                                    unselectedIconColor = MutedParchment,
                                    unselectedTextColor = MutedParchment
                                )
                            )
                        }
                    }
                    VerticalDivider(thickness = 1.dp, color = HairlineBorder)
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MissingPageTopBar(
                        engineStatus = engineStatus,
                        ramTelemetry = uiState.ramTelemetry,
                        onTelemetryClick = { navigateToTab(ROUTE_ENGINE) },
                        modifier = Modifier.widthIn(max = 840.dp)
                    )
                    HorizontalDivider(thickness = 1.dp, color = HairlineBorder)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        NavHost(
                            navController = navController,
                            startDestination = ROUTE_STUDIO,
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = 840.dp)
                        ) {
                            composable(ROUTE_STUDIO) {
                                StudioScreen(
                                    uiState = uiState,
                                    engineStatus = engineStatus,
                                    onCategorySelected = viewModel::updateCategory,
                                    onStoryCountChange = viewModel::updateStoryCount,
                                    onPartsChange = viewModel::updatePartsPerStory,
                                    onPartModeChange = viewModel::updatePartAllocationMode,
                                    onPerStoryPartChange = viewModel::updatePerStoryPart,
                                    onRuntimeMinutesChange = viewModel::updateRuntimeMinutes,
                                    onRuntimeModeChange = viewModel::updateRuntimeAllocationMode,
                                    onPremiseChange = viewModel::updatePremise,
                                    onGenerateClick = viewModel::generateEpisode,
                                    onStopClick = viewModel::stopGeneration,
                                    onExpandStoryClick = viewModel::expandActiveStory,
                                    onStoryTabSelected = viewModel::selectActiveStoryTab,
                                    onOpenEngineTab = { navigateToTab(ROUTE_ENGINE) },
                                    onCopyText = viewModel::copyToClipboard,
                                    onSaveTxtFile = { suggestedName, content ->
                                        pendingExportContent = content
                                        txtExportLauncher.launch(suggestedName)
                                    }
                                )
                            }

                            composable(ROUTE_ARCHIVE) {
                                ArchiveScreen(
                                    episodes = savedEpisodes,
                                    onCopyText = viewModel::copyToClipboard,
                                    onSaveTxtFile = { suggestedName, content ->
                                        pendingExportContent = content
                                        txtExportLauncher.launch(suggestedName)
                                    },
                                    onDeleteEpisode = viewModel::deleteEpisode
                                )
                            }

                            composable(ROUTE_ENGINE) {
                                EngineVaultScreen(
                                    engineStatus = engineStatus,
                                    persistedModelRef = uiState.persistedModelRef,
                                    stagingProgress = stagingProgress,
                                    isPreparingModel = uiState.isPreparingModel,
                                    ramTelemetry = uiState.ramTelemetry,
                                    inferenceSettings = inferenceSettings,
                                    vaultState = uiState.vaultState,
                                    onPickGgufModel = { ggufPickerLauncher.launch(arrayOf("*/*")) },
                                    onLoadModel = { viewModel.loadPersistedModel() },
                                    onUnloadModel = viewModel::unloadModel,
                                    onUpdateSettings = viewModel::updateInferenceSettings,
                                    onImportGodPrompt = {
                                        promptPickerLauncher.launch(arrayOf("text/plain", "text/*"))
                                    },
                                    onResetGodPrompt = viewModel::resetGodPromptToBuiltIn
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
