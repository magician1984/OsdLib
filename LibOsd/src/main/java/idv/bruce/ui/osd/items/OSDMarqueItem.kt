package idv.bruce.ui.osd.items

import android.graphics.*
import android.text.TextPaint
import android.util.Size
import android.util.SizeF
import androidx.annotation.ColorInt
import idv.bruce.ui.osd.OSDContainer

class OSDMarqueItem(
    var text: String,
    locationPoint: PointF,
    size: SizeF,
    @ColorInt var color: Int = Color.WHITE,
    private var velocity: Float = 0.1f,
) : OSDContainer(locationPoint, size) {


    private var mBitmap: Bitmap? = null

    private var mCanvas: Canvas? = null

    private var mPaint: Paint = Paint()

    private var textPaint: TextPaint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG)

    private var dx: Int = 0

    private var lastTime: Long = 0

    private var windowRect: Rect? = null

    private var drawRect: RectF? = null

    override fun onUpdate(canvas: Canvas, time: Long): Boolean {

        if (windowRect == null) return false

        if (drawRect == null) return false


        if (windowRect!!.right >= mBitmap!!.width) {
            windowRect!!.offsetTo(0, 0)
        }

        if (lastTime != 0L) {
            val x: Float = (dx * (time - lastTime).toFloat() / 1000f)

            windowRect!!.offset(x.toInt(), 0)
        }

        canvas.drawBitmap(mBitmap!!, windowRect, drawRect!!, mPaint)

        lastTime = time

        return false
    }

    override fun release() {
        if (mBitmap?.isRecycled == false)
            mBitmap?.recycle()
    }

    override fun onAttachToContainer(containerSize: Size) {
        drawRect = RectF().apply {
            left = containerSize.width.toFloat() * locationPercent.x
            top = containerSize.height.toFloat() * locationPercent.y
            right = left + size.width * containerSize.width
            bottom = top + size.height * containerSize.height
        }

        textPaint.textSize = drawRect!!.height() * 0.95f
        textPaint.color = color

        val rect: Rect = Rect()

        textPaint.getTextBounds(text, 0, text.length, rect)

        val fontMatrix: Paint.FontMetrics = textPaint.fontMetrics

        val y: Float = rect.height() - fontMatrix.descent

        mBitmap = Bitmap.createBitmap(
            (rect.width() + (2 * drawRect!!.width())).toInt(), rect.height(),
            Bitmap.Config.ARGB_8888
        )

        mCanvas = Canvas(mBitmap!!)

        mCanvas!!.drawText(text, 0, text.length, drawRect!!.width(), y.toFloat(), textPaint)

        windowRect = Rect(
            drawRect!!.left.toInt(),
            drawRect!!.top.toInt(),
            drawRect!!.right.toInt(),
            drawRect!!.bottom.toInt()
        )

        dx = (drawRect!!.width() * velocity).toInt()
    }
}