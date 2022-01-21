package idv.bruce.ui.osd.item

import android.graphics.Canvas
import android.graphics.PointF
import android.graphics.Rect
import android.util.SizeF
import androidx.annotation.IntRange
import idv.bruce.ui.osd.OSDItem
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap

class OSDTextBarrageItem(location: PointF, size: SizeF, private val defaultSizePixel: Int) :
    OSDItem<Canvas>(location, size) {

    companion object {
        const val BARRAGE_ON_READY = 1
        const val BARRAGE_ON_DEPARTING = 2
        const val BARRAGE_ON_FLYING = 3
        const val BARRAGE_ON_ENTERING = 4
        const val BARRAGE_ON_DONE = 5
    }

    private lateinit var mLimit: Pair<Int, Int>

    private lateinit var mController: Controller

    private var mIsReady: Boolean = false


    override fun onWindowSizeChanged(width: Int, height: Int) {
        mLimit = Pair(0, width)

        mController = Controller(width, height, defaultSizePixel)


    }

    override fun drawFrame(drawer: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        if (mIsReady) {

        }
        return isEnded
    }

    override fun release() {

    }

    fun addText(
        txt: String,
        textColor: Int,
        outlineColor: Int,
        velocity: Float,
        @IntRange(from = 1, to = 3) scale: Int
    ) {
        if (mIsReady)
            mController.addBarrage(Barrage(txt, textColor, outlineColor, velocity, scale))
    }


    private inner class Barrage(
        val txt: String,
        val textColor: Int,
        val outlineColor: Int,
        val velocity: Float,
        val scale: Int
    ) {

        var state: Int = BARRAGE_ON_READY

        private var mTracksIndex: Pair<Int, Int>? = null

        private var mRect: Rect? = null

        var uid: Long = System.nanoTime()

        fun draw(canvas: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long) {
            if (state != BARRAGE_ON_FLYING) return


        }
    }

    private inner class Controller(width: Int, height: Int, baseTextSize: Int) {
        private val mBarrageQueue: LinkedList<Barrage> = LinkedList()

        private var mTrackMap: Array<Boolean>

        private var mTrackOffset: Int = 0

        val mLaunchBarrage: HashMap<Long, Barrage> = HashMap()

        init {
            val trackCount: Int = height / baseTextSize

            mTrackMap = Array(trackCount) { true }

            mTrackOffset = (height % baseTextSize) / trackCount

//            Collections.indexOfSubList()
        }

        fun addBarrage(barrage: Barrage) {

        }

        fun releaseTrack(from: Int, to: Int) {
            mTrackMap.fill(true, from, to)
        }

        fun lockTrack(from: Int, to: Int) {
            mTrackMap.fill(false, from, to)
        }

        fun updateTrackState() {

        }
    }
}