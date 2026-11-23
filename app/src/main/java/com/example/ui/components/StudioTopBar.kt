package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.StudioUiState
import com.example.ui.theme.LocalStudioColors

@Composable
fun StudioTopBar(
    uiState: StudioUiState,
    onOpenPdfClick: () -> Unit,
    onLoadSampleClick: () -> Unit,
    onPrevPageClick: () -> Unit,
    onNextPageClick: () -> Unit,
    onOpenDrawerClick: () -> Unit,
    onUndoClick: () -> Unit,
    onRedoClick: () -> Unit,
    onTogglePalmLock: () -> Unit,
    onToggleKidLock: () -> Unit,
    onToggleFullscreen: () -> Unit,
    onSavePngClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    val showAdminActions = !uiState.fullscreenEnabled && !uiState.kidLockEnabled

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = studio.chromeSurface,
        shadowElevation = 2.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            val availableWidth = maxWidth
            val isCompactPortrait = availableWidth < 600.dp && !uiState.fullscreenEnabled

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left section: Brand + Open PDF + Samples
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (showAdminActions) {
                            // Brand Badge
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(studio.orangeAccent, studio.roseAccent)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Palette,
                                    contentDescription = stringResource(R.string.app_name),
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            if (availableWidth >= 820.dp) {
                                Text(
                                    text = stringResource(R.string.app_name),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = studio.inkPrimary,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }

                            // Open PDF button
                            Button(
                                onClick = onOpenPdfClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studio.orangeAccent,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("open_pdf_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.UploadFile,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = stringResource(R.string.open_pdf),
                                    style = MaterialTheme.typography.labelLarge,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Samples button
                            Surface(
                                onClick = onLoadSampleClick,
                                shape = RoundedCornerShape(12.dp),
                                color = studio.amberBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, studio.amberBorder),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("sample_book_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = stringResource(R.string.samples),
                                        tint = studio.orangeAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    if (availableWidth >= 700.dp) {
                                        Text(
                                            text = stringResource(R.string.samples),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = studio.amberText
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Center section when wide or fullscreen
                    if (!isCompactPortrait) {
                        PageNavigationPill(
                            currentPage = uiState.currentPage,
                            totalPages = uiState.totalPages,
                            onPrevClick = onPrevPageClick,
                            onNextClick = onNextPageClick,
                            onOpenDrawerClick = onOpenDrawerClick
                        )
                    }

                    // Right section: Undo, Redo, Palm Lock, Kid Lock, Fullscreen, Save
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        TopIconChip(
                            icon = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = stringResource(R.string.undo),
                            enabled = uiState.canUndo,
                            onClick = onUndoClick,
                            testTag = "undo_button"
                        )

                        if (!uiState.fullscreenEnabled) {
                            TopIconChip(
                                icon = Icons.AutoMirrored.Rounded.Redo,
                                contentDescription = stringResource(R.string.redo),
                                enabled = uiState.canRedo,
                                onClick = onRedoClick,
                                testTag = "redo_button"
                            )
                        }

                        if (showAdminActions) {
                            TopPillToggle(
                                icon = Icons.Rounded.PanTool,
                                label = if (availableWidth >= 880.dp) stringResource(R.string.palm_lock) else null,
                                contentDescription = stringResource(R.string.palm_lock),
                                active = uiState.palmLockEnabled,
                                activeBg = studio.palmLockActive,
                                activeContentColor = Color.White,
                                onClick = onTogglePalmLock,
                                testTag = "palm_lock_button"
                            )
                        }

                        TopPillToggle(
                            icon = if (uiState.kidLockEnabled) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            label = if (availableWidth >= 720.dp) stringResource(R.string.kid_lock) else null,
                            contentDescription = stringResource(R.string.kid_lock),
                            active = uiState.kidLockEnabled,
                            activeBg = studio.kidLockActive,
                            activeContentColor = studio.inkPrimary,
                            onClick = onToggleKidLock,
                            testTag = "kid_lock_button"
                        )

                        // Fullscreen Toggle
                        Surface(
                            onClick = onToggleFullscreen,
                            shape = RoundedCornerShape(12.dp),
                            color = studio.inkPrimary,
                            contentColor = Color.White,
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("fullscreen_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.fullscreenEnabled) {
                                        Icons.Rounded.FullscreenExit
                                    } else {
                                        Icons.Rounded.Fullscreen
                                    },
                                    contentDescription = stringResource(R.string.fullscreen),
                                    modifier = Modifier.size(17.dp)
                                )
                                if (availableWidth >= 560.dp || uiState.fullscreenEnabled) {
                                    Text(
                                        text = if (uiState.fullscreenEnabled) {
                                            stringResource(R.string.exit_fullscreen)
                                        } else {
                                            stringResource(R.string.fullscreen)
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        if (showAdminActions) {
                            Button(
                                onClick = onSavePngClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = studio.emeraldSave,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("save_png_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = stringResource(R.string.save_png),
                                    modifier = Modifier.size(16.dp)
                                )
                                if (availableWidth >= 560.dp) {
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = stringResource(R.string.save_png),
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                            }
                        }
                    }
                }

                // Compact portrait second row for Page Nav Pill
                if (isCompactPortrait) {
                    Spacer(Modifier.height(6.dp))
                    HorizontalDivider(color = studio.chromeMuted, thickness = 1.dp)
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        PageNavigationPill(
                            currentPage = uiState.currentPage,
                            totalPages = uiState.totalPages,
                            onPrevClick = onPrevPageClick,
                            onNextClick = onNextPageClick,
                            onOpenDrawerClick = onOpenDrawerClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PageNavigationPill(
    currentPage: Int,
    totalPages: Int,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onOpenDrawerClick: () -> Unit
) {
    val studio = LocalStudioColors.current
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(studio.chromeMuted)
            .border(1.dp, studio.chromeBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Surface(
            onClick = onPrevClick,
            enabled = currentPage > 1,
            shape = RoundedCornerShape(9.dp),
            color = if (currentPage > 1) studio.chromeSurface else studio.chromeSurface.copy(alpha = 0.45f),
            modifier = Modifier
                .size(34.dp)
                .testTag("prev_page_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_page),
                    tint = if (currentPage > 1) studio.inkPrimary else studio.inkSubtle.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Surface(
            onClick = onOpenDrawerClick,
            shape = RoundedCornerShape(9.dp),
            color = studio.chromeSurface,
            modifier = Modifier
                .height(34.dp)
                .testTag("open_drawer_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = stringResource(R.string.open_pages_drawer),
                    tint = studio.orangeAccent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Page ",
                    style = MaterialTheme.typography.labelLarge,
                    color = studio.inkPrimary
                )
                Text(
                    text = "$currentPage",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = studio.orangeAccent
                )
                Text(
                    text = "/ $totalPages",
                    style = MaterialTheme.typography.labelLarge,
                    color = studio.inkSubtle
                )
            }
        }

        Surface(
            onClick = onNextClick,
            enabled = currentPage < totalPages,
            shape = RoundedCornerShape(9.dp),
            color = if (currentPage < totalPages) studio.chromeSurface else studio.chromeSurface.copy(alpha = 0.45f),
            modifier = Modifier
                .size(34.dp)
                .testTag("next_page_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_page),
                    tint = if (currentPage < totalPages) studio.inkPrimary else studio.inkSubtle.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun TopIconChip(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val studio = LocalStudioColors.current
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(38.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) studio.chromeMuted else studio.chromeMuted.copy(alpha = 0.5f))
            .clickable(enabled = enabled, onClick = onClick)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (enabled) studio.inkPrimary else studio.inkSubtle.copy(alpha = 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun TopPillToggle(
    icon: ImageVector,
    label: String?,
    contentDescription: String,
    active: Boolean,
    activeBg: Color,
    activeContentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    val studio = LocalStudioColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (active) activeBg else studio.chromeMuted,
        contentColor = if (active) activeContentColor else studio.inkMuted,
        modifier = Modifier
            .height(38.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(16.dp)
            )
            if (label != null) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
    }
}
