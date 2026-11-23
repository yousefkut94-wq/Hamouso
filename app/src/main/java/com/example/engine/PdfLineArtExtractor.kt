package com.example.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class PdfDocumentSession(
    private val cacheFile: File,
    val title: String
) {
    private val mutex = Mutex()
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null

    val pageCount: Int
        get() = renderer?.pageCount ?: 0

    suspend fun open(): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            closeInternal()
            val fd = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            fileDescriptor = fd
            val pdfRenderer = PdfRenderer(fd)
            renderer = pdfRenderer
            pdfRenderer.pageCount
        }
    }

    suspend fun renderLineArtPage(pageNum: Int): Bitmap = withContext(Dispatchers.IO) {
        mutex.withLock {
            val pdfRenderer = renderer ?: throw IllegalStateException("PDF renderer not open")
            val index = (pageNum - 1).coerceIn(0, pdfRenderer.pageCount - 1)
            pdfRenderer.openPage(index).use { page ->
                val baseW = max(1, page.width)
                val baseH = max(1, page.height)
                val targetWidth = 1400f
                val scale = targetWidth / baseW.toFloat()
                val outW = max(100, (baseW * scale).roundToInt())
                val outH = max(100, (baseH * scale).roundToInt())

                val rawBitmap = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(rawBitmap)
                canvas.drawColor(Color.WHITE)
                page.render(rawBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                val lineArt = extractTransparentLineArt(rawBitmap)
                rawBitmap.recycle()
                lineArt
            }
        }
    }

    private fun extractTransparentLineArt(source: Bitmap): Bitmap {
        val w = source.width
        val h = source.height
        val pixels = IntArray(w * h)
        source.getPixels(pixels, 0, w, 0, 0, w, h)

        for (i in pixels.indices) {
            val c = pixels[i]
            val r = (c shr 16) and 0xFF
            val g = (c shr 8) and 0xFF
            val b = c and 0xFF
            val luminance = 0.299 * r + 0.587 * g + 0.114 * b

            pixels[i] = if (luminance > 235.0) {
                0x00000000
            } else {
                val alpha = min(255, max(0, ((245.0 - luminance) * 1.5).toInt()))
                (alpha shl 24) or 0x00111111
            }
        }

        return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).apply {
            setPixels(pixels, 0, w, 0, 0, w, h)
        }
    }

    private fun closeInternal() {
        try {
            renderer?.close()
        } catch (_: Exception) {
        }
        renderer = null
        try {
            fileDescriptor?.close()
        } catch (_: Exception) {
        }
        fileDescriptor = null
    }

    fun close() {
        closeInternal()
        if (cacheFile.exists()) {
            cacheFile.delete()
        }
    }

    companion object {
        suspend fun createFromUri(context: Context, uri: Uri): PdfDocumentSession =
            withContext(Dispatchers.IO) {
                val title = resolveFileName(context, uri)
                val tempFile = File(context.cacheDir, "active_coloring_book.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                } ?: throw IllegalArgumentException("Unable to read selected PDF")

                PdfDocumentSession(tempFile, title)
            }

        private fun resolveFileName(context: Context, uri: Uri): String {
            var name = "Coloring Book"
            context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (idx >= 0) {
                            name = cursor.getString(idx) ?: name
                        }
                    }
                }
            return name.replace(Regex("\\.pdf$", RegexOption.IGNORE_CASE), "").ifBlank { "Coloring Book" }
        }
    }
}
