package idv.bruce.osd.item.picture

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.SizeF
import idv.bruce.ui.osd.OSDItem

class OSDPictureItem(private val picture : Picture, positionPercent : PointF, sizePercent : SizeF) :
    OSDItem<Canvas>(positionPercent, sizePercent) {

    private val paint : Paint = Paint()

    override fun drawFrame(
        drawer : Canvas,
        frameTimeNanos : Long,
        timeIntervalNanos : Long
    ) : Boolean {
        return if (super.drawFrame(drawer, frameTimeNanos, timeIntervalNanos))
            true
        else {
            if (picture.state == Picture.STATE_READY)
                drawer.drawBitmap(picture.bitmap!!, null, mRect, paint)
            false
        }
    }

    override fun release() {

    }


}