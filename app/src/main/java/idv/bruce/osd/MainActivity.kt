package idv.bruce.osd

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.Log
import android.util.SizeF
import androidx.appcompat.app.AppCompatActivity
import idv.bruce.osd.databinding.ActivityMainBinding
import idv.bruce.osd.item.barrage.OSDBarrageItem
import idv.bruce.ui.osd.OSDItem
import idv.bruce.ui.osd.OsdEventListener
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val rnd: Random = Random(System.currentTimeMillis())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.osd.eventListener = object : OsdEventListener {

            override fun onDone(item: OSDItem<*>) {

            }

            override fun onContainerReady() {
                val item: OSDBarrageItem =
                    OSDBarrageItem(resources.getDimensionPixelSize(R.dimen.def_text_size))

                Log.d("Trace", "OnReady")
                binding.osd.addOsdItem(item)

                binding.run.setOnClickListener {
                    item.addText(
                        "eedddccc",
                        Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)),
                        Color.argb(255, rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)),
                        20f,
                        rnd.nextInt(0, 3)
                    )
                }
            }

            override fun onContainerSizeChanged() {

            }
        }


    }
}