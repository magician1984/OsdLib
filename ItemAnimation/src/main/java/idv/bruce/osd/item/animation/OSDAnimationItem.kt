package idv.bruce.osd.item.animation

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.PointF
import android.util.SizeF
import com.linecorp.apng.ApngDrawable
import idv.bruce.ui.osd.OSDItem
import pl.droidsonroids.gif.GifDrawable
import java.io.File

class OSDAnimationItem(
    private val filePath: String,
    private val type: FileType,
    private val repeatCount: Int,
    location: PointF,
    size: SizeF
) :
    OSDItem<Canvas>(location, size) {
    sealed class FileType {
        object GIF : FileType()
        object APNG : FileType()
    }

    private var mApngDrawable: ApngDrawable? = null

    private var mGifDrawable: GifDrawable? = null

    private var mDurationMillis: Long = -1L

    private var mPlayCount: Int = 0

    private var mStartTimeNanos: Long = -1L

    override fun onWindowSizeChanged(width: Int, height: Int) {
        super.onWindowSizeChanged(width, height)

        when (type) {
            FileType.APNG -> mApngDrawable = generateApng()
            FileType.GIF -> mGifDrawable = generateGif()
        }
    }

    override fun drawFrame(drawer: Canvas, frameTimeNanos: Long, timeIntervalNanos: Long): Boolean {
        return if (super.drawFrame(drawer, frameTimeNanos, timeIntervalNanos))
            true
        else {
            if (mStartTimeNanos == -1L) {
                mPlayCount++
                mStartTimeNanos = frameTimeNanos
            }

            if ((frameTimeNanos - mStartTimeNanos) / 1000000L >= mDurationMillis) {
                if (mPlayCount == repeatCount)
                    return true
                else
                    mStartTimeNanos = -1L
            }

            when (type) {
                FileType.APNG -> {
                    mApngDrawable!!.seekTo((frameTimeNanos - mStartTimeNanos) / 1000000L)

                    mApngDrawable!!.draw(drawer)
                }
                FileType.GIF -> {
                    mGifDrawable!!.seekTo(((frameTimeNanos - mStartTimeNanos) / 1000000L).toInt())

                    mGifDrawable!!.draw(drawer)
                }
            }
            isEnded
        }
    }

    override fun release() {
        mApngDrawable?.recycle()
        mGifDrawable?.recycle()
    }

    @SuppressLint("Range")
    private fun generateApng() =
        ApngDrawable.Companion.decode(File(filePath)).apply {
            this.bounds = mRect
            mDurationMillis = this.durationMillis.toLong()
        }


    private fun generateGif() =
        GifDrawable(filePath).apply {
            this.bounds = mRect
            mDurationMillis = this.duration.toLong()
        }
}