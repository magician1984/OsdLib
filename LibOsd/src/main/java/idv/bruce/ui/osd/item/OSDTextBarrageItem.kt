package idv.bruce.ui.osd.item

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.text.TextPaint
import android.util.Log
import android.util.SizeF
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import idv.bruce.ui.osd.OSDItem
import java.util.*
import kotlin.collections.HashMap

class OSDTextBarrageItem(location : PointF, size : SizeF, private val defaultSizePixel : Int) :
    OSDItem<Canvas>(location, size) {

    companion object {
        const val BARRAGE_STATE_ERROR = -1
        const val BARRAGE_STATE_NOT_CHANGE = 0
        const val BARRAGE_STATE_ON_READY = 1
        const val BARRAGE_STATE_ON_DEPARTING = 2
        const val BARRAGE_STATE_ON_FLYING = 3
        const val BARRAGE_STATE_ON_ENTERING = 4
        const val BARRAGE_STATE_ON_DONE = 5
    }

    private lateinit var mLimit : Pair<Int, Int>

    private lateinit var mController : Controller

    private var mIsReady : Boolean = false

    private var mTextPaint : TextPaint =
        TextPaint(TextPaint.ANTI_ALIAS_FLAG or TextPaint.DITHER_FLAG)

    init {
        mTextPaint.textSize = defaultSizePixel.toFloat()
    }

    override fun onWindowSizeChanged(width : Int, height : Int) {

        mLimit = Pair(0, width)

        mController = Controller(width, height, defaultSizePixel)

        mIsReady = true
    }

    override fun drawFrame(
        drawer : Canvas,
        frameTimeNanos : Long,
        timeIntervalNanos : Long
    ) : Boolean {
        if (mIsReady) {

            val iterator = mController.mLaunchBarrage.values.iterator()

            var barrage : Barrage
            while (iterator.hasNext()) {
                barrage = iterator.next()
                when (barrage.draw(drawer, timeIntervalNanos)) {
                    BARRAGE_STATE_ON_FLYING -> mController.unLockTrack(
                        barrage.trackIndex,
                        barrage.scale
                    )
                    BARRAGE_STATE_ON_DONE -> iterator.remove()
                }
            }
        }
        return isEnded
    }

    override fun release() {
        mController.release()
    }

    fun addText(
        txt : String,
        @ColorInt textColor : Int,
        @ColorInt outlineColor : Int,
        velocity : Float,
        @IntRange(from = 1, to = 3) scale : Int
    ) {
        if (mIsReady)
            mController.addBarrage(Barrage(txt, textColor, outlineColor, velocity, scale))
    }

    private inner class Barrage(
        val txt : String,
        val textColor : Int,
        val outlineColor : Int,
        val velocity : Float,
        val scale : Int
    ) {

        var state : Int = BARRAGE_STATE_ON_READY

        var trackIndex : Int = -1

        private var position : PointF = PointF()

        private val mRect : Rect = Rect()

        private val mItemTextPaint : TextPaint = TextPaint(mTextPaint)

        var uid : Long = System.nanoTime()

        init {
            mItemTextPaint.textSize *= scale.toFloat()

            mItemTextPaint.getTextBounds(txt, 0, txt.length, mRect)
        }

        fun setInitPosition(x : Float, y : Float) {
            position.set(x, y)
        }


        fun draw(canvas : Canvas, timeIntervalNanos : Long) : Int {
                if (state < BARRAGE_STATE_ON_DEPARTING) return BARRAGE_STATE_ERROR

                position.offset(-50f * (timeIntervalNanos.toFloat() / 1000000000f), 0f)
//
                canvas.save()

                canvas.translate(position.x, position.y)

                mItemTextPaint.style = Paint.Style.STROKE
                mItemTextPaint.color = outlineColor
                canvas.drawText(txt, 0f, 0f, mItemTextPaint)

                mItemTextPaint.style = Paint.Style.FILL
                mItemTextPaint.color = textColor
            mItemTextPaint.bgColor
                canvas.drawText(txt, 0f, 0f, mItemTextPaint)

                canvas.restore()

                state = when {
                    state == BARRAGE_STATE_ON_ENTERING && position.x + mRect.width() < mLimit.first -> {
                        BARRAGE_STATE_ON_DONE
                    }
                    state == BARRAGE_STATE_ON_FLYING && position.x <= mLimit.first -> {
                        BARRAGE_STATE_ON_ENTERING
                    }
                    state == BARRAGE_STATE_ON_DEPARTING && (position.x + mRect.width()) < mLimit.second -> {
                        BARRAGE_STATE_ON_FLYING
                    }
                    else -> {
                        return BARRAGE_STATE_NOT_CHANGE
                    }
                }

                return state
        }

        override fun toString() : String {
            return "Barrage(txt='$txt', textColor=$textColor, outlineColor=$outlineColor, velocity=$velocity, scale=$scale, state=$state, trackIndex=$trackIndex, position=$position, mRect=$mRect, mItemTextPaint=$mItemTextPaint, uid=$uid)"
        }


    }


    private inner class Controller(private var width : Int, height : Int, baseTextSize : Int) {
        private val mWaitingQueue : LinkedList<Barrage> = LinkedList()

        private var mTrackMap : Array<Boolean>

        private var mTrackOffset : Int = 0

        val mLaunchBarrage : HashMap<Long, Barrage> = HashMap()

        init {
            val trackCount : Int = height / baseTextSize

            mTrackMap = Array(trackCount) { true }

            mTrackOffset = (height % baseTextSize) / trackCount

        }

        fun addBarrage(barrage : Barrage) {

            val index = findIdleTrack(barrage.scale)
            if (index == -1) {
                mWaitingQueue.push(barrage)
            } else {
                lockTrack(index, barrage.scale)
                barrage.setInitPosition(
                    width.toFloat(),
                    ((defaultSizePixel + mTrackOffset) * (index + 1)).toFloat()
                )
                barrage.state = BARRAGE_STATE_ON_DEPARTING
                barrage.trackIndex = index
                mLaunchBarrage[barrage.uid] = barrage
                Log.d("Trace", "Count : ${mLaunchBarrage.size}")
            }

        }

        fun unLockTrack(index : Int, size : Int) {
            Log.d("Trace", "Unlock $index, $size")
            mTrackMap.fill(true, index, index + size)
            if (mWaitingQueue.isNotEmpty()) {
                if (size >= mWaitingQueue.first?.scale ?: return)
                    addBarrage(mWaitingQueue.poll() ?: return)
            }
        }

        fun lockTrack(index : Int, size : Int) {
            Log.d("Trace", "lockTrack $index, $size")
            mTrackMap.fill(false, index, index + size)
        }

        fun release() {
            mWaitingQueue.clear()
            mLaunchBarrage.clear()
        }

        private fun findIdleTrack(n : Int) : Int {
            val reqSize : Array<Boolean> = Array(n) { true }
            return Collections.indexOfSubList(mTrackMap.asList(), reqSize.asList())
        }
    }
}