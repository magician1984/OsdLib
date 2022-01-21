package idv.bruce.ui.osd

interface OsdEventListener<T> {
    fun onDone(item: OSDItem<T>)
    fun onContainerReady()
    fun onContainerSizeChanged()
}