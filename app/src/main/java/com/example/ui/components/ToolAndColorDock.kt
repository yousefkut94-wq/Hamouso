package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Create
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Draw
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FormatColorFill
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrushSize
import com.example.model.BrushTool
import com.example.model.STUDIO_PALETTE_COLORS
import com.example.model.StudioUiState
import com.example.ui.theme.LocalStudioColors

private fun BrushTool.iconVector(): ImageVector = when (this) {
    BrushTool.MARKER -> Icons.Rounded.Brush
    BrushTool.PENCIL -> Icons.Rounded.Edit
    BrushTool.CRAYON -> Icons.Rounded.Create
    BrushTool.WATERCOLOR -> Icons.Rounded.WaterDrop
    BrushTool.GEL_PEN -> Icons.Rounded.Draw
    BrushTool.FILL -> Icons.Rounded.FormatColorFill
    BrushTool.ERASER -> Icons.Rounded.CleaningServices
    BrushTool.EYEDROPPER -> Icons.Rounded.Colorize
    BrushTool.PAN -> Icons.Rounded.OpenWith
}

@Composable
fun PortraitBottomDock(
    uiState: StudioUiState,
    onSelectTool: (BrushTool) -> Unit,
    onSelectSize: (BrushSize) -> Unit,
    onSelectColor: (Color) -> Unit,
    onOpenCustomColor: () -> Unit,
    onClearPageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    val showClear = !uiState.fullscreenEnabled && !uiState.kidLockEnabled

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = studio.chromeSurface,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, studio.chromeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // Row 1: 9 Brush Tools
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BrushTool.entries.forEach { tool ->
                    ToolChipButton(
                        tool = tool,
                        selected = uiState.activeTool == tool,
                        onClick = { onSelectTool(tool) },
                        modifier = Modifier.width(60.dp)
                    )
                }
            }

            // Row 2: Brush Size Dots + Clear Page Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(studio.chromeMuted)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BrushSize.entries.forEach { sizeOption ->
                        SizeDotButton(
                            brushSize = sizeOption,
                            selected = uiState.activeBrushSize == sizeOption,
                            onClick = { onSelectSize(sizeOption) },
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                if (showClear) {
                    Surface(
                        onClick = onClearPageClick,
                        shape = RoundedCornerShape(12.dp),
                        color = studio.dangerBg,
                        contentColor = studio.dangerText,
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("clear_page_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = stringResource(R.string.clear_page),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = stringResource(R.string.clear_page),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // Row 3: Custom Color Wheel + 16 Curated Swatches
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomColorWheelButton(
                    onClick = onOpenCustomColor,
                    sizeDp = 38
                )
                STUDIO_PALETTE_COLORS.forEachIndexed { idx, color ->
                    ColorSwatchDot(
                        color = color,
                        selected = uiState.activeColor == color,
                        onClick = { onSelectColor(color) },
                        sizeDp = 38,
                        testTag = "color_swatch_$idx"
                    )
                }
            }
        }
    }
}

@Composable
fun LandscapeLeftToolRail(
    uiState: StudioUiState,
    onSelectTool: (BrushTool) -> Unit,
    onSelectSize: (BrushSize) -> Unit,
    onClearPageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    val showClear = !uiState.fullscreenEnabled && !uiState.kidLockEnabled

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(74.dp),
        color = studio.chromeSurface,
        border = BorderStroke(1.dp, studio.chromeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .displayCutoutPadding()
                .padding(horizontal = 5.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vertical scrollable tools
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BrushTool.entries.forEach { tool ->
                    ToolChipButton(
                        tool = tool,
                        selected = uiState.activeTool == tool,
                        onClick = { onSelectTool(tool) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // 2x2 Brush Size Dots Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(studio.chromeMuted)
                    .padding(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                val sizes = BrushSize.entries
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    SizeDotButton(
                        brushSize = sizes[0],
                        selected = uiState.activeBrushSize == sizes[0],
                        onClick = { onSelectSize(sizes[0]) },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    )
                    SizeDotButton(
                        brushSize = sizes[1],
                        selected = uiState.activeBrushSize == sizes[1],
                        onClick = { onSelectSize(sizes[1]) },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    SizeDotButton(
                        brushSize = sizes[2],
                        selected = uiState.activeBrushSize == sizes[2],
                        onClick = { onSelectSize(sizes[2]) },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    )
                    SizeDotButton(
                        brushSize = sizes[3],
                        selected = uiState.activeBrushSize == sizes[3],
                        onClick = { onSelectSize(sizes[3]) },
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                    )
                }
            }

            if (showClear) {
                Spacer(Modifier.height(6.dp))
                Surface(
                    onClick = onClearPageClick,
                    shape = RoundedCornerShape(10.dp),
                    color = studio.dangerBg,
                    contentColor = studio.dangerText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .testTag("clear_page_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = stringResource(R.string.clear_page),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LandscapeRightColorRail(
    uiState: StudioUiState,
    onSelectColor: (Color) -> Unit,
    onOpenCustomColor: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(66.dp),
        color = studio.chromeSurface,
        border = BorderStroke(1.dp, studio.chromeBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomColorWheelButton(
                onClick = onOpenCustomColor,
                sizeDp = 38
            )
            STUDIO_PALETTE_COLORS.forEachIndexed { idx, color ->
                ColorSwatchDot(
                    color = color,
                    selected = uiState.activeColor == color,
                    onClick = { onSelectColor(color) },
                    sizeDp = 38,
                    testTag = "color_swatch_$idx"
                )
            }
        }
    }
}

@Composable
private fun ToolChipButton(
    tool: BrushTool,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    val bgColor by animateColorAsState(
        targetValue = if (selected) studio.orangeAccent else studio.chromeMuted,
        label = "tool_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) Color.White else studio.inkMuted,
        label = "tool_fg"
    )

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        contentColor = contentColor,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .height(48.dp)
            .testTag("tool_${tool.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = tool.iconVector(),
                contentDescription = tool.label,
                modifier = Modifier.size(17.dp)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = tool.label,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SizeDotButton(
    brushSize: BrushSize,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(9.dp),
        color = if (selected) studio.chromeSurface else Color.Transparent,
        shadowElevation = if (selected) 1.dp else 0.dp,
        modifier = modifier.testTag("size_${brushSize.name.lowercase()}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(brushSize.dotDp.dp)
                    .clip(CircleShape)
                    .background(if (selected) studio.orangeAccent else studio.inkMuted)
            )
        }
    }
}

@Composable
private fun CustomColorWheelButton(
    onClick: () -> Unit,
    sizeDp: Int
) {
    val studio = LocalStudioColors.current
    val rainbow = listOf(
        Color(0xFFFF595E),
        Color(0xFFFFCA3A),
        Color(0xFF8AC926),
        Color(0xFF1982C4),
        Color(0xFF6A4C93),
        Color(0xFFFF595E)
    )
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(Brush.sweepGradient(rainbow))
            .border(2.dp, studio.chromeBorder, CircleShape)
            .clickable(onClick = onClick)
            .testTag("custom_color_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.95f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(R.string.custom_color_title),
                tint = studio.inkPrimary,
                modifier = Modifier.size(13.dp)
            )
        }
    }
}

@Composable
private fun ColorSwatchDot(
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    sizeDp: Int,
    testTag: String
) {
    val studio = LocalStudioColors.current
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(sizeDp.dp)
            .scale(if (selected) 1.1f else 1f)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (selected) 3.dp else 1.5.dp,
                color = if (selected) studio.inkPrimary else studio.chromeBorder,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag(testTag)
    )
}
