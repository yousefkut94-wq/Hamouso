package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.BrushTool
import com.example.model.StudioUiState
import com.example.ui.theme.LocalStudioColors
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Composable
fun ColoringCanvasViewport(
    uiState: StudioUiState,
    colorBitmap: Bitmap?,
    lineArtBitmap: Bitmap?,
    fitEvents: SharedFlow<Unit>,
    onBeginStroke: () -> Unit,
    onDrawSegment: (x1: Float, y1: Float, x2: Float, y2: Float, pressure: Float) -> Unit,
    onFloodFill: (x: Int, y: Int) -> Unit,
    onPickColor: (x: Int, y: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val studio = LocalStudioColors.current
    val density = LocalDensity.current

    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var panX by remember { mutableFloatStateOf(0f) }
    var panY by remember { mutableFloatStateOf(0f) }

    var hoverPos by remember { mutableStateOf<Offset?>(null) }

    fun fitPageToViewport() {
        if (viewportSize.width == 0 || viewportSize.height == 0) return
        val padPx = with(density) { if (viewportSize.width < 1200) 20.dp.toPx() else 40.dp.toPx() }
        val availW = max(60f, viewportSize.width - padPx)
        val availH = max(60f, viewportSize.height - padPx)
        val pw = uiState.pageWidth.toFloat().coerceAtLeast(1f)
        val ph = uiState.pageHeight.toFloat().coerceAtLeast(1f)

        val nextZoom = min(availW / pw, availH / ph)
        val renderedW = pw * nextZoom
        val renderedH = ph * nextZoom

        zoom = nextZoom
        panX = (viewportSize.width - renderedW) / 2f
        panY = (viewportSize.height - renderedH) / 2f
    }

    fun zoomAroundPoint(factor: Float, px: Float, py: Float) {
        val prevZoom = zoom
        val nextZoom = (prevZoom * factor).coerceIn(0.15f, 5.0f)
        val ratio = nextZoom / prevZoom
        panX = px - (px - panX) * ratio
        panY = py - (py - panY) * ratio
        zoom = nextZoom
    }

    LaunchedEffect(viewportSize, uiState.pageWidth, uiState.pageHeight) {
        if (viewportSize != IntSize.Zero && zoom == 1f && panX == 0f && panY == 0f) {
            fitPageToViewport()
        }
    }

    LaunchedEffect(fitEvents) {
        fitEvents.collect {
            fitPageToViewport()
        }
    }

    val colorImageBitmap = remember(colorBitmap, uiState.renderTick) {
        colorBitmap?.asImageBitmap()
    }
    val lineArtImageBitmap = remember(lineArtBitmap) {
        lineArtBitmap?.asImageBitmap()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(studio.deskBackground)
            .clipToBounds()
            .onSizeChanged { newSize ->
                val wasZero = viewportSize == IntSize.Zero
                viewportSize = newSize
                if (wasZero) {
                    fitPageToViewport()
                }
            }
            // Track stylus / mouse hover for brush ring preview
            .pointerInput(uiState.activeTool, uiState.activeBrushSize, zoom) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull()
                        if (change != null &&
                            (change.type == PointerType.Mouse || change.type == PointerType.Stylus)
                        ) {
                            when (event.type) {
                                PointerEventType.Move, PointerEventType.Enter -> {
                                    hoverPos = change.position
                                }
                                PointerEventType.Exit -> {
                                    hoverPos = null
                                }
                                else -> Unit
                            }
                        } else if (event.type == PointerEventType.Exit) {
                            hoverPos = null
                        }
                    }
                }
            }
            // Main 1-finger draw + 2-finger simultaneous pinch & pan gesture handler
            .pointerInput(
                uiState.activeTool,
                uiState.palmLockEnabled,
                uiState.pageWidth,
                uiState.pageHeight
            ) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val activePointers = mutableMapOf<PointerId, Offset>()
                    activePointers[down.id] = down.position

                    var isDrawing = false
                    var isPanning = false
                    var isPinching = false
                    var lastCanvasX = 0f
                    var lastCanvasY = 0f
                    var lastPanPos = down.position
                    var prevPinchDist = 0f
                    var prevPinchMid = Offset.Zero

                    fun toCanvas(pos: Offset): Offset {
                        return Offset(
                            x = (pos.x - panX) / zoom,
                            y = (pos.y - panY) / zoom
                        )
                    }

                    val isPalmBlocked = uiState.palmLockEnabled && down.type == PointerType.Touch

                    when {
                        uiState.activeTool == BrushTool.PAN -> {
                            isPanning = true
                            lastPanPos = down.position
                        }
                        !isPalmBlocked -> {
                            val mapped = toCanvas(down.position)
                            when (uiState.activeTool) {
                                BrushTool.EYEDROPPER -> {
                                    onPickColor(mapped.x.toInt(), mapped.y.toInt())
                                }
                                BrushTool.FILL -> {
                                    onFloodFill(mapped.x.toInt(), mapped.y.toInt())
                                }
                                else -> {
                                    isDrawing = true
                                    lastCanvasX = mapped.x
                                    lastCanvasY = mapped.y
                                    onBeginStroke()
                                    onDrawSegment(
                                        mapped.x,
                                        mapped.y,
                                        mapped.x + 0.01f,
                                        mapped.y + 0.01f,
                                        down.pressure
                                    )
                                }
                            }
                        }
                    }

                    while (true) {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                activePointers[change.id] = change.position
                            } else {
                                activePointers.remove(change.id)
                            }
                        }

                        if (activePointers.isEmpty()) {
                            break
                        }

                        // Transition to 2-finger pinch & pan when 2 pointers are active
                        if (activePointers.size >= 2) {
                            val pts = activePointers.values.take(2)
                            val dist = hypot(
                                (pts[1].x - pts[0].x).toDouble(),
                                (pts[1].y - pts[0].y).toDouble()
                            ).toFloat()
                            val mid = Offset(
                                (pts[0].x + pts[1].x) / 2f,
                                (pts[0].y + pts[1].y) / 2f
                            )

                            if (!isPinching) {
                                isDrawing = false
                                isPanning = false
                                isPinching = true
                                prevPinchDist = dist
                                prevPinchMid = mid
                            } else {
                                panX += mid.x - prevPinchMid.x
                                panY += mid.y - prevPinchMid.y
                                if (prevPinchDist > 10f && dist > 10f) {
                                    zoomAroundPoint(dist / prevPinchDist, mid.x, mid.y)
                                }
                                prevPinchDist = dist
                                prevPinchMid = mid
                            }
                            event.changes.forEach { it.consume() }
                            continue
                        }

                        // Single active pointer
                        val singleChange = event.changes.firstOrNull { it.pressed } ?: continue
                        if (isPinching) {
                            // Wait for full lift before resuming drawing after pinch
                            continue
                        }

                        if (isPanning) {
                            val delta = singleChange.position - lastPanPos
                            panX += delta.x
                            panY += delta.y
                            lastPanPos = singleChange.position
                            singleChange.consume()
                        } else if (isDrawing) {
                            val mapped = toCanvas(singleChange.position)
                            onDrawSegment(
                                lastCanvasX,
                                lastCanvasY,
                                mapped.x,
                                mapped.y,
                                singleChange.pressure
                            )
                            lastCanvasX = mapped.x
                            lastCanvasY = mapped.y
                            singleChange.consume()
                        }
                    }
                }
            }
    ) {
        // Tactile dotted desk background + 3-Layer Paper Stack
        Canvas(modifier = Modifier.fillMaxSize()) {
            // 1. Studio desk dot grid
            val step = 20.dp.toPx()
            val dotRadius = 1.25.dp.toPx()
            var y = step / 2f
            while (y < size.height) {
                var x = step / 2f
                while (x < size.width) {
                    drawCircle(
                        color = studio.deskDots,
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                    x += step
                }
                y += step
            }

            // 2. Transformed 3-Layer Paper Stack
            val pw = uiState.pageWidth.toFloat()
            val ph = uiState.pageHeight.toFloat()

            withTransform({
                translate(left = panX, top = panY)
                scale(scaleX = zoom, scaleY = zoom, pivot = Offset.Zero)
            }) {
                // Soft paper drop shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.16f),
                    topLeft = Offset(4f / zoom, 10f / zoom),
                    size = Size(pw, ph),
                    cornerRadius = CornerRadius(8f / zoom, 8f / zoom)
                )

                // Layer 1: Crisp White Paper
                drawRoundRect(
                    color = studio.paperWhite,
                    topLeft = Offset.Zero,
                    size = Size(pw, ph),
                    cornerRadius = CornerRadius(6f / zoom, 6f / zoom)
                )

                // Layer 2: User Coloring Layer
                colorImageBitmap?.let { bmp ->
                    drawImage(
                        image = bmp,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(uiState.pageWidth, uiState.pageHeight)
                    )
                }

                // Layer 3: Crisp Black Outlines on Top
                lineArtImageBitmap?.let { bmp ->
                    drawImage(
                        image = bmp,
                        dstOffset = IntOffset.Zero,
                        dstSize = IntSize(uiState.pageWidth, uiState.pageHeight)
                    )
                }
            }

            // 3. Hover / Stylus Brush Ring Preview
            val hover = hoverPos
            val showRing =
                uiState.activeTool !in listOf(BrushTool.FILL, BrushTool.EYEDROPPER, BrushTool.PAN)
            if (hover != null && showRing) {
                val radiusPx = max(3f, (uiState.activeBrushSize.sizePx * zoom) / 2f)
                drawCircle(
                    color = Color.White.copy(alpha = 0.9f),
                    radius = radiusPx + 1.5f,
                    center = hover,
                    style = Stroke(width = 2f)
                )
                drawCircle(
                    color = studio.inkPrimary.copy(alpha = 0.85f),
                    radius = radiusPx,
                    center = hover,
                    style = Stroke(width = 2f)
                )
            }
        }

        // Subtle auto-fading Gesture Hint Pill
        AnimatedVisibility(
            visible = !uiState.hasDrawnYet,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(studio.inkPrimary.copy(alpha = 0.78f))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = stringResource(R.string.gesture_hint),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White
                )
            }
        }

        // Floating Bottom-Right Zoom Pill (Hidden in Fullscreen)
        if (!uiState.fullscreenEnabled) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .testTag("zoom_pill"),
                shape = RoundedCornerShape(14.dp),
                color = studio.chromeSurface.copy(alpha = 0.96f),
                shadowElevation = 6.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, studio.chromeBorder)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        onClick = {
                            zoomAroundPoint(
                                0.8f,
                                viewportSize.width / 2f,
                                viewportSize.height / 2f
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("zoom_out_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ZoomOut,
                                contentDescription = stringResource(R.string.zoom_out),
                                tint = studio.inkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Surface(
                        onClick = { fitPageToViewport() },
                        shape = RoundedCornerShape(10.dp),
                        color = studio.orangeSoft,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("zoom_fit_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${(zoom * 100f).roundToInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = studio.orangeAccent
                            )
                        }
                    }

                    Surface(
                        onClick = {
                            zoomAroundPoint(
                                1.25f,
                                viewportSize.width / 2f,
                                viewportSize.height / 2f
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("zoom_in_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.ZoomIn,
                                contentDescription = stringResource(R.string.zoom_in),
                                tint = studio.inkMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
