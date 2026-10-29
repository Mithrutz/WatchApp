package com.example.watchapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.google.android.material.color.MaterialColors
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

class AnalogClockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {

    // Valorile corespund cu enum-ul "face" din attrs.xml
    private enum class Face { PLAIN, ROMANIA, INDIA, GLOBE }

    private val zone: ZoneId
    private val face: Face

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.AnalogClockView)
        zone = ZoneId.of(a.getString(R.styleable.AnalogClockView_timeZone) ?: "UTC")
        face = Face.entries[a.getInt(R.styleable.AnalogClockView_face, 0)]
        a.recycle()
    }

    // Culoarea pentru cifre, liniute si ace, aleasa sa se vada bine pe fiecare fundal
    private val ink = when (face) {
        Face.PLAIN -> MaterialColors.getColor(
            this, com.google.android.material.R.attr.colorOnSurface, Color.BLACK
        )
        Face.ROMANIA -> Color.BLACK
        Face.INDIA -> NAVY
        Face.GLOBE -> Color.WHITE
    }

    private val facePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        style = Paint.Style.STROKE
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        strokeCap = Paint.Cap.ROUND
    }
    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        textAlign = Paint.Align.CENTER
        isFakeBoldText = face != Face.PLAIN
    }
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ink
        strokeCap = Paint.Cap.ROUND
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        strokeCap = Paint.Cap.ROUND
    }
    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val clipPath = Path()

    // Redeseneaza ceasul in fiecare secunda
    private val ticker = object : Runnable {
        override fun run() {
            invalidate()
            postDelayed(this, 1000 - System.currentTimeMillis() % 1000)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        post(ticker)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(ticker)
        super.onDetachedFromWindow()
    }

    // Ceasul e mereu patrat: latura = cea mai mica dintre latimea si inaltimea disponibile
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val w = MeasureSpec.getSize(widthMeasureSpec)
        val h = MeasureSpec.getSize(heightMeasureSpec)
        val size = if (h == 0) w else min(w, h)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        val radius = min(width, height) / 2f * 0.95f

        // Fundalul (steagul) se taie dupa forma cercului cu clipPath
        if (face != Face.PLAIN) {
            clipPath.reset()
            clipPath.addCircle(cx, cy, radius, Path.Direction.CW)
            canvas.save()
            canvas.clipPath(clipPath)
            when (face) {
                Face.ROMANIA -> drawRomania(canvas, cx, cy, radius)
                Face.INDIA -> drawIndia(canvas, cx, cy, radius)
                Face.GLOBE -> drawGlobe(canvas, cx, cy, radius)
                Face.PLAIN -> Unit
            }
            canvas.restore()
        }

        // Cadranul
        facePaint.strokeWidth = radius * 0.04f
        canvas.drawCircle(cx, cy, radius, facePaint)

        // Liniutele: 60 pentru minute, cele de la fiecare ora mai groase si mai lungi
        for (i in 0 until 60) {
            val isHour = i % 5 == 0
            tickPaint.strokeWidth = radius * if (isHour) 0.03f else 0.012f
            val angle = Math.toRadians(i * 6.0)
            val outer = radius * 0.92f
            val inner = radius * if (isHour) 0.80f else 0.87f
            canvas.drawLine(
                cx + (sin(angle) * inner).toFloat(), cy - (cos(angle) * inner).toFloat(),
                cx + (sin(angle) * outer).toFloat(), cy - (cos(angle) * outer).toFloat(),
                tickPaint
            )
        }

        // Cifrele 1..12
        numberPaint.textSize = radius * 0.16f
        val textOffset = (numberPaint.descent() + numberPaint.ascent()) / 2
        for (h in 1..12) {
            val angle = Math.toRadians(h * 30.0)
            val r = radius * 0.66f
            canvas.drawText(
                h.toString(),
                cx + (sin(angle) * r).toFloat(),
                cy - (cos(angle) * r).toFloat() - textOffset,
                numberPaint
            )
        }

        // Ora curenta in fusul orar ales
        val now = ZonedDateTime.now(zone)
        val seconds = now.second
        val minutes = now.minute + seconds / 60f
        val hours = now.hour % 12 + minutes / 60f

        // Unghiul in grade: o tura completa = 360
        drawHand(canvas, cx, cy, hours * 30f, radius * 0.5f, radius * 0.06f, handPaint)
        drawHand(canvas, cx, cy, minutes * 6f, radius * 0.75f, radius * 0.04f, handPaint)
        drawHand(canvas, cx, cy, seconds * 6f, radius * 0.85f, radius * 0.015f, secondPaint)

        canvas.drawCircle(cx, cy, radius * 0.04f, secondPaint)
    }

    // Tricolor vertical: albastru, galben, rosu
    private fun drawRomania(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val colors = intArrayOf(RO_BLUE, RO_YELLOW, RO_RED)
        val stripe = 2 * r / 3
        for (i in colors.indices) {
            fillPaint.color = colors[i]
            val left = cx - r + i * stripe
            canvas.drawRect(left, cy - r, left + stripe, cy + r, fillPaint)
        }
        drawWash(canvas, cx, cy, r)
    }

    // Tricolor orizontal: sofran, alb, verde + roata Ashoka in mijloc
    private fun drawIndia(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        val colors = intArrayOf(IN_SAFFRON, Color.WHITE, IN_GREEN)
        val stripe = 2 * r / 3
        for (i in colors.indices) {
            fillPaint.color = colors[i]
            val top = cy - r + i * stripe
            canvas.drawRect(cx - r, top, cx + r, top + stripe, fillPaint)
        }
        drawWash(canvas, cx, cy, r)

        // Roata are 24 de spite
        val wheel = r * 0.28f
        fillPaint.color = NAVY
        fillPaint.style = Paint.Style.STROKE
        fillPaint.strokeWidth = r * 0.02f
        canvas.drawCircle(cx, cy, wheel, fillPaint)
        fillPaint.strokeWidth = r * 0.008f
        for (i in 0 until 24) {
            val angle = Math.toRadians(i * 15.0)
            canvas.drawLine(
                cx, cy,
                cx + (sin(angle) * wheel).toFloat(),
                cy - (cos(angle) * wheel).toFloat(),
                fillPaint
            )
        }
        fillPaint.style = Paint.Style.FILL
    }

    // Glob: ocean albastru cu meridiane (elipse) si paralele (linii orizontale)
    private fun drawGlobe(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        fillPaint.color = GLOBE_BLUE
        canvas.drawCircle(cx, cy, r, fillPaint)

        fillPaint.color = Color.argb(70, 255, 255, 255)
        fillPaint.style = Paint.Style.STROKE
        fillPaint.strokeWidth = r * 0.015f
        for (k in 1..2) {
            val half = r * k / 3f
            canvas.drawOval(cx - half, cy - r, cx + half, cy + r, fillPaint)
        }
        canvas.drawLine(cx, cy - r, cx, cy + r, fillPaint)
        for (k in -2..2) {
            val y = r * k / 3f
            val halfWidth = sqrt(r * r - y * y)
            canvas.drawLine(cx - halfWidth, cy + y, cx + halfWidth, cy + y, fillPaint)
        }
        fillPaint.style = Paint.Style.FILL
    }

    // Un strat alb semi-transparent peste steag, ca cifrele si acele sa ramana lizibile
    private fun drawWash(canvas: Canvas, cx: Float, cy: Float, r: Float) {
        fillPaint.color = Color.argb(90, 255, 255, 255)
        canvas.drawCircle(cx, cy, r, fillPaint)
    }

    private fun drawHand(
        canvas: Canvas, cx: Float, cy: Float,
        degrees: Float, length: Float, thickness: Float, paint: Paint,
    ) {
        val angle = Math.toRadians(degrees.toDouble())
        paint.strokeWidth = thickness
        canvas.drawLine(
            cx, cy,
            cx + (sin(angle) * length).toFloat(),
            cy - (cos(angle) * length).toFloat(),
            paint
        )
    }

    private companion object {
        val RO_BLUE = Color.rgb(0x00, 0x2B, 0x7F)
        val RO_YELLOW = Color.rgb(0xFC, 0xD1, 0x16)
        val RO_RED = Color.rgb(0xCE, 0x11, 0x26)
        val IN_SAFFRON = Color.rgb(0xFF, 0x99, 0x33)
        val IN_GREEN = Color.rgb(0x13, 0x88, 0x08)
        val NAVY = Color.rgb(0x00, 0x00, 0x80)
        val GLOBE_BLUE = Color.rgb(0x15, 0x65, 0xC0)
    }
}
