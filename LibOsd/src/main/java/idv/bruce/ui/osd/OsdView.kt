package idv.bruce.ui.osd

interface OsdView<T> {
    fun addOsdItem(item : OSDItem<T>)

    fun removeOsdItem(item : OSDItem<T>)
}