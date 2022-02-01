package idv.bruce.osd.item.picture

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.lang.ref.WeakReference
import java.net.URL
import java.net.URLConnection

class Picture private constructor(builder : Builder) {
    companion object {
        const val STATE_NONE = 0

        const val STATE_DOWNLOADING = 1

        const val STATE_READY = 2

        const val TYPE_UNKNOWN = -1

        const val TYPE_FILE = 0

        const val TYPE_INPUT_STREAM = 1

        const val TYPE_NET = 2
    }

    class Builder(context : Context) {
        private val contextRef : WeakReference<Context> = WeakReference(context)

        internal var type : Int = TYPE_UNKNOWN

        internal var input : InputStream? = null

        internal val mCachePath : File = File(context.cacheDir, "picture_cache")

        init {
            if (!mCachePath.exists())
                mCachePath.mkdirs()
        }

        fun load(file : File) = apply {
            type = TYPE_FILE
            input = FileInputStream(file)
        }

        fun load(inputStream : InputStream) = apply {
            type = TYPE_INPUT_STREAM
            input = inputStream
        }

        fun load(url : String) = apply {
            type = TYPE_NET
            val connection:URLConnection = URL(url).openConnection()
            input = connection.getInputStream()
        }
    }

    internal var state : Int = STATE_NONE

    internal var mType : Int = builder.type

    internal var bitmap : Bitmap? = null

    internal fun prepare() {

    }
}