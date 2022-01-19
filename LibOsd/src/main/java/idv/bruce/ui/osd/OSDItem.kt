package idv.bruce.ui.osd

import android.graphics.PointF
import android.util.SizeF

abstract class OSDItem<T>(var locationPercent : PointF, var size : SizeF) {

    private val uid : Long = System.currentTimeMillis()

    abstract fun onWindowSizeChanged(width : Int, height : Int)

    abstract fun drawFrame(drawer : T, frameTimeNanos : Long, timeIntervalNanos : Long) : Boolean

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