package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ClearPageConfirmDialog
import com.example.ui.components.ColoringCanvasViewport
import com.example.ui.components.CustomColorPickerDialog
import com.example.ui.components.LandscapeLeftToolRail
import com.example.ui.components.LandscapeRightColorRail
import com.example.ui.components.PageThumbnailDrawerDialog
import com.example.ui.components.PortraitBottomDock
import com.example.ui.components.StudioLoadingOverlay
import com.example.ui.components.StudioToastBanner
import com.example.ui.components.StudioTopBar
import com.example.ui.theme.ChromaStudioTheme
import com.example.viewmodel.StudioViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChromaStudioTheme {
                ChromaStudioScreen(
                    onFullscreenChanged = { isFullscreen ->
                        applySystemBarVisibility(isFullscreen)
                    }
                )
            }
        }
    }

    private fun applySystemBarVisibility(fullscreen: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (fullscreen) {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}

@Composable
fun ChromaStudioScreen(
    onFullscreenChanged: (Boolean) -> Unit,
    viewModel: StudioViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Sync Android system bars with Fullscreen toggle
    LaunchedEffect(uiState.fullscreenEnabled) {
        onFullscreenChanged(uiState.fullscreenEnabled)
    }

    // Handle back press gracefully from Fullscreen or open modals
    BackHandler(
        enabled = uiState.showThumbnailDrawer ||
            uiState.showCustomColorDialog ||
            uiState.showClearDialog ||
            uiState.fullscreenEnabled
    ) {
        when {
            uiState.showThumbnailDrawer -> viewModel.closeThumbnailDrawer()
            uiState.showCustomColorDialog -> viewModel.closeCustomColorDialog()
            uiState.showClearDialog -> viewModel.dismissClearPage()
            uiState.fullscreenEnabled -> viewModel.toggleFullscreen()
        }
    }

    // Listen for PNG share intents
    LaunchedEffect(viewModel) {
        viewModel.shareIntentEvents.collect { shareIntent ->
            context.startActivity(shareIntent)
        }
    }

    // Zero-permission SAF PDF picker
    val openPdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.openPdfUri(uri)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            StudioTopBar(
                uiState = uiState,
                onOpenPdfClick = { openPdfLauncher.launch(arrayOf("application/pdf")) },
                onLoadSampleClick = { viewModel.loadSampleBook() },
                onPrevPageClick = { viewModel.loadPage(uiState.currentPage - 1, fitAfterLoad = true) },
                onNextPageClick = { viewModel.loadPage(uiState.currentPage + 1, fitAfterLoad = true) },
                onOpenDrawerClick = { viewModel.openThumbnailDrawer() },
                onUndoClick = { viewModel.undo() },
                onRedoClick = { viewModel.redo() },
                onTogglePalmLock = { viewModel.togglePalmLock() },
                onToggleKidLock = { viewModel.toggleKidLock() },
                onToggleFullscreen = { viewModel.toggleFullscreen() },
                onSavePngClick = { viewModel.exportCurrentPage() }
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isLandscapeStudio = maxWidth > maxHeight && maxHeight <= 820.dp

                if (isLandscapeStudio) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        LandscapeLeftToolRail(
                            uiState = uiState,
                            onSelectTool = viewModel::selectTool,
                            onSelectSize = viewModel::selectBrushSize,
                            onClearPageClick = viewModel::requestClearPage
                        )

                        ColoringCanvasViewport(
                            uiState = uiState,
                            colorBitmap = viewModel.activeColorBitmap,
                            lineArtBitmap = viewModel.activeLineArtBitmap,
                            fitEvents = viewModel.fitViewportEvents,
                            onBeginStroke = viewModel::beginStroke,
                            onDrawSegment = viewModel::drawStrokeSegment,
                            onFloodFill = viewModel::performFloodFill,
                            onPickColor = viewModel::pickColorAt,
                            modifier = Modifier.weight(1f)
                        )

                        LandscapeRightColorRail(
                            uiState = uiState,
                            onSelectColor = viewModel::selectColor,
                            onOpenCustomColor = viewModel::openCustomColorDialog
                        )
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ColoringCanvasViewport(
                            uiState = uiState,
                            colorBitmap = viewModel.activeColorBitmap,
                            lineArtBitmap = viewModel.activeLineArtBitmap,
                            fitEvents = viewModel.fitViewportEvents,
                            onBeginStroke = viewModel::beginStroke,
                            onDrawSegment = viewModel::drawStrokeSegment,
                            onFloodFill = viewModel::performFloodFill,
                            onPickColor = viewModel::pickColorAt,
                            modifier = Modifier.weight(1f)
                        )

                        PortraitBottomDock(
                            uiState = uiState,
                            onSelectTool = viewModel::selectTool,
                            onSelectSize = viewModel::selectBrushSize,
                            onSelectColor = viewModel::selectColor,
                            onOpenCustomColor = viewModel::openCustomColorDialog,
                            onClearPageClick = viewModel::requestClearPage
                        )
                    }
                }
            }
        }

        // Loading overlay
        StudioLoadingOverlay(
            visible = uiState.isLoading,
            title = uiState.loadingTitle,
            subtitle = uiState.loadingSubtitle
        )

        // Floating Toast Banner
        StudioToastBanner(
            message = uiState.toastMessage,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
        )

        // Visual Page Thumbnail Drawer
        PageThumbnailDrawerDialog(
            uiState = uiState,
            onSelectPage = { pageNum ->
                viewModel.closeThumbnailDrawer()
                viewModel.loadPage(pageNum, fitAfterLoad = true)
            },
            onDismiss = viewModel::closeThumbnailDrawer
        )

        // Clear Page Confirmation Dialog
        ClearPageConfirmDialog(
            visible = uiState.showClearDialog,
            onConfirm = viewModel::confirmClearPage,
            onDismiss = viewModel::dismissClearPage
        )

        // Custom Color Picker Dialog
        CustomColorPickerDialog(
            visible = uiState.showCustomColorDialog,
            initialColor = uiState.activeColor,
            onSelectColor = viewModel::selectColor,
            onDismiss = viewModel::closeCustomColorDialog
        )
    }
}
