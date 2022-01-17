package idv.bruce.ui.osd.items

import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Size
import android.util.SizeF
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import idv.bruce.ui.osd.OSDContainer
import java.lang.Exception
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class OSDPictureItem(
    private var uri: String,
    private var duration: Long,
    locationPercent: PointF,
    areaPercent: SizeF
) : OSDContainer(locationPercent, areaPercent) {

    private var mPaint: Paint = Paint()

    private var mBitmap: Bitmap? = null

    private var isLoaded: Boolean = false

    private var isError: Boolean = false

    private var mCanvas: Canvas? = null

    private var mMatrix: Matrix? = null

    private var mDrawSize: Size? = null

    private var mDrawPoint: Point? = null

    private val downloadThread: ExecutorService = Executors.newSingleThreadExecutor()

    override fun onUpdate(canvas: Canvas, time: Long): Boolean {
        if (isError) return true

        if (!isLoaded) return false

        if (mBitmap == null) return false

        if (startTime == -1L)
            startTime = time

        if (time - startTime >= duration)
            return true

        canvas.drawBitmap(mBitmap!!, mMatrix!!, mPaint)

        return false
    }

    override fun release() {
        if (!isLoaded)
            Picasso.get().cancelRequest(target)
        mBitmap?.recycle()
        downloadThread.shutdown()
    }

    private val target: Target = object : Target {
        override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
            if (bitmap == null) {
                isError = true
                return
            }

            from?.ordinal

            val w: Int =
                (bitmap.width.toFloat() * mDrawSize!!.height.toFloat() / bitmap.height.toFloat()).toInt()

            mCanvas!!.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            mCanvas!!.drawBitmap(
                bitmap,
                null,
                Rect(0, 0, w, mCanvas!!.height),
                mPaint
            )

            mMatrix!!.postTranslate((mDrawSize!!.width - w) / 2f, 0f)

            isLoaded = true
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            isError = true
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            isLoaded = false
        }
    }

    override fun onAttachToContainer(containerSize: Size) {

        mDrawSize = Size(
            if (size.width != 0f)
                (containerSize.width * size.width).toInt()
            else
                (containerSize.height * size.height).toInt(),
            if (size.height != 0f)
                (containerSize.height * size.height).toInt()
            else
                (containerSize.width * size.width).toInt()
        )

        val point: Point = Point(
            if (locationPercent.x != -1f)
                (containerSize.width * locationPercent.x).toInt()
            else
                (containerSize.width - mDrawSize!!.width) / 2,
            if (locationPercent.y != -1f)
                (containerSize.height * locationPercent.y).toInt()
            else
                (containerSize.height - mDrawSize!!.height) / 2
        )

        mBitmap =
            Bitmap.createBitmap(mDrawSize!!.width, mDrawSize!!.height, Bitmap.Config.ARGB_8888)

        mCanvas = Canvas(mBitmap!!)

        mMatrix = Matrix().apply {
            postTranslate(point.x.toFloat(), point.y.toFloat())
        }

        Picasso.get()
            .load(Uri.parse(uri))
            .rotate(90.0f)
            .into(target)

//        downloadThread.submit {
//            val exifInterface: ExifInterface =
//                ExifInterface(URL(uri).openConnection().getInputStream())
//
//            val rotate: Int = exifInterface.rotationDegrees
//
//            Log.d("Trace", "Rotate : $rotate")
//
//
//        }

    }
}