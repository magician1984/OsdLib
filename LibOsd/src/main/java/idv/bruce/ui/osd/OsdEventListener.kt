package idv.bruce.ui.osd

interface OsdEventListener {
    fun onDone(item: OSDContainer)
    fun onContainerReady()
    fun onContainerSizeChanged()
}