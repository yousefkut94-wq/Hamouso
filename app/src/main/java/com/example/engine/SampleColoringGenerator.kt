package com.example.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object SampleColoringGenerator {

    const val PAGE_WIDTH = 1000
    const val PAGE_HEIGHT = 1300

    fun generatePage(pageNum: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(PAGE_WIDTH, PAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val clearFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
            style = Paint.Style.FILL
        }

        val solidBlackFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111")
            style = Paint.Style.FILL
        }

        val w = PAGE_WIDTH.toFloat()
        val h = PAGE_HEIGHT.toFloat()

        // Decorative page border
        strokePaint.strokeWidth = 8f
        canvas.drawRect(42f, 42f, w - 42f, h - 42f, strokePaint)

        when (pageNum) {
            1 -> drawMandalaPage(canvas, w, h, strokePaint, clearFillPaint)
            2 -> drawOwlPage(canvas, w, h, strokePaint, clearFillPaint, solidBlackFillPaint)
            else -> drawRocketPage(canvas, w, h, strokePaint, clearFillPaint)
        }

        return bitmap
    }

    private fun drawMandalaPage(
        canvas: Canvas,
        w: Float,
        h: Float,
        strokePaint: Paint,
        clearFillPaint: Paint
    ) {
        val cx = w / 2f
        val cy = h / 2f

        // Corner floral accents
        val corners = listOf(
            120f to 120f,
            (w - 120f) to 120f,
            120f to (h - 120f),
            (w - 120f) to (h - 120f)
        )
        strokePaint.strokeWidth = 5f
        for ((kx, ky) in corners) {
            for (i in 0 until 6) {
                val a = (i / 6.0) * PI * 2.0
                canvas.drawCircle(
                    (kx + cos(a) * 26.0).toFloat(),
                    (ky + sin(a) * 26.0).toFloat(),
                    18f,
                    strokePaint
                )
            }
            canvas.drawCircle(kx, ky, 16f, clearFillPaint)
            canvas.drawCircle(kx, ky, 16f, strokePaint)
        }

        // Outer Scallop Ring (24 petals)
        strokePaint.strokeWidth = 5f
        for (i in 0 until 24) {
            val a = (i / 24.0) * PI * 2.0
            val nextA = ((i + 1) / 24.0) * PI * 2.0
            val midA = (a + nextA) / 2.0
            val path = Path().apply {
                moveTo((cx + cos(a) * 330.0).toFloat(), (cy + sin(a) * 330.0).toFloat())
                quadTo(
                    (cx + cos(midA) * 435.0).toFloat(),
                    (cy + sin(midA) * 435.0).toFloat(),
                    (cx + cos(nextA) * 330.0).toFloat(),
                    (cy + sin(nextA) * 330.0).toFloat()
                )
            }
            canvas.drawPath(path, strokePaint)
        }

        // Concentric Mandala Rings
        val rings = listOf(330f, 295f, 225f, 150f, 75f)
        rings.forEachIndexed { idx, r ->
            strokePaint.strokeWidth = if (idx % 2 == 0) 6f else 4f
            canvas.drawCircle(cx, cy, r, strokePaint)
        }

        // Mid-layer 16 Lotus Petals
        strokePaint.strokeWidth = 5f
        for (i in 0 until 16) {
            val deg = (i / 16f) * 360f
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(deg)
            val path = Path().apply {
                moveTo(0f, -150f)
                quadTo(42f, -225f, 0f, -295f)
                quadTo(-42f, -225f, 0f, -150f)
            }
            canvas.drawPath(path, strokePaint)
            canvas.restore()
        }

        // Inner 8 Heart Petals
        for (i in 0 until 8) {
            val deg = (i / 8f) * 360f
            canvas.save()
            canvas.translate(cx, cy)
            canvas.rotate(deg)
            val path = Path().apply {
                moveTo(0f, -75f)
                cubicTo(45f, -105f, 35f, -145f, 0f, -150f)
                cubicTo(-35f, -145f, -45f, -105f, 0f, -75f)
            }
            canvas.drawPath(path, strokePaint)
            canvas.restore()
        }

        // Center Star
        val starPath = Path()
        for (i in 0 until 16) {
            val r = if (i % 2 == 0) 55.0 else 25.0
            val a = (i / 16.0) * PI * 2.0
            val sx = (cx + cos(a) * r).toFloat()
            val sy = (cy + sin(a) * r).toFloat()
            if (i == 0) starPath.moveTo(sx, sy) else starPath.lineTo(sx, sy)
        }
        starPath.close()
        canvas.drawPath(starPath, strokePaint)
    }

    private fun drawOwlPage(
        canvas: Canvas,
        w: Float,
        h: Float,
        strokePaint: Paint,
        clearFillPaint: Paint,
        solidBlackFillPaint: Paint
    ) {
        val cx = w / 2f

        // Tree Branch
        strokePaint.strokeWidth = 7f
        val branchPath = Path().apply {
            moveTo(42f, 960f)
            quadTo(480f, 910f, w - 42f, 975f)
            moveTo(42f, 1015f)
            quadTo(480f, 965f, w - 42f, 1030f)
        }
        canvas.drawPath(branchPath, strokePaint)

        // Branch Leaves
        fun drawLeaf(lx: Float, ly: Float, rotDeg: Float) {
            canvas.save()
            canvas.translate(lx, ly)
            canvas.rotate(rotDeg)
            strokePaint.strokeWidth = 5f
            val leaf = Path().apply {
                moveTo(0f, 0f)
                quadTo(45f, -35f, 95f, 0f)
                quadTo(45f, 35f, 0f, 0f)
            }
            canvas.drawPath(leaf, clearFillPaint)
            canvas.drawPath(leaf, strokePaint)
            canvas.drawLine(0f, 0f, 80f, 0f, strokePaint)
            canvas.restore()
        }
        drawLeaf(150f, 950f, -34f)
        drawLeaf(210f, 1005f, 40f)
        drawLeaf(780f, 960f, -29f)
        drawLeaf(830f, 1020f, 34f)

        // Owl Wings
        strokePaint.strokeWidth = 7f
        for (dir in listOf(-1f, 1f)) {
            canvas.save()
            canvas.translate(cx + dir * 215f, 670f)
            canvas.rotate((dir * 0.22f * 180f / PI).toFloat())
            val rect = RectF(-75f, -195f, 75f, 195f)
            canvas.drawOval(rect, clearFillPaint)
            canvas.drawOval(rect, strokePaint)
            canvas.restore()
        }

        // Owl Main Body & Ear Tufts
        val bodyPath = Path().apply {
            moveTo(cx - 180f, 290f)
            lineTo(cx - 105f, 355f)
            quadTo(cx, 330f, cx + 105f, 355f)
            lineTo(cx + 180f, 290f)
            quadTo(cx + 235f, 440f, cx + 215f, 680f)
            quadTo(cx + 195f, 925f, cx, 935f)
            quadTo(cx - 195f, 925f, cx - 215f, 680f)
            quadTo(cx - 235f, 440f, cx - 180f, 290f)
            close()
        }
        canvas.drawPath(bodyPath, clearFillPaint)
        canvas.drawPath(bodyPath, strokePaint)

        // Belly Scallops
        strokePaint.strokeWidth = 5f
        canvas.drawOval(RectF(cx - 145f, 715f - 175f, cx + 145f, 715f + 175f), strokePaint)
        for (row in 0 until 3) {
            val y = 635f + row * 65f
            val cols = if (row == 1) 3 else 2
            for (c in 0 until cols) {
                val x = cx + (c - (cols - 1) / 2f) * 75f
                val arcRect = RectF(x - 28f, y - 28f, x + 28f, y + 28f)
                canvas.drawArc(arcRect, 18f, 144f, false, strokePaint)
            }
        }

        // Big Owl Eyes
        for (dx in listOf(-92f, 92f)) {
            strokePaint.strokeWidth = 6f
            canvas.drawCircle(cx + dx, 465f, 76f, clearFillPaint)
            canvas.drawCircle(cx + dx, 465f, 76f, strokePaint)
            canvas.drawCircle(cx + dx, 465f, 48f, strokePaint)
            canvas.drawCircle(cx + dx, 465f, 25f, solidBlackFillPaint)
            canvas.drawCircle(cx + dx - 9f, 456f, 8f, clearFillPaint)
        }

        // Cute Beak
        strokePaint.strokeWidth = 6f
        val beak = Path().apply {
            moveTo(cx - 28f, 520f)
            lineTo(cx + 28f, 520f)
            lineTo(cx, 575f)
            close()
        }
        canvas.drawPath(beak, clearFillPaint)
        canvas.drawPath(beak, strokePaint)

        // Owl Feet gripping branch
        strokePaint.strokeWidth = 5f
        for (fx in listOf(-65f, 65f)) {
            for (toe in listOf(-16f, 0f, 16f)) {
                val toeRect = RectF(cx + fx + toe - 10f, 938f - 22f, cx + fx + toe + 10f, 938f + 22f)
                canvas.drawOval(toeRect, clearFillPaint)
                canvas.drawOval(toeRect, strokePaint)
            }
        }

        // Crescent Moon in Sky
        strokePaint.strokeWidth = 5f
        val moonRect = RectF(185f - 52f, 185f - 52f, 185f + 52f, 185f + 52f)
        val moonPath = Path().apply {
            arcTo(moonRect, 45f, 252f, true)
            quadTo(
                160f,
                185f,
                (185.0 + cos(0.25 * PI) * 52.0).toFloat(),
                (185.0 + sin(0.25 * PI) * 52.0).toFloat()
            )
        }
        canvas.drawPath(moonPath, strokePaint)
    }

    private fun drawRocketPage(
        canvas: Canvas,
        w: Float,
        h: Float,
        strokePaint: Paint,
        clearFillPaint: Paint
    ) {
        val cx = w / 2f

        // Stars
        val stars = listOf(
            Triple(180f, 200f, 30f),
            Triple(820f, 185f, 34f),
            Triple(155f, 560f, 24f),
            Triple(845f, 520f, 28f),
            Triple(210f, 960f, 30f),
            Triple(805f, 925f, 25f)
        )
        strokePaint.strokeWidth = 5f
        for ((sx, sy, sr) in stars) {
            val star = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) sr else sr * 0.42f
                val a = (i * PI) / 5.0 - PI / 2.0
                val px = (sx + cos(a) * r).toFloat()
                val py = (sy + sin(a) * r).toFloat()
                if (i == 0) star.moveTo(px, py) else star.lineTo(px, py)
            }
            star.close()
            canvas.drawPath(star, strokePaint)
        }

        // Ringed Planet
        strokePaint.strokeWidth = 5f
        canvas.drawCircle(210f, 355f, 56f, strokePaint)
        canvas.save()
        canvas.translate(210f, 355f)
        canvas.rotate(-17.2f)
        canvas.drawOval(RectF(-104f, -24f, 104f, 24f), strokePaint)
        canvas.restore()

        // Rocket Fins
        strokePaint.strokeWidth = 7f
        for (dir in listOf(-1f, 1f)) {
            val fin = Path().apply {
                moveTo(cx + dir * 115f, 665f)
                quadTo(cx + dir * 245f, 735f, cx + dir * 225f, 895f)
                lineTo(cx + dir * 110f, 845f)
                close()
            }
            canvas.drawPath(fin, clearFillPaint)
            canvas.drawPath(fin, strokePaint)
        }

        // Rocket Body
        val body = Path().apply {
            moveTo(cx, 195f)
            cubicTo(cx + 165f, 370f, cx + 165f, 680f, cx + 110f, 865f)
            lineTo(cx - 110f, 865f)
            cubicTo(cx - 165f, 680f, cx - 165f, 370f, cx, 195f)
            close()
        }
        canvas.drawPath(body, clearFillPaint)
        canvas.drawPath(body, strokePaint)

        // Nose Cone Stripe
        strokePaint.strokeWidth = 6f
        val nose = Path().apply {
            moveTo(cx - 88f, 355f)
            quadTo(cx, 385f, cx + 88f, 355f)
        }
        canvas.drawPath(nose, strokePaint)

        // Porthole Window
        strokePaint.strokeWidth = 7f
        canvas.drawCircle(cx, 535f, 76f, strokePaint)
        strokePaint.strokeWidth = 5f
        canvas.drawCircle(cx, 535f, 54f, strokePaint)

        // Exhaust Flames
        strokePaint.strokeWidth = 6f
        val outerFlame = Path().apply {
            moveTo(cx - 75f, 865f)
            quadTo(cx - 95f, 995f, cx - 35f, 1045f)
            quadTo(cx, 1135f, cx + 35f, 1045f)
            quadTo(cx + 95f, 995f, cx + 75f, 865f)
            close()
        }
        canvas.drawPath(outerFlame, strokePaint)

        val innerFlame = Path().apply {
            moveTo(cx - 38f, 865f)
            quadTo(cx, 1020f, cx + 38f, 865f)
        }
        canvas.drawPath(innerFlame, strokePaint)
    }
}
