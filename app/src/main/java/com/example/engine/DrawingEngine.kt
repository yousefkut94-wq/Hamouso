package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RadialGradient
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.BrushTool
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

object DrawingEngine {

    fun drawBrushStroke(
        targetBitmap: Bitmap,
        tool: BrushTool,
        argbColor: Int,
        brushSizePx: Float,
        x1: Float,
        y1: Float,
        x2: Float,
        y2: Float,
        pressure: Float
    ) {
        val canvas = Canvas(targetBitmap)
        val pMult = if (pressure > 0f) (0.65f + pressure * 0.55f) else 1f
        val radius = (brushSizePx * pMult) / 2f
        val r = Color.red(argbColor)
        val g = Color.green(argbColor)
        val b = Color.blue(argbColor)

        when (tool) {
            BrushTool.ERASER -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                    style = Paint.Style.STROKE
                    strokeWidth = brushSizePx * pMult
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                canvas.drawLine(x1, y1, x2, y2, paint)
            }

            BrushTool.MARKER -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(72, r, g, b)
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    style = Paint.Style.STROKE
                    strokeWidth = brushSizePx * pMult
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                canvas.drawLine(x1, y1, x2, y2, paint)
            }

            BrushTool.GEL_PEN -> {
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.argb(250, r, g, b)
                    style = Paint.Style.STROKE
                    strokeWidth = max(2.5f, brushSizePx * 0.32f * pMult)
                    strokeCap = Paint.Cap.ROUND
                    strokeJoin = Paint.Join.ROUND
                }
                canvas.drawLine(x1, y1, x2, y2, paint)
            }

            BrushTool.WATERCOLOR -> {
                val dist = hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
                val stepDiv = max(2f, radius * 0.25f)
                val steps = max(1, ceil(dist / stepDiv).toInt())
                val washRadius = max(4f, radius * 1.35f)
                val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.MULTIPLY)
                    style = Paint.Style.FILL
                }
                val cCenter = Color.argb(22, r, g, b)
                val cMid = Color.argb(14, r, g, b)
                val cEdge = Color.argb(0, r, g, b)
                for (i in 0..steps) {
                    val t = i.toFloat() / steps.toFloat()
                    val cx = x1 + (x2 - x1) * t
                    val cy = y1 + (y2 - y1) * t
                    paint.shader = RadialGradient(
                        cx,
                        cy,
                        washRadius,
                        intArrayOf(cCenter, cMid, cEdge),
                        floatArrayOf(0.1f, 0.7f, 1.0f),
                        Shader.TileMode.CLAMP
                    )
                    canvas.drawCircle(cx, cy, washRadius, paint)
                }
            }

            BrushTool.CRAYON, BrushTool.PENCIL -> {
                val isCrayon = tool == BrushTool.CRAYON
                val dist = hypot((x2 - x1).toDouble(), (y2 - y1).toDouble()).toFloat()
                val stepLen = max(1.5f, radius * (if (isCrayon) 0.22f else 0.3f))
                val steps = max(1, ceil(dist / stepLen).toInt())
                val grains = if (isCrayon) 24 else 14
                val spread = radius * (if (isCrayon) 1.05f else 0.78f)
                val alpha = if (isCrayon) 122 else 92
                val paint = Paint().apply {
                    color = Color.argb(alpha, r, g, b)
                    style = Paint.Style.FILL
                }

                for (i in 0..steps) {
                    val t = i.toFloat() / steps.toFloat()
                    val cx = x1 + (x2 - x1) * t
                    val cy = y1 + (y2 - y1) * t
                    for (gIdx in 0 until grains) {
                        val ang = Random.nextDouble() * PI * 2.0
                        val rad = sqrt(Random.nextDouble()) * spread
                        val dotSize = if (isCrayon) {
                            1.5f + Random.nextFloat() * 2.3f
                        } else {
                            1.0f + Random.nextFloat() * 1.2f
                        }
                        val dx = cx + (cos(ang) * rad).toFloat()
                        val dy = cy + (sin(ang) * rad).toFloat()
                        canvas.drawRect(dx, dy, dx + dotSize, dy + dotSize, paint)
                    }
                }
            }

            BrushTool.FILL, BrushTool.EYEDROPPER, BrushTool.PAN -> Unit
        }
    }

    sealed class FloodFillResult {
        data object Success : FloodFillResult()
        data object HitLineArt : FloodFillResult()
        data object AlreadyFilled : FloodFillResult()
    }

    fun scanlineFloodFill(
        colorBitmap: Bitmap,
        lineArtBitmap: Bitmap,
        startX: Int,
        startY: Int,
        fillArgb: Int
    ): FloodFillResult {
        val w = colorBitmap.width
        val h = colorBitmap.height
        if (startX < 0 || startX >= w || startY < 0 || startY >= h) {
            return FloodFillResult.AlreadyFilled
        }

        val total = w * h
        val linePixels = IntArray(total)
        lineArtBitmap.getPixels(linePixels, 0, w, 0, 0, w, h)

        val startIdx = startY * w + startX
        val startLineAlpha = (linePixels[startIdx] ushr 24) and 0xFF
        if (startLineAlpha > 140) {
            return FloodFillResult.HitLineArt
        }

        val colorPixels = IntArray(total)
        colorBitmap.getPixels(colorPixels, 0, w, 0, 0, w, h)

        val targetColor = colorPixels[startIdx]
        val tr = (targetColor shr 16) and 0xFF
        val tg = (targetColor shr 8) and 0xFF
        val tb = targetColor and 0xFF
        val ta = (targetColor ushr 24) and 0xFF

        val fr = Color.red(fillArgb)
        val fg = Color.green(fillArgb)
        val fb = Color.blue(fillArgb)
        val opaqueFill = (0xFF shl 24) or (fr shl 16) or (fg shl 8) or fb

        if (abs(tr - fr) < 5 && abs(tg - fg) < 5 && abs(tb - fb) < 5 && ta > 240) {
            return FloodFillResult.AlreadyFilled
        }

        val visited = BooleanArray(total)

        fun matches(pixelIdx: Int): Boolean {
            if (visited[pixelIdx]) return false
            val lineAlpha = (linePixels[pixelIdx] ushr 24) and 0xFF
            if (lineAlpha > 125) return false
            val c = colorPixels[pixelIdx]
            val ca = (c ushr 24) and 0xFF
            val cr = (c shr 16) and 0xFF
            val cg = (c shr 8) and 0xFF
            val cb = c and 0xFF
            return abs(cr - tr) <= 36 &&
                abs(cg - tg) <= 36 &&
                abs(cb - tb) <= 36 &&
                abs(ca - ta) <= 64
        }

        // Primitive IntArray stack storing packed coordinates (y * w + x)
        var stack = IntArray(4096)
        var sp = 0
        fun push(x: Int, y: Int) {
            if (sp >= stack.size) {
                stack = stack.copyOf(stack.size * 2)
            }
            stack[sp++] = y * w + x
        }

        push(startX, startY)

        while (sp > 0) {
            val packed = stack[--sp]
            val x = packed % w
            val y = packed / w
            val rowOffset = y * w

            var lx = x
            while (lx >= 0 && matches(rowOffset + lx)) {
                lx--
            }
            lx++

            var rx = x
            while (rx < w && matches(rowOffset + rx)) {
                rx++
            }
            rx--

            var spanAbove = false
            var spanBelow = false

            for (px in lx..rx) {
                val pIdx = rowOffset + px
                visited[pIdx] = true
                colorPixels[pIdx] = opaqueFill

                if (y > 0) {
                    val aboveMatches = matches((y - 1) * w + px)
                    if (!spanAbove && aboveMatches) {
                        push(px, y - 1)
                        spanAbove = true
                    } else if (spanAbove && !aboveMatches) {
                        spanAbove = false
                    }
                }

                if (y < h - 1) {
                    val belowMatches = matches((y + 1) * w + px)
                    if (!spanBelow && belowMatches) {
                        push(px, y + 1)
                        spanBelow = true
                    } else if (spanBelow && !belowMatches) {
                        spanBelow = false
                    }
                }
            }
        }

        colorBitmap.setPixels(colorPixels, 0, w, 0, 0, w, h)
        return FloodFillResult.Success
    }

    fun sampleColorAt(colorBitmap: Bitmap, x: Int, y: Int): Int? {
        if (x < 0 || x >= colorBitmap.width || y < 0 || y >= colorBitmap.height) return null
        val pixel = colorBitmap.getPixel(x, y)
        val alpha = (pixel ushr 24) and 0xFF
        return if (alpha > 20) {
            Color.rgb(Color.red(pixel), Color.green(pixel), Color.blue(pixel))
        } else {
            Color.WHITE
        }
    }

    fun buildCompositePage(
        colorBitmap: Bitmap,
        lineArtBitmap: Bitmap
    ): Bitmap {
        val w = lineArtBitmap.width
        val h = lineArtBitmap.height
        val out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        canvas.drawColor(Color.WHITE)
        canvas.drawBitmap(colorBitmap, 0f, 0f, null)
        canvas.drawBitmap(lineArtBitmap, 0f, 0f, null)
        return out
    }

    fun buildThumbnail(
        colorBitmap: Bitmap?,
        lineArtBitmap: Bitmap,
        width: Int = 210,
        height: Int = 280
    ): Bitmap {
        val thumb = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(thumb)
        canvas.drawColor(Color.WHITE)
        val destRect = android.graphics.Rect(0, 0, width, height)
        if (colorBitmap != null && !colorBitmap.isRecycled) {
            canvas.drawBitmap(colorBitmap, null, destRect, null)
        }
        canvas.drawBitmap(lineArtBitmap, null, destRect, null)
        return thumb
    }

    fun exportPngToGalleryAndShareUri(
        context: Context,
        compositeBitmap: Bitmap,
        fileName: String
    ): Pair<Boolean, Uri?> {
        var savedToGallery = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/ChromaStudio")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { stream ->
                    compositeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                savedToGallery = true
            }
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val exportFile = File(exportDir, fileName)
        FileOutputStream(exportFile).use { stream ->
            compositeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            exportFile
        )
        return savedToGallery to shareUri
    }
}
