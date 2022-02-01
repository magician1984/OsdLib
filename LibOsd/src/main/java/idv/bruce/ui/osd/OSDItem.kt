package idv.bruce.ui.osd

import android.graphics.PointF
import android.graphics.Rect
import android.util.SizeF

abstract class OSDItem<T>(var positionPercent : PointF, var sizePercent : SizeF?) {

    private val uid : Long = System.currentTimeMillis()

    var isEnded : Boolean = false

    protected val mRect : Rect = Rect()

    open fun onWindowSizeChanged(width : Int, height : Int) {
        if (sizePercent != null) {
            mRect.left = (width.toFloat() * positionPercent.x).toInt()
            mRect.top = (height.toFloat() * positionPercent.y).toInt()
            mRect.right = mRect.left + (width.toFloat() * sizePercent!!.width).toInt()
            mRect.bottom = mRect.top + (height.toFloat() * sizePercent!!.height).toInt()
        }
    }

    open fun drawFrame(drawer : T, frameTimeNanos : Long, timeIntervalNanos : Long) : Boolean =
        isEnded

    abstract fun release()

    override fun equals(other : Any?) : Boolean {

        if (this === other) return true

        if (other !is OSDItem<*>) return false

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode() : Int {
        return uid.hashCode()
    }

}