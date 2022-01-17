package idv.bruce.ui.osd.items

import android.graphics.*
import android.text.TextPaint
import android.util.Size
import android.util.SizeF
import androidx.annotation.ColorInt
import idv.bruce.ui.osd.OSDContainer

class OSDBarrageItem(
    var text: String,
    var direction: Direction = Direction.RIGHT_TO_LEFT,
    var scale: Float = 1f,
    @ColorInt var color: Int = Color.WHITE,
    var duration: Long = 20000L,
    var displayRange: Pair<Float, Float> = Pair(0f, 1f)
) : OSDContainer(PointF(0f, 0f), SizeF(0f, 0f)) {
    companion object {
        private const val DEFAULT_SIZE: Int = 48
    }

    enum class Direction {
        LEFT_TO_RIGHT, RIGHT_TO_LEFT
    }

    private var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    private var dx: Float = 0f

    private var point: PointF = PointF()

    private var mLimit: Pair<Float, Float>? = null

    private var lastTime: Long = -1L

    override fun onUpdate(canvas: Canvas, time: Long): Boolean {


        if (startTime == -1L) {
            startTime = time
            lastTime = time
        }

        point.offset(dx * (time - lastTime).toFloat() / 1000f, 0f)

        canvas.save()

        canvas.drawText(text, point.x, point.y, textPaint)

        canvas.restore()

        if (point.x < mLimit!!.first || point.x > mLimit!!.second) return true

        lastTime = time

        return false
    }

    override fun release() {

    }

    override fun onAttachToContainer(containerSize: Size) {
        textPaint.textSize = DEFAULT_SIZE * scale
        textPaint.color = color


        val width: Int = textPaint.measureText(text).toInt()

        val fontMatrix: Paint.FontMetrics = textPaint.fontMetrics

        val mRect = Rect(
            0, 0, width, (fontMatrix.descent - fontMatrix.ascent).toInt()
        )

        val startHighRange: Pair<Int, Int> = Pair(
            first = (containerSize.height * displayRange.first).toInt(),
            second = (containerSize.height * displayRange.second - mRect.height()).toInt()
        )

        mLimit = Pair((0 - mRect.width()).toFloat(), containerSize.width.toFloat())

        val h: Int =
            (Math.random() * ((startHighRange.second - startHighRange.first).toFloat()) + startHighRange.first.toFloat()).toInt()

        dx = when (direction) {
            Direction.LEFT_TO_RIGHT -> {
                point.offset(0f, h.toFloat())
                ((containerSize.width + mRect.width()).toFloat() / duration.toFloat() * 1000f)
            }
            Direction.RIGHT_TO_LEFT -> {
                point.offset(containerSize.width.toFloat(), h.toFloat())
                -((containerSize.width + mRect.width()).toFloat() / duration.toFloat() * 1000f)
            }
        }
    }
}