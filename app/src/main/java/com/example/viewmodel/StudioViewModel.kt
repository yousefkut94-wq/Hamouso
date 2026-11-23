package com.example.viewmodel

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.graphics.PorterDuff
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.DrawingEngine
import com.example.engine.PdfDocumentSession
import com.example.engine.SampleColoringGenerator
import com.example.model.BookMode
import com.example.model.BrushSize
import com.example.model.BrushTool
import com.example.model.PageThumbInfo
import com.example.model.StudioUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private class PageRecord(
        var lineArtBitmap: Bitmap? = null,
        var colorBitmap: Bitmap? = null,
        var hasColoring: Boolean = false,
        val undoStack: ArrayDeque<Bitmap> = ArrayDeque(),
        val redoStack: ArrayDeque<Bitmap> = ArrayDeque()
    )

    private val _uiState = MutableStateFlow(StudioUiState())
    val uiState: StateFlow<StudioUiState> = _uiState.asStateFlow()

    private val _shareIntentEvents = MutableSharedFlow<Intent>()
    val shareIntentEvents: SharedFlow<Intent> = _shareIntentEvents.asSharedFlow()

    private val _fitViewportEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 4)
    val fitViewportEvents: SharedFlow<Unit> = _fitViewportEvents.asSharedFlow()

    private val pages = mutableMapOf<Int, PageRecord>()
    private var pdfSession: PdfDocumentSession? = null
    private var toastJob: Job? = null

    var activeColorBitmap: Bitmap? = null
        private set
    var activeLineArtBitmap: Bitmap? = null
        private set

    init {
        loadPage(1, fitAfterLoad = true)
    }

    private fun getOrCreateRecord(pageNum: Int): PageRecord {
        return pages.getOrPut(pageNum) { PageRecord() }
    }

    fun loadSampleBook() {
        viewModelScope.launch {
            pdfSession?.close()
            pdfSession = null
            clearAllPageBitmaps()
            _uiState.update {
                it.copy(
                    bookMode = BookMode.SAMPLE,
                    bookTitle = "Kids Sample Coloring Book",
                    totalPages = 3,
                    currentPage = 1
                )
            }
            loadPageInternal(1, fitAfterLoad = true)
            showToast("Loaded 3-page Sample Coloring Book!")
        }
    }

    fun openPdfUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadingTitle = "Opening PDF Coloring Book…",
                    loadingSubtitle = "Extracting crisp pages"
                )
            }
            try {
                val session = PdfDocumentSession.createFromUri(getApplication(), uri)
                val count = session.open()
                if (count <= 0) {
                    session.close()
                    showToast("PDF has no readable pages.")
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch
                }

                pdfSession?.close()
                pdfSession = session
                clearAllPageBitmaps()

                _uiState.update {
                    it.copy(
                        bookMode = BookMode.PDF,
                        bookTitle = session.title,
                        totalPages = count,
                        currentPage = 1
                    )
                }
                loadPageInternal(1, fitAfterLoad = true)
                val plural = if (count > 1) "s" else ""
                showToast("Loaded \"${session.title}\" ($count page$plural)")
            } catch (e: Exception) {
                showToast("Could not open PDF. Please check the file.")
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun loadPage(pageNum: Int, fitAfterLoad: Boolean = true) {
        viewModelScope.launch {
            loadPageInternal(pageNum, fitAfterLoad)
        }
    }

    private suspend fun loadPageInternal(pageNum: Int, fitAfterLoad: Boolean) {
        val total = _uiState.value.totalPages
        if (pageNum < 1 || pageNum > total) return

        _uiState.update {
            it.copy(
                isLoading = true,
                loadingTitle = "Opening Page $pageNum of $total…",
                loadingSubtitle = "Preparing crisp outlines"
            )
        }

        try {
            val rec = getOrCreateRecord(pageNum)
            val lineArt = rec.lineArtBitmap ?: withContext(Dispatchers.Default) {
                val mode = _uiState.value.bookMode
                val session = pdfSession
                if (mode == BookMode.PDF && session != null) {
                    session.renderLineArtPage(pageNum)
                } else {
                    SampleColoringGenerator.generatePage(pageNum)
                }
            }.also { rec.lineArtBitmap = it }

            val colorBmp = rec.colorBitmap ?: Bitmap.createBitmap(
                lineArt.width,
                lineArt.height,
                Bitmap.Config.ARGB_8888
            ).also { rec.colorBitmap = it }

            activeLineArtBitmap = lineArt
            activeColorBitmap = colorBmp

            _uiState.update {
                it.copy(
                    currentPage = pageNum,
                    pageWidth = lineArt.width,
                    pageHeight = lineArt.height,
                    canUndo = rec.undoStack.isNotEmpty(),
                    canRedo = rec.redoStack.isNotEmpty(),
                    isLoading = false,
                    renderTick = it.renderTick + 1L
                )
            }

            if (fitAfterLoad) {
                _fitViewportEvents.tryEmit(Unit)
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
            showToast("Could not render page.")
        }
    }

    fun selectTool(tool: BrushTool) {
        _uiState.update { it.copy(activeTool = tool) }
    }

    fun selectBrushSize(size: BrushSize) {
        _uiState.update { it.copy(activeBrushSize = size) }
    }

    fun selectColor(color: Color) {
        _uiState.update { state ->
            val nextTool = if (state.activeTool == BrushTool.ERASER || state.activeTool == BrushTool.PAN) {
                BrushTool.CRAYON
            } else {
                state.activeTool
            }
            state.copy(
                activeColor = color,
                activeTool = nextTool
            )
        }
    }

    fun togglePalmLock() {
        _uiState.update { state ->
            val next = !state.palmLockEnabled
            showToast(if (next) "Palm Lock ON — Only stylus draws" else "Palm Lock OFF — Finger & stylus draw")
            state.copy(palmLockEnabled = next)
        }
    }

    fun toggleKidLock() {
        _uiState.update { state ->
            val next = !state.kidLockEnabled
            showToast(if (next) "Kid Lock ON — Extra buttons hidden" else "Kid Lock OFF")
            state.copy(kidLockEnabled = next)
        }
    }

    fun toggleFullscreen() {
        _uiState.update { state ->
            state.copy(fullscreenEnabled = !state.fullscreenEnabled)
        }
        _fitViewportEvents.tryEmit(Unit)
    }

    fun beginStroke() {
        val current = _uiState.value.currentPage
        val rec = pages[current] ?: return
        val colorBmp = rec.colorBitmap ?: return

        pushUndoSnapshot(rec, colorBmp)
        rec.hasColoring = true
        _uiState.update {
            it.copy(
                hasDrawnYet = true,
                canUndo = rec.undoStack.isNotEmpty(),
                canRedo = rec.redoStack.isNotEmpty()
            )
        }
    }

    fun drawStrokeSegment(
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        pressure: Float
    ) {
        val colorBmp = activeColorBitmap ?: return
        val state = _uiState.value
        DrawingEngine.drawBrushStroke(
            targetBitmap = colorBmp,
            tool = state.activeTool,
            argbColor = state.activeColor.toArgb(),
            brushSizePx = state.activeBrushSize.sizePx,
            x1 = x1,
            y1 = y1,
            x2 = x2,
            y2 = y2,
            pressure = pressure
        )
        _uiState.update { it.copy(renderTick = it.renderTick + 1L) }
    }

    fun performFloodFill(x: Int, y: Int) {
        val pageNum = _uiState.value.currentPage
        val rec = pages[pageNum] ?: return
        val colorBmp = rec.colorBitmap ?: return
        val lineArtBmp = rec.lineArtBitmap ?: return
        val fillColor = _uiState.value.activeColor.toArgb()

        viewModelScope.launch {
            val snapshot = colorBmp.copy(Bitmap.Config.ARGB_8888, false)
            val result = withContext(Dispatchers.Default) {
                DrawingEngine.scanlineFloodFill(colorBmp, lineArtBmp, x, y, fillColor)
            }
            when (result) {
                DrawingEngine.FloodFillResult.Success -> {
                    if (rec.undoStack.size >= 25) rec.undoStack.removeFirst()
                    rec.undoStack.addLast(snapshot)
                    rec.redoStack.clear()
                    rec.hasColoring = true
                    _uiState.update {
                        it.copy(
                            hasDrawnYet = true,
                            canUndo = rec.undoStack.isNotEmpty(),
                            canRedo = false,
                            renderTick = it.renderTick + 1L
                        )
                    }
                }
                DrawingEngine.FloodFillResult.HitLineArt -> {
                    snapshot.recycle()
                    showToast("Tap inside a white space to fill!")
                }
                DrawingEngine.FloodFillResult.AlreadyFilled -> {
                    snapshot.recycle()
                }
            }
        }
    }

    fun pickColorAt(x: Int, y: Int) {
        val colorBmp = activeColorBitmap ?: return
        val sampled = DrawingEngine.sampleColorAt(colorBmp, x, y) ?: return
        val pickedColor = Color(sampled)
        val hex = String.format("#%06X", 0xFFFFFF and sampled)
        _uiState.update {
            it.copy(
                activeColor = pickedColor,
                activeTool = BrushTool.CRAYON
            )
        }
        showToast(if (sampled == AndroidColor.WHITE) "Picked white paper" else "Picked $hex")
    }

    fun undo() {
        val rec = pages[_uiState.value.currentPage] ?: return
        val colorBmp = rec.colorBitmap ?: return
        if (rec.undoStack.isEmpty()) return

        val currentSnapshot = colorBmp.copy(Bitmap.Config.ARGB_8888, false)
        rec.redoStack.addLast(currentSnapshot)

        val prev = rec.undoStack.removeLast()
        val canvas = android.graphics.Canvas(colorBmp)
        canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(prev, 0f, 0f, null)
        prev.recycle()

        _uiState.update {
            it.copy(
                canUndo = rec.undoStack.isNotEmpty(),
                canRedo = rec.redoStack.isNotEmpty(),
                renderTick = it.renderTick + 1L
            )
        }
    }

    fun redo() {
        val rec = pages[_uiState.value.currentPage] ?: return
        val colorBmp = rec.colorBitmap ?: return
        if (rec.redoStack.isEmpty()) return

        val currentSnapshot = colorBmp.copy(Bitmap.Config.ARGB_8888, false)
        rec.undoStack.addLast(currentSnapshot)

        val next = rec.redoStack.removeLast()
        val canvas = android.graphics.Canvas(colorBmp)
        canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)
        canvas.drawBitmap(next, 0f, 0f, null)
        next.recycle()

        _uiState.update {
            it.copy(
                canUndo = rec.undoStack.isNotEmpty(),
                canRedo = rec.redoStack.isNotEmpty(),
                renderTick = it.renderTick + 1L
            )
        }
    }

    fun requestClearPage() {
        _uiState.update { it.copy(showClearDialog = true) }
    }

    fun dismissClearPage() {
        _uiState.update { it.copy(showClearDialog = false) }
    }

    fun confirmClearPage() {
        val rec = pages[_uiState.value.currentPage] ?: return
        val colorBmp = rec.colorBitmap ?: return

        pushUndoSnapshot(rec, colorBmp)
        val canvas = android.graphics.Canvas(colorBmp)
        canvas.drawColor(AndroidColor.TRANSPARENT, PorterDuff.Mode.CLEAR)
        rec.hasColoring = false

        _uiState.update {
            it.copy(
                showClearDialog = false,
                canUndo = rec.undoStack.isNotEmpty(),
                canRedo = rec.redoStack.isNotEmpty(),
                renderTick = it.renderTick + 1L
            )
        }
        showToast("Page cleared!")
    }

    fun openThumbnailDrawer() {
        _uiState.update { it.copy(showThumbnailDrawer = true) }
        viewModelScope.launch {
            val total = _uiState.value.totalPages
            val mode = _uiState.value.bookMode
            val session = pdfSession
            val thumbs = withContext(Dispatchers.Default) {
                val list = mutableListOf<PageThumbInfo>()
                for (i in 1..total) {
                    val rec = getOrCreateRecord(i)
                    val lineArt = rec.lineArtBitmap ?: if (mode == BookMode.PDF && session != null) {
                        session.renderLineArtPage(i)
                    } else {
                        SampleColoringGenerator.generatePage(i)
                    }.also { rec.lineArtBitmap = it }

                    val thumbBmp = DrawingEngine.buildThumbnail(rec.colorBitmap, lineArt)
                    list.add(
                        PageThumbInfo(
                            pageNumber = i,
                            hasColoring = rec.hasColoring,
                            thumbnail = thumbBmp
                        )
                    )
                }
                list
            }
            _uiState.update { it.copy(thumbnails = thumbs) }
        }
    }

    fun closeThumbnailDrawer() {
        _uiState.update { it.copy(showThumbnailDrawer = false) }
    }

    fun openCustomColorDialog() {
        _uiState.update { it.copy(showCustomColorDialog = true) }
    }

    fun closeCustomColorDialog() {
        _uiState.update { it.copy(showCustomColorDialog = false) }
    }

    fun exportCurrentPage() {
        val colorBmp = activeColorBitmap ?: return
        val lineArtBmp = activeLineArtBitmap ?: return
        val state = _uiState.value
        val slug = state.bookTitle.lowercase().replace(Regex("\\s+"), "-").replace(Regex("[^a-z0-9-]"), "")
        val fileName = "${slug.ifBlank { "chromastudio" }}-page-${state.currentPage}.png"

        viewModelScope.launch {
            val (savedToGallery, shareUri) = withContext(Dispatchers.IO) {
                val composite = DrawingEngine.buildCompositePage(colorBmp, lineArtBmp)
                val res = DrawingEngine.exportPngToGalleryAndShareUri(getApplication(), composite, fileName)
                composite.recycle()
                res
            }

            if (savedToGallery) {
                showToast("Saved PNG to Pictures/ChromaStudio!")
            } else {
                showToast("PNG ready to share!")
            }

            if (shareUri != null) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, shareUri)
                    putExtra(Intent.EXTRA_SUBJECT, "ChromaStudio Artwork")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                _shareIntentEvents.emit(Intent.createChooser(shareIntent, "Save or Share Artwork"))
            }
        }
    }

    fun showToast(message: String) {
        toastJob?.cancel()
        _uiState.update { it.copy(toastMessage = message) }
        toastJob = viewModelScope.launch {
            delay(2400L)
            _uiState.update { it.copy(toastMessage = null) }
        }
    }

    private fun pushUndoSnapshot(rec: PageRecord, colorBmp: Bitmap) {
        if (rec.undoStack.size >= 25) {
            rec.undoStack.removeFirst().recycle()
        }
        rec.undoStack.addLast(colorBmp.copy(Bitmap.Config.ARGB_8888, false))
        rec.redoStack.forEach { it.recycle() }
        rec.redoStack.clear()
    }

    private fun clearAllPageBitmaps() {
        pages.values.forEach { rec ->
            rec.lineArtBitmap?.recycle()
            rec.colorBitmap?.recycle()
            rec.undoStack.forEach { it.recycle() }
            rec.redoStack.forEach { it.recycle() }
        }
        pages.clear()
        activeColorBitmap = null
        activeLineArtBitmap = null
    }

    override fun onCleared() {
        super.onCleared()
        pdfSession?.close()
        clearAllPageBitmaps()
    }
}
