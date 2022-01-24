package idv.bruce.osd.item.barrage

import android.graphics.*
import android.text.TextPaint
import android.util.SizeF
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import idv.bruce.ui.osd.OSDItem
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.abs

class OSDBarrageItem(location: PointF, size: SizeF, private val defaultSizePixel: Int) :
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

    private lateinit var mLimit: Pair<Int, Int>

    private lateinit var mController: Controller

    private var mIsReady: Boolean = false

    private var mTextPaint: TextPaint =
        TextPaint(TextPaint.ANTI_ALIAS_FLAG or TextPaint.DITHER_FLAG)

    init {
        mTextPaint.textSize = defaultSizePixel.toFloat()
    }

    override fun onWindowSizeChanged(width: Int, height: Int) {

        mLimit = Pair(0, width)

        val matrix = mTextPaint.fontMetrics

        val trackHeight: Int = abs(matrix.top).toInt() + abs(matrix.bottom).toInt()
        mController = Controller(width, height, trackHeight)

        mIsReady = true
    }

    override fun drawFrame(
        drawer: Canvas,
        frameTimeNanos: Long,
        timeIntervalNanos: Long
    ): Boolean {
        if (mIsReady) {

            val iterator = mController.mLaunchBarrages.iterator()

            var barrage: Barrage
            synchronized(iterator) {
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

        }
        return isEnded
    }

    override fun release() {
        mController.release()
    }

    fun addText(
        txt: String,
        @ColorInt textColor: Int,
        @ColorInt outlineColor: Int,
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

        var state: Int = BARRAGE_STATE_ON_READY

        var trackIndex: Int = -1

        private var position: PointF = PointF()

        private val mRect: Rect = Rect()

        private val mItemTextPaint: TextPaint = TextPaint(mTextPaint)

        private val mPicture: Picture

        var uid: Long = System.nanoTime()

        init {
            mItemTextPaint.textSize *= scale.toFloat()

            mItemTextPaint.getTextBounds(txt, 0, txt.length, mRect)
            mItemTextPaint.strokeWidth = mItemTextPaint.textSize / 10f

            mPicture = Picture()

            val canvas = mPicture.beginRecording(mRect.width(), mRect.height())

            canvas.translate(position.x, position.y)

            mItemTextPaint.style = Paint.Style.STROKE
            mItemTextPaint.color = outlineColor
            canvas.drawText(txt, 0f, 0f, mItemTextPaint)

            mItemTextPaint.style = Paint.Style.FILL
            mItemTextPaint.color = textColor
            mItemTextPaint.bgColor
            canvas.drawText(txt, 0f, 0f, mItemTextPaint)

            mPicture.endRecording()

//            assert(mPicture.requiresHardwareAcceleration())
        }

        fun setInitPosition(x: Float, y: Float) {
            position.set(x, y)
        }


        fun draw(canvas: Canvas, timeIntervalNanos: Long): Int {
            if (state < BARRAGE_STATE_ON_DEPARTING) return BARRAGE_STATE_ERROR

            position.offset(-50f * (timeIntervalNanos.toFloat() / 1000000000f), 0f)

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

        override fun toString(): String =
            "Barrage(txt='$txt', textColor=$textColor, outlineColor=$outlineColor, velocity=$velocity, scale=$scale, state=$state, trackIndex=$trackIndex, position=$position, mRect=$mRect, mItemTextPaint=$mItemTextPaint, uid=$uid)"
    }


    private class Controller(private var width: Int, height: Int, private val trackHeight: Int) {
        private val mWaitingQueue: LinkedList<Barrage> = LinkedList()

        private var mTrackMap: Array<Boolean>

        private var mTrackOffset: Int = 0

        val mLaunchBarrages: ConcurrentLinkedQueue<Barrage> = ConcurrentLinkedQueue()

        init {
            val trackCount: Int = height / trackHeight

            mTrackMap = Array(trackCount) { true }

            mTrackOffset = (height % trackHeight) / trackCount

        }

        fun addBarrage(barrage: Barrage) {

            val index = findIdleTrack(barrage.scale)
            if (index == -1) {
                mWaitingQueue.push(barrage)
            } else {
                lockTrack(index, barrage.scale)
                barrage.setInitPosition(
                    width.toFloat(),
                    ((trackHeight + mTrackOffset) * (index + barrage.scale)).toFloat() - mTrackOffset
                )
                barrage.state = BARRAGE_STATE_ON_DEPARTING
                barrage.trackIndex = index
                mLaunchBarrages.add(barrage)
            }

        }

        fun unLockTrack(index: Int, size: Int) {
            mTrackMap.fill(true, index, index + size)
            if (mWaitingQueue.isNotEmpty()) {
                if (findIdleTrack(mWaitingQueue.first.scale) != -1)
                    addBarrage(mWaitingQueue.poll() ?: return)
            }
        }

        fun lockTrack(index: Int, size: Int) {
            mTrackMap.fill(false, index, index + size)
        }

        fun release() {
            mWaitingQueue.clear()
            mLaunchBarrages.clear()
        }

        private fun findIdleTrack(n: Int): Int {
            val reqSize: Array<Boolean> = Array(n) { true }
            return Collections.indexOfSubList(mTrackMap.asList(), reqSize.asList())
        }
    }
}