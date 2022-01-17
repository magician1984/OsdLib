package idv.bruce.ui.osd.items

import android.graphics.*
import android.util.Size
import android.util.SizeF
import android.view.View
import idv.bruce.ui.osd.OSDContainer

class OSDViewItem(
    private var view: View,
    private var duration: Long,
    locationPoint: PointF,
    areaPercent: SizeF
) :
    OSDContainer(locationPoint, areaPercent) {


    private var mDrawRect: Rect? = null

    private var mCanvas: Canvas? = null

    private var mBitmap: Bitmap? = null

    private var mMatrix: Matrix? = null

    private var mPaint: Paint = Paint()

    var hidden: Boolean = false

    override fun onUpdate(canvas: Canvas, time: Long): Boolean {
        if (mCanvas == null) return true

        if (startTime == -1L) {
            startTime = time
        }

        if (time - startTime >= duration && duration != -1L) {
            return true
        }

        if (!hidden)
            canvas.drawBitmap(mBitmap!!, mMatrix!!, mPaint)

        return false
    }

    override fun release() {
        mBitmap?.recycle()
    }

    fun notifyContentChanged() {
        if (mBitmap?.isRecycled == true) {
            view.draw(mCanvas)
        }
    }

    override fun onAttachToContainer(containerSize: Size) {
        val drawSize: Size = Size(
            if (size.width != 0f)
                (containerSize.width * size.width).toInt()
            else
                (containerSize.height * size.height).toInt(),
            if (size.height != 0f)
                (containerSize.height * size.height).toInt()
            else
                (containerSize.width * size.width).toInt()
        )

        val point: Point = Point(
            if (locationPercent.x != -1f)
                (containerSize.width * locationPercent.x).toInt()
            else
                (containerSize.width - drawSize.width) / 2,
            if (locationPercent.y != -1f)
                (containerSize.height * locationPercent.y).toInt()
            else
                (containerSize.height - drawSize.height) / 2
        )

        mDrawRect = Rect(
            point.x,
            point.y,
            point.x + drawSize.width,
            point.y + drawSize.height
        )

        val withSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect!!.width() /* any */,
                View.MeasureSpec.EXACTLY
            )

        val hightSpec =
            View.MeasureSpec.makeMeasureSpec(
                mDrawRect!!.height() /* any */,
                View.MeasureSpec.EXACTLY
            )

        view.measure(withSpec, hightSpec)

        val w: Int = view.measuredWidth

        val h: Int = view.measuredHeight

        view.layout(0, 0, w, h)

        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        mCanvas = Canvas(mBitmap!!)

        view.draw(mCanvas)

        mMatrix = Matrix().apply {
            postTranslate(mDrawRect!!.left.toFloat(), mDrawRect!!.top.toFloat())
        }

    }
}