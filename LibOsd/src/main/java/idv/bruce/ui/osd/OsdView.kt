package idv.bruce.ui.osd

interface OsdView {

    fun addOsdItem(item: OSDContainer)

    fun removeOsdItem(item: OSDContainer)

    fun onStart()

    fun onStop()
}