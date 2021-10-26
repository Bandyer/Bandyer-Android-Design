package com.bandyer.video_android_glass_ui.menu

import android.view.View
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassMenuItemLayoutBinding
import com.bandyer.video_android_glass_ui.utils.CallAction
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A menu item. If the active text is null, isActivated will always be false.
 * @constructor
 */
class MenuItem(val action: CallAction): AbstractItem<MenuItem.ViewHolder>() {

    /**
     * Set an unique identifier for the identifiable which do not have one set already
     */
    override var identifier: Long = action.hashCode().toLong()

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = action.hashCode()

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = action.layoutRes

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * View holder for menu item
     *
     * @constructor
     */
    class ViewHolder(view: View): FastAdapter.ViewHolder<MenuItem>(view) {

        private val binding: BandyerGlassMenuItemLayoutBinding = BandyerGlassMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: MenuItem, payloads: List<Any>) = with(binding.bandyerText) {
            item.action.itemView = itemView
            item.action.onReady()
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: MenuItem) = with(binding.bandyerText) {
            item.action.itemView = null
        }
    }
}