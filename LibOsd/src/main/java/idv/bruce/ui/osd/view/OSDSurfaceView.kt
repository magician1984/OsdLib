package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.*
import android.os.Build
import android.util.AttributeSet
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import idv.bruce.ui.osd.*
import idv.bruce.ui.osd.CallbackHandler
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue

@RequiresApi(Build.VERSION_CODES.O)
class OSDSurfaceView(context: Context, attr: AttributeSet) : SurfaceView(context, attr),
    Choreographer.FrameCallback,
    OsdView<Canvas> {
    private val queue: ConcurrentLinkedQueue<OSDItem<Canvas>> = ConcurrentLinkedQueue()

    private val choreographer: Choreographer = Choreographer.getInstance()


    var eventListener: OsdEventListener?
        get() = mHandler.eventListener
        set(value) {
            mHandler.eventListener = value
        }
    private var mHolder: SurfaceHolder? = null

    private var isResume: Boolean = false

    private var isSurfaceReady: Boolean = false

    private var mWidth: Int = -1

    private var mHeight: Int = -1

    private var mLastTimeNanos: Long = -1L

    private val mHandler: CallbackHandler<Canvas> = CallbackHandler(null)

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

                mHolder = holder

                mHolder!!.setFormat(PixelFormat.TRANSLUCENT)

                mHandler.onReady()
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
                if (mWidth != width || mHeight != height)
                    isSurfaceReady = false

                if (!isSurfaceReady) {
                    mWidth = width
                    mHeight = height
                    if (queue.isNotEmpty()) {
                        queue.forEach {
                            it.onWindowSizeChanged(mWidth, mHeight)
                        }
                    }
                    isSurfaceReady = true
                    mHandler.onSizeChanged()
                }

                onStart()
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                isSurfaceReady = false
            }
        })
    }


    override fun doFrame(frameTimeNanos: Long) {
        if (isResume && isSurfaceReady && queue.isNotEmpty()) {

            val mCanvas = mHolder?.lockCanvas() ?: return

            val t: Long = System.nanoTime()

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

            mHolder?.unlockCanvasAndPost(mCanvas)

            if (BuildConfig.DEBUG)
                mHandler.debug("${(System.nanoTime() - t) / 1000000f} ms")

            choreographer.postFrameCallback(this)
        } else {
            isResume = false
        }


    }

    override fun addOsdItem(item: OSDItem<Canvas>) {
        if (isSurfaceReady)
            item.onWindowSizeChanged(mWidth, mHeight)
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