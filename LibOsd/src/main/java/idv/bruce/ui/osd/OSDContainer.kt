package idv.bruce.ui.osd

import android.graphics.Canvas
import android.graphics.PointF
import android.util.Size
import android.util.SizeF

abstract class OSDContainer(var locationPercent: PointF, var size: SizeF) {
    var viewSize: Size? = null
        set(value) {
            field = value
            onAttachToContainer(field ?: return)
        }

    open var startTime: Long = -1L

    private val uid: Long = System.currentTimeMillis()


    abstract fun onAttachToContainer(containerSize: Size)
    abstract fun onUpdate(canvas: Canvas, time: Long): Boolean

    abstract fun release()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OSDContainer) return false

        if (uid != other.uid) return false

        return true
    }

    override fun hashCode(): Int {
        return uid.hashCode()
    }

}