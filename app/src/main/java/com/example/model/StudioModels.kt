package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color

enum class BrushTool(val label: String) {
    MARKER("Marker"),
    PENCIL("Pencil"),
    CRAYON("Crayon"),
    WATERCOLOR("Water"),
    GEL_PEN("Gel Pen"),
    FILL("Fill"),
    ERASER("Eraser"),
    EYEDROPPER("Picker"),
    PAN("Move")
}

enum class BrushSize(val sizePx: Float, val dotDp: Float, val label: String) {
    SMALL(8f, 6f, "Small Brush"),
    MEDIUM(18f, 12f, "Medium Brush"),
    LARGE(34f, 16f, "Large Brush"),
    XLARGE(56f, 22f, "Extra Large Brush")
}

enum class BookMode {
    SAMPLE,
    PDF
}

val STUDIO_PALETTE_HEX = listOf(
    "#FF595E", "#FF924C", "#FFCA3A", "#8AC926",
    "#1982C4", "#6A4C93", "#FF70A6", "#00BBF9",
    "#2EC4B6", "#A47148", "#F7D1BA", "#FFCCD5",
    "#CDB4DB", "#52B788", "#343A40", "#FFFFFF"
)

val STUDIO_PALETTE_COLORS: List<Color> = STUDIO_PALETTE_HEX.map { hex ->
    Color(android.graphics.Color.parseColor(hex))
}

data class PageThumbInfo(
    val pageNumber: Int,
    val hasColoring: Boolean,
    val thumbnail: Bitmap
)

data class StudioUiState(
    val bookMode: BookMode = BookMode.SAMPLE,
    val bookTitle: String = "Kids Sample Coloring Book",
    val currentPage: Int = 1,
    val totalPages: Int = 3,
    val pageWidth: Int = 1000,
    val pageHeight: Int = 1300,
    val activeTool: BrushTool = BrushTool.CRAYON,
    val activeColor: Color = Color(0xFFFF595E),
    val activeBrushSize: BrushSize = BrushSize.MEDIUM,
    val palmLockEnabled: Boolean = false,
    val kidLockEnabled: Boolean = false,
    val fullscreenEnabled: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val isLoading: Boolean = false,
    val loadingTitle: String = "",
    val loadingSubtitle: String = "",
    val showClearDialog: Boolean = false,
    val showThumbnailDrawer: Boolean = false,
    val showCustomColorDialog: Boolean = false,
    val thumbnails: List<PageThumbInfo> = emptyList(),
    val hasDrawnYet: Boolean = false,
    val toastMessage: String? = null,
    val renderTick: Long = 0L
)
