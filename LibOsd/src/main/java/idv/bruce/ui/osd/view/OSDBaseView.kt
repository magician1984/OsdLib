package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OSDQueue
import idv.bruce.ui.osd.OsdEventListener
import idv.bruce.ui.osd.OsdView
import kotlinx.coroutines.launch

class OSDBaseView(context: Context, attr: AttributeSet) : View(context, attr),
                                                             Choreographer.FrameCallback,
                                                             OsdView<Canvas> {
    companion object {
        const val TAG: String = "OSD_Container"
    }

    private val queue: OSDQueue<Canvas> = OSDQueue()

    private val choreographer: Choreographer = Choreographer.getInstance()


    var eventListener: OsdEventListener<Canvas>?
        get() = queue.eventListener
        set(value) {
            queue.eventListener = value
        }

    private var isResume: Boolean = false

    private var isSurfaceReady: Boolean = false

    private var mWidth: Int = -1

    private var mHeight: Int = -1

    private var mLastTimeNanos: Long = -1L

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
        Log.d(TAG, "surfaceChanged : $measuredWidth, $measuredHeight")
        if (mWidth != measuredWidth || mHeight != measuredHeight)
            isSurfaceReady = false

        if (!isSurfaceReady) {
            mWidth = measuredWidth
            mHeight = measuredHeight
            queue.viewSize = Size(mWidth, mHeight)
            isSurfaceReady = true
        }
    }

    override fun onDraw(canvas : Canvas?) {
        if (isResume && isSurfaceReady && queue.isNotEmpty()) {
            val frameTimeNanos : Long = System.nanoTime()

            val mCanvas = canvas ?: return

            assert(mCanvas.isHardwareAccelerated)

            if (mLastTimeNanos == -1L)
                mLastTimeNanos = frameTimeNanos

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            queue.drawFrame(mCanvas, frameTimeNanos, frameTimeNanos - mLastTimeNanos)

            mLastTimeNanos = frameTimeNanos

        }else{
            super.onDraw(canvas)
        }
    }

    override fun doFrame(frameTimeNanos: Long) {
        postInvalidate()

        choreographer.postFrameCallback(this)
    }

    override fun addOsdItem(item: OSDItem<Canvas>) {
        queue.add(item)


        onStart()
    }

    override fun removeOsdItem(item: OSDItem<Canvas>) {
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