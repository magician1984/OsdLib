package idv.bruce.ui.osd

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Size
import java.lang.NullPointerException
import java.util.*
import kotlin.NoSuchElementException

internal class OSDQueue<T>(var eventListener : OsdEventListener? = null) : Queue<OSDItem<T>> {
    private companion object {
        const val ON_REMOVED = 0x00
    }

    var viewSize : Size? = null
        set(value) {
            val isChanged : Boolean =
                (field?.width == value?.width && field?.height == value?.height)

            field = value

            val size : Size = field ?: return

            if (isChanged)
                eventListener?.onContainerSizeChanged()
            else
                eventListener?.onContainerReady()

            var node : Node<T>? = first

            while (node != null) {
                node.element
                node = node.nextNode
            }
        }

    private var first : Node<T>? = null

    private var last : Node<T>? = null

    private var count = 0

    private val handler : Handler = CallBackHandler()

    private val message : Message = Message()

    override val size : Int
        get() = count

    override fun add(element : OSDItem<T>?) : Boolean {
        val item : OSDItem<T> = element ?: return false
        if (find(element) != null) return false
        if (viewSize != null)
            item.onWindowSizeChanged(viewSize!!.width, viewSize!!.height)
        linkLast(item)
        return true
    }

    override fun addAll(elements : Collection<OSDItem<T>>) : Boolean {
        if (containsAll(elements)) return false

        for (e in elements)
            linkLast(e)
        return true
    }

    override fun clear() {
        var node : Node<T>? = first ?: return

        while (node != null) {
            node.element.release()
            node = node.nextNode
        }
        first = null

        last = null
    }

    override fun iterator() : MutableIterator<OSDItem<T>> {
        val iterator : MutableIterator<OSDItem<T>> = object : MutableIterator<OSDItem<T>> {

            private var mNode : Node<T>? = first

            override fun hasNext() : Boolean {
                return mNode != null
            }

            override fun next() : OSDItem<T> {
                val n : Node<T> = mNode ?: throw  NullPointerException()
                mNode = mNode?.nextNode
                return n.element
            }

            override fun remove() {
                TODO("Not yet implemented")
            }
        }
        return iterator
    }

    override fun remove() : OSDItem<T> {
        return removeNode(first) ?: throw NoSuchElementException()
    }

    override fun contains(element : OSDItem<T>?) : Boolean {
        return find(element ?: return false) != null
    }

    override fun containsAll(elements : Collection<OSDItem<T>>) : Boolean {
        for (e in elements) {
            if (find(e) == null)
                return false
        }
        return true
    }

    override fun isEmpty() : Boolean {
        return size == 0
    }

    override fun remove(element : OSDItem<T>?) : Boolean {

        val item : OSDItem<T> = element ?: return false

        val node : Node<T> = find(item) ?: return false

//        removeNode(node)
        node.removed = true
        return true
    }

    override fun removeAll(elements : Collection<OSDItem<T>>) : Boolean {
        return false
    }

    override fun retainAll(elements : Collection<OSDItem<T>>) : Boolean {
        return false
    }

    override fun offer(e : OSDItem<T>?) : Boolean {
        val item : OSDItem<T> = e ?: return false
        linkLast(item)
        return true
    }

    override fun poll() : OSDItem<T>? {
        if (first == null)
            return null

        val element : OSDItem<T> = first!!.element

        removeNode(first)

        return element
    }

    override fun element() : OSDItem<T> {
        return first!!.element
    }

    override fun peek() : OSDItem<T>? {
        return first?.element
    }

    fun drawFrame(drawer : T, frameTimeNanos : Long, timeIntervalNanos : Long) {
        var node : Node<T>? = first

        var isRemove : Boolean

        while (node != null) {
            isRemove = node.element.drawFrame(drawer, frameTimeNanos, timeIntervalNanos) || node.removed

            node = node.nextNode

            if (isRemove) {
                message.what = ON_REMOVED
                message.obj = removeNode(node?.prevNode)
                handler.sendMessage(message)
            }
        }
    }


    private fun find(element : OSDItem<T>) : Node<T>? {
        var node : Node<T>? = first ?: return null

        while (node != null) {
            if (node.element == element)
                return node
            node = node.nextNode
        }
        return null
    }

    private fun removeNode(node : Node<T>?) : OSDItem<T>? {
        val mNode : Node<T> = node ?: return null

        val element : OSDItem<T> = mNode.element

        if (node == first)
            first = mNode.nextNode
        if (node == last)
            last = mNode.prevNode

        mNode.prevNode?.nextNode = mNode.nextNode
        mNode.nextNode?.prevNode = mNode.prevNode
        mNode.prevNode = null
        mNode.nextNode = null
        count--

        return element
    }

    private fun linkLast(element : OSDItem<T>) {
        val l : Node<T>? = last
        val node : Node<T> = Node(l, element, null)
        last = node
        if (l == null)
            first = node
        else
            l.nextNode = node
        count++
    }

    private class Node<T>(
        var prevNode : Node<T>?,
        var element : OSDItem<T>,
        var nextNode : Node<T>?
    ) {
        var removed : Boolean = false
    }

    private inner class CallBackHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg : Message) {
            when (msg.what) {
                ON_REMOVED -> eventListener?.onDone(msg.obj as OSDItem<*>)
            }
        }
    }
}