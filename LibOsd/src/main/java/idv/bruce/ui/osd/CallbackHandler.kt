package idv.bruce.ui.osd

import android.os.Handler
import android.os.Looper
import android.util.Log

internal class CallbackHandler<T>(var eventListener : OsdEventListener<T>?) :
    Handler(Looper.getMainLooper()) {
    companion object {
        const val TAG : String = "OSDView"
    }

    fun onItemRemoved(item : OSDItem<T>) =
        post {
            eventListener?.onDone(item)
        }


    fun onReady() =
        post {
            eventListener?.onContainerReady()
        }


    fun onSizeChanged() =
        post {
            eventListener?.onContainerSizeChanged()
        }


    fun debug(msg : String) {
        post {
            Log.d(TAG, msg)
        }
    }
}