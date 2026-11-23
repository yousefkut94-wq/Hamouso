package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.engine.DrawingEngine
import com.example.engine.SampleColoringGenerator
import com.example.model.BrushTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ExampleRobolectricTest {

    @Test
    fun `app name matches ChromaStudio`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ChromaStudio", appName)
    }

    @Test
    fun `sample generator creates all 3 high-res transparent line art pages`() {
        for (page in 1..3) {
            val bmp = SampleColoringGenerator.generatePage(page)
            assertEquals(SampleColoringGenerator.PAGE_WIDTH, bmp.width)
            assertEquals(SampleColoringGenerator.PAGE_HEIGHT, bmp.height)
            // White background area should be transparent (alpha 0)
            assertEquals(0, Color.alpha(bmp.getPixel(10, 10)))
            // Border outline at (42, 42) should be opaque dark ink
            assertTrue(Color.alpha(bmp.getPixel(42, 42)) > 200)
            bmp.recycle()
        }
    }

    @Test
    fun `scanline flood fill respects line art boundaries`() {
        val lineArt = SampleColoringGenerator.generatePage(1)
        val colorLayer = Bitmap.createBitmap(lineArt.width, lineArt.height, Bitmap.Config.ARGB_8888)
        val fillColor = Color.parseColor("#FF595E")

        // Center of Mandala flower (500, 650) is inside the center star
        val result = DrawingEngine.scanlineFloodFill(colorLayer, lineArt, 500, 650, fillColor)
        assertEquals(DrawingEngine.FloodFillResult.Success, result)

        val sampledCenter = DrawingEngine.sampleColorAt(colorLayer, 500, 650)
        assertEquals(fillColor, sampledCenter)

        // Outside the mandala (100, 300) should remain untouched (white / transparent)
        val outsideSample = DrawingEngine.sampleColorAt(colorLayer, 100, 300)
        assertEquals(Color.WHITE, outsideSample)

        lineArt.recycle()
        colorLayer.recycle()
    }

    @Test
    fun `brush stroke and eraser modify color layer without touching line art`() {
        val lineArt = SampleColoringGenerator.generatePage(2)
        val colorLayer = Bitmap.createBitmap(lineArt.width, lineArt.height, Bitmap.Config.ARGB_8888)
        val inkColor = Color.parseColor("#1982C4")

        DrawingEngine.drawBrushStroke(
            targetBitmap = colorLayer,
            tool = BrushTool.GEL_PEN,
            argbColor = inkColor,
            brushSizePx = 34f,
            x1 = 200f,
            y1 = 200f,
            x2 = 260f,
            y2 = 200f,
            pressure = 0.8f
        )

        val drawnSample = DrawingEngine.sampleColorAt(colorLayer, 230, 200)
        assertNotEquals(Color.WHITE, drawnSample)

        DrawingEngine.drawBrushStroke(
            targetBitmap = colorLayer,
            tool = BrushTool.ERASER,
            argbColor = inkColor,
            brushSizePx = 56f,
            x1 = 200f,
            y1 = 200f,
            x2 = 260f,
            y2 = 200f,
            pressure = 0.8f
        )

        assertEquals(Color.WHITE, DrawingEngine.sampleColorAt(colorLayer, 230, 200))

        val composite = DrawingEngine.buildCompositePage(colorLayer, lineArt)
        assertEquals(lineArt.width, composite.width)
        assertEquals(lineArt.height, composite.height)

        composite.recycle()
        lineArt.recycle()
        colorLayer.recycle()
    }
}
