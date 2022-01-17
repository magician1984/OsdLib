package idv.bruce.ui.osd

import android.graphics.Canvas
import android.util.Size
import java.util.*
import kotlin.NoSuchElementException

internal class OSDQueue(var eventListener: OsdEventListener? = null) : Queue<OSDContainer> {
    var viewSize: Size? = null
        set(value) {
            val isChanged: Boolean =
                (field?.width == value?.width && field?.height == value?.height)

            field = value

            val size: Size = field ?: return

            if (isChanged)
                eventListener?.onContainerSizeChanged()
            else
                eventListener?.onContainerReady()

            var node: Node? = first

            while (node != null) {
                node.element.viewSize = size
                node = node.nextNode
            }
        }

    private var first: Node? = null

    private var last: Node? = null

    private var count = 0

    override val size: Int
        get() = count

    private val releaseThread: Thread = Thread()

    override fun add(element: OSDContainer?): Boolean {
        val item: OSDContainer = element ?: return false
        if (find(element) != null) return false
        item.viewSize = viewSize
        linkLast(item)
        return true
    }

    override fun addAll(elements: Collection<OSDContainer>): Boolean {
        if (containsAll(elements)) return false

        for (e in elements)
            linkLast(e)
        return true
    }

    override fun clear() {
        var node: Node? = first ?: return

        while (node != null) {
            node.element.release()
            node = node.nextNode
        }
        first = null

        last = null
    }

    override fun iterator(): MutableIterator<OSDContainer> {
        val iterator: Iterator<OSDContainer> = object : Iterator<OSDContainer> {
            override fun hasNext(): Boolean {
                TODO("Not yet implemented")
            }

            override fun next(): OSDContainer {
                TODO("Not yet implemented")
            }
        }
        return iterator()
    }

    override fun remove(): OSDContainer {
        val node: Node = first ?: throw NoSuchElementException()
        val item: OSDContainer = node.element
        removeNode(node)
        return item
    }

    override fun contains(element: OSDContainer?): Boolean {
        return find(element ?: return false) != null
    }

    override fun containsAll(elements: Collection<OSDContainer>): Boolean {
        for (e in elements) {
            if (find(e) == null)
                return false
        }
        return true
    }

    override fun isEmpty(): Boolean {
        return size == 0
    }

    override fun remove(element: OSDContainer?): Boolean {

        val item: OSDContainer = element ?: return false

        val node: Node = find(item) ?: return false

//        removeNode(node)
        node.removed = true
        return true
    }

    override fun removeAll(elements: Collection<OSDContainer>): Boolean {
        TODO("Not yet implemented")
    }

    override fun retainAll(elements: Collection<OSDContainer>): Boolean {
        TODO("Not yet implemented")
    }

    override fun offer(e: OSDContainer?): Boolean {
        val item: OSDContainer = e ?: return false
        linkLast(item)
        return true
    }

    override fun poll(): OSDContainer? {
        if (first == null)
            return null

        val element: OSDContainer = first!!.element

        removeNode(first)

        return element
    }

    override fun element(): OSDContainer {
        return first!!.element
    }

    override fun peek(): OSDContainer? {
        return first?.element
    }

    fun onDraw(canvas: Canvas) {
        var node: Node? = first

        val time: Long = System.currentTimeMillis()

        var isRemove: Boolean

        while (node != null) {
            isRemove = node.element.onUpdate(canvas, time) || node.removed

            node = node.nextNode

            if (isRemove) {
                removeNode(node?.prevNode)
            }
        }
    }


    private fun find(element: OSDContainer): Node? {
        var node: Node? = first ?: return null

        while (node != null) {
            if (node.element == element)
                return node
            node = node.nextNode
        }
        return null
    }

    private fun removeNode(node: Node?) {
        val mNode: Node = node ?: return

        val element: OSDContainer = mNode.element

        eventListener?.onDone(element)

        if (node == first)
            first = mNode.nextNode
        if (node == last)
            last = mNode.prevNode

        mNode.prevNode?.nextNode = mNode.nextNode
        mNode.nextNode?.prevNode = mNode.prevNode
        mNode.prevNode = null
        mNode.nextNode = null
        count--
//        eventListener?.onDone(element)
    }

    private fun linkLast(element: OSDContainer) {
        val l: Node? = last
        val node: Node = Node(l, element, null)
        last = node
        if (l == null)
            first = node
        else
            l.nextNode = node
        count++
    }

    private class Node(var prevNode: Node?, var element: OSDContainer, var nextNode: Node?) {
        var removed: Boolean = false
    }
}