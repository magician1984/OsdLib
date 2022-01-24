package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.view.Choreographer
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import idv.bruce.ui.osd.*
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

class OSDBaseView(context : Context, attr : AttributeSet) : View(context, attr),
                                                            Choreographer.FrameCallback,
                                                            OsdView<Canvas> {

    private val queue : ConcurrentLinkedQueue<OSDItem<Canvas>> = ConcurrentLinkedQueue()

    private val choreographer : Choreographer = Choreographer.getInstance()

    var eventListener : OsdEventListener?
        get() = mHandler.eventListener
        set(value) {
            mHandler.eventListener = value
        }

    private var isResume : Boolean = false

    private var isSurfaceReady : Boolean = false

    private var mWidth : Int = -1

    private var mHeight : Int = -1

    private var mLastTimeNanos : Long = -1L

    private val mHandler : CallbackHandler<Canvas> = CallbackHandler(null)


    init {
        (context as LifecycleOwner).apply {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    onStart()
                }
                repeatOnLifecycle(Lifecycle.State.CREATED) {
                    onStop()
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec : Int, heightMeasureSpec : Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (mWidth != measuredWidth || mHeight != measuredHeight)
            isSurfaceReady = false

        if (!isSurfaceReady) {
            mWidth = measuredWidth
            mHeight = measuredHeight
            if (queue.isNotEmpty()) {
                queue.forEach {
                    it.onWindowSizeChanged(mWidth, mHeight)
                }
            }
            isSurfaceReady = true
            mHandler.onReady()
            mHandler.onSizeChanged()
        }
    }

    override fun onDraw(canvas : Canvas?) {
        if (isResume && isSurfaceReady) {
            val frameTimeNanos : Long = System.nanoTime()

            val mCanvas = canvas ?: return

            assert(mCanvas.isHardwareAccelerated)

            if (mLastTimeNanos == -1L)
                mLastTimeNanos = frameTimeNanos

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            val iterator = queue.iterator()

            while (iterator.hasNext()) {
                iterator.next().apply {
                    if (this.drawFrame(mCanvas, frameTimeNanos, frameTimeNanos - mLastTimeNanos)) {
                        iterator.remove()
                        mHandler.onItemRemoved(this)
                    }
                }
            }

            mLastTimeNanos = frameTimeNanos
//            if (BuildConfig.DEBUG)
//                mHandler.debug("${(System.nanoTime() - frameTimeNanos) / 1000000f} ms")

        } else {
            super.onDraw(canvas)
        }
    }

    override fun doFrame(frameTimeNanos : Long) {
        postInvalidate()

        choreographer.postFrameCallback(this)
    }

    override fun addOsdItem(item : OSDItem<Canvas>) {
        if (isSurfaceReady)
            item.onWindowSizeChanged(mWidth, mHeight)
        queue.add(item)


        onStart()
    }

    override fun removeOsdItem(item : OSDItem<Canvas>) {
        queue.remove(item)
    }

    private fun onStart() {
        if (!isResume) {
            isResume = true
            choreographer.postFrameCallback(this)
        }
    }

    private fun onStop() {
        if (isResume) {
            isResume = false
            choreographer.removeFrameCallback(this)
        }
    }
}