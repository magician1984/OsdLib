package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OSDQueue
import idv.bruce.ui.osd.OsdView
import idv.bruce.ui.osd.OsdEventListener
import kotlinx.coroutines.launch

class OSDSurfaceView(context: Context, attr: AttributeSet) : SurfaceView(context, attr),
    Choreographer.FrameCallback,
    OsdView<Canvas> {
    companion object {
        const val TAG: String = "OSD_Container"
    }

    private val queue: OSDQueue<Canvas> = OSDQueue()

    private val choreographer: Choreographer = Choreographer.getInstance()


    var eventListener: OsdEventListener<Canvas>?
        get() {
            return queue.eventListener
        }
        set(value) {
            queue.eventListener = value
        }

    private var mHolder: SurfaceHolder? = null

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

        setZOrderMediaOverlay(true)

        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                Log.d(TAG, "surfaceCreated")

                mHolder = holder

                mHolder!!.setFormat(PixelFormat.TRANSLUCENT)
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                Log.d(TAG, "surfaceChanged : $format, $width, $height")
                if (mWidth != width || mHeight != height)
                    isSurfaceReady = false

                if (!isSurfaceReady) {
                    mWidth = width
                    mHeight = height
                    queue.viewSize = Size(width, height)
                    isSurfaceReady
                }

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isSurfaceReady = false
            }
        })
    }

    override fun doFrame(frameTimeNanos: Long) {

        if (isResume && isSurfaceReady && queue.isNotEmpty()) {
            val mCanvas = mHolder?.lockCanvas() ?: return

            if (mLastTimeNanos == -1L)
                mLastTimeNanos = frameTimeNanos

            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            queue.drawFrame(mCanvas, frameTimeNanos, mLastTimeNanos)

            mLastTimeNanos = frameTimeNanos

            mHolder?.unlockCanvasAndPost(mCanvas)

            choreographer.postFrameCallback(this)
        } else {
            isResume = false
        }


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