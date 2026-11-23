package com.example.ui.components

import android.graphics.Color as AndroidColor
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.model.PageThumbInfo
import com.example.model.StudioUiState
import com.example.ui.theme.LocalStudioColors

@Composable
fun PageThumbnailDrawerDialog(
    uiState: StudioUiState,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val studio = LocalStudioColors.current
    if (!uiState.showThumbnailDrawer) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 580.dp),
            shape = RoundedCornerShape(26.dp),
            color = studio.chromeSurface,
            shadowElevation = 16.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.drawer_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = studio.inkPrimary
                        )
                        val subtitle = if (uiState.totalPages == 1) {
                            stringResource(R.string.drawer_subtitle_single, uiState.bookTitle)
                        } else {
                            stringResource(
                                R.string.drawer_subtitle_format,
                                uiState.bookTitle,
                                uiState.totalPages
                            )
                        }
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = studio.inkSubtle
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(studio.chromeMuted)
                            .testTag("close_drawer_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.close_drawer),
                            tint = studio.inkMuted
                        )
                    }
                }

                HorizontalDivider(color = studio.chromeBorder)

                if (uiState.thumbnails.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = studio.orangeAccent)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 128.dp),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(uiState.thumbnails, key = { it.pageNumber }) { item ->
                            ThumbnailPageCard(
                                item = item,
                                isCurrent = item.pageNumber == uiState.currentPage,
                                onClick = { onSelectPage(item.pageNumber) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThumbnailPageCard(
    item: PageThumbInfo,
    isCurrent: Boolean,
    onClick: () -> Unit
) {
    val studio = LocalStudioColors.current
    val bmp = remember(item.thumbnail) { item.thumbnail.asImageBitmap() }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isCurrent) studio.orangeSoft else studio.chromeMuted,
        border = BorderStroke(
            width = 2.dp,
            color = if (isCurrent) studio.orangeAccent else studio.chromeBorder
        ),
        modifier = Modifier.testTag("thumbnail_page_${item.pageNumber}")
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(studio.paperWhite)
                    .border(1.dp, studio.chromeBorder, RoundedCornerShape(12.dp))
            ) {
                Image(
                    bitmap = bmp,
                    contentDescription = "Page ${item.pageNumber}",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )

                if (item.hasColoring) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(studio.emeraldBadge)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.colored_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }

            Text(
                text = "Page ${item.pageNumber}",
                style = MaterialTheme.typography.labelLarge,
                color = if (isCurrent) studio.orangeAccent else studio.inkPrimary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

@Composable
fun ClearPageConfirmDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val studio = LocalStudioColors.current
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = studio.chromeSurface,
        title = {
            Text(
                text = stringResource(R.string.clear_modal_title),
                style = MaterialTheme.typography.titleMedium,
                color = studio.inkPrimary
            )
        },
        text = {
            Text(
                text = stringResource(R.string.clear_modal_body),
                style = MaterialTheme.typography.bodyMedium,
                color = studio.inkSubtle
            )
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_clear_button")
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge,
                    color = studio.inkMuted
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = studio.dangerText,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("confirm_clear_button")
            ) {
                Text(
                    text = stringResource(R.string.confirm_clear),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    )
}

@Composable
fun CustomColorPickerDialog(
    visible: Boolean,
    initialColor: Color,
    onSelectColor: (Color) -> Unit,
    onDismiss: () -> Unit
) {
    val studio = LocalStudioColors.current
    if (!visible) return

    val initialHsv = remember(initialColor) {
        FloatArray(3).also { AndroidColor.colorToHSV(initialColor.toArgb(), it) }
    }
    var hue by remember(initialColor) { mutableFloatStateOf(initialHsv[0]) }
    var sat by remember(initialColor) { mutableFloatStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(initialHsv[2]) }

    val pickedColor = remember(hue, sat, value) {
        Color(AndroidColor.HSVToColor(floatArrayOf(hue, sat, value)))
    }
    val pickedHex = remember(pickedColor) {
        String.format("#%06X", 0xFFFFFF and pickedColor.toArgb())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = studio.chromeSurface,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.custom_color_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = studio.inkPrimary
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(pickedColor)
                                .border(1.5.dp, studio.chromeBorder, CircleShape)
                        )
                        Text(
                            text = pickedHex,
                            style = MaterialTheme.typography.labelLarge,
                            color = studio.inkMuted
                        )
                    }
                }

                // 2D Saturation & Value picker pad
                val hueColor = remember(hue) {
                    Color(AndroidColor.HSVToColor(floatArrayOf(hue, 1f, 1f)))
                }
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .pointerInput(Unit) {
                            detectTapGestures { pos ->
                                sat = (pos.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (pos.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                sat = (change.position.x / size.width).coerceIn(0f, 1f)
                                value = 1f - (change.position.y / size.height).coerceIn(0f, 1f)
                            }
                        }
                ) {
                    drawRect(
                        brush = Brush.horizontalGradient(listOf(Color.White, hueColor))
                    )
                    drawRect(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black))
                    )
                    val cx = sat * size.width
                    val cy = (1f - value) * size.height
                    drawCircle(
                        color = Color.White,
                        radius = 10.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = studio.inkPrimary,
                        radius = 11.dp.toPx(),
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }

                // Hue Rainbow Slider
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFF0000),
                                        Color(0xFFFFFF00),
                                        Color(0xFF00FF00),
                                        Color(0xFF00FFFF),
                                        Color(0xFF0000FF),
                                        Color(0xFFFF00FF),
                                        Color(0xFFFF0000)
                                    )
                                )
                            )
                    )
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = hueColor,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = stringResource(R.string.cancel),
                            style = MaterialTheme.typography.labelLarge,
                            color = studio.inkMuted
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSelectColor(pickedColor)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = studio.orangeAccent,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("apply_custom_color_button")
                    ) {
                        Text(
                            text = stringResource(R.string.apply_color),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioLoadingOverlay(
    visible: Boolean,
    title: String,
    subtitle: String
) {
    val studio = LocalStudioColors.current
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(studio.inkPrimary.copy(alpha = 0.28f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = studio.chromeSurface,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, studio.chromeBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    CircularProgressIndicator(
                        color = studio.orangeAccent,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(26.dp)
                    )
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = studio.inkPrimary
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = studio.inkSubtle
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudioToastBanner(
    message: String?,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    AnimatedVisibility(
        visible = message != null,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Surface(
            shape = CircleShape,
            color = studio.inkPrimary,
            shadowElevation = 8.dp
        ) {
            Text(
                text = message.orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
            )
        }
    }
}
