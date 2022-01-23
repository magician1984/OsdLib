package idv.bruce.ui.osd.view

import android.content.Context
import android.graphics.SurfaceTexture
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.Surface
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OsdView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OSDGlSurfaceView(context: Context, attrs: AttributeSet) : GLSurfaceView(context, attrs),
    OsdView<GLES30> {
    private lateinit var surface: Surface

    private var isVideoSurfaceUpdated: Boolean = false

    init {
        setRenderer(PlayRenderer())
    }

    override fun addOsdItem(item: OSDItem<GLES30>) {

    }

    override fun removeOsdItem(item: OSDItem<GLES30>) {

    }

    private class PlayRenderer : Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        }

        override fun onDrawFrame(gl: GL10?) {

        }
    }

    val onFrameAvailableLister: SurfaceTexture.OnFrameAvailableListener =
        SurfaceTexture.OnFrameAvailableListener {
            isVideoSurfaceUpdated = true
            requestRender()
        }
}