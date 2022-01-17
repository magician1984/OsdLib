package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.Size
import android.view.Choreographer
import android.view.SurfaceHolder
import android.view.SurfaceView
import idv.bruce.ui.osd.OSDContainer
import idv.bruce.ui.osd.OSDQueue
import idv.bruce.ui.osd.OsdView
import idv.bruce.ui.osd.OsdEventListener
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class OSDSurfaceView(context: Context, attr: AttributeSet) : SurfaceView(context, attr),
    Choreographer.FrameCallback, OsdView {
    companion object {
        const val TAG: String = "OSD_Container"
    }

    private val queue: OSDQueue = OSDQueue()

    private val service: ExecutorService = Executors.newSingleThreadExecutor()

    private var future: Future<*>? = null

    private val choreographer: Choreographer = Choreographer.getInstance()

    var fps: Int = 60

    var eventListener: OsdEventListener?
        get() {
            return queue.eventListener
        }
        set(value) {
            queue.eventListener = value
        }

    private var mHolder: SurfaceHolder? = null

    private var isAnime: Boolean = false

    private var frameCount: Byte = 0

    init {
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
                queue.viewSize = Size(width, height)

            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {

            }
        })
    }

    override fun doFrame(frameTimeNanos: Long) {

        if (isAttachedToWindow && queue.viewSize != null && queue.isNotEmpty()) {
            isAnime = true
            val mCanvas = mHolder?.lockCanvas() ?: return
            mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            queue.onDraw(mCanvas)
            mHolder?.unlockCanvasAndPost(mCanvas)
            choreographer.postFrameCallback(this)
        } else {
            isAnime = false
        }


    }

    override fun addOsdItem(item: OSDContainer) {
        queue.add(item)
        onStart()
    }

    override fun removeOsdItem(item: OSDContainer) {
        queue.remove(item)
    }

    override fun onStart() {
        if (!isAnime)
            choreographer.postFrameCallback(this)
    }

    override fun onStop() {
        if (isAnime)
            choreographer.removeFrameCallback(this)
    }

}