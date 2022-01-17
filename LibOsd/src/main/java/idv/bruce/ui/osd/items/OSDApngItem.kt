package idv.bruce.ui.osd.items

import android.annotation.SuppressLint
import android.graphics.*
import android.util.Size
import android.util.SizeF
import com.linecorp.apng.ApngDrawable
import idv.bruce.ui.osd.OSDContainer

class OSDApngItem(
    private var mApngDrawable: ApngDrawable,
    private var repeatCount: Int,
    locationPercent: PointF,
    areaPercent: SizeF
) : OSDContainer(locationPercent, areaPercent) {

    private var mStartTime: Long = -1L

    private var mDuration: Long = -1L


    private var mCount: Int = 0

    override fun onUpdate(canvas: Canvas, time: Long): Boolean {
        if (mStartTime == -1L) {
            mCount++
            mStartTime = time
        }

        if (time - mStartTime >= mDuration) {
            if (mCount == repeatCount)
                return true
            else
                mStartTime = -1L
        }

        mApngDrawable.seekTo(time - mStartTime)

        mApngDrawable.draw(canvas)


        return false
    }

    override fun release() {
        mApngDrawable.recycle()
    }

    @SuppressLint("Range")
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

        val drawRect = Rect(
            point.x,
            point.y,
            point.x + drawSize.width,
            point.y + drawSize.height
        )

        mApngDrawable.bounds = drawRect

        mDuration = mApngDrawable.durationMillis.toLong()
    }
}