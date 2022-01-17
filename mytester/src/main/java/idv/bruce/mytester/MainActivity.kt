package idv.bruce.mytester

import android.app.ActivityManager
import android.content.Context
import android.content.pm.ConfigurationInfo
import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import idv.bruce.mytester.databinding.ActivityMainBinding
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val am: ActivityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val configurationInfo: ConfigurationInfo = am.deviceConfigurationInfo

        binding.surface.setRenderer(MyRenderer())

        Log.d("Trace", "GLES version : ${configurationInfo.glEsVersion}")
    }

    override fun onResume() {
        super.onResume()
        binding.surface.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.surface.onPause()
    }

    private class MyRenderer : GLSurfaceView.Renderer {
        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {

        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {

        }

        override fun onDrawFrame(gl: GL10?) {

        }
    }
}