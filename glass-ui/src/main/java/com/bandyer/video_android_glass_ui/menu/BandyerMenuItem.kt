package com.bandyer.video_android_glass_ui.menu

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A menu item. If the active text is null, isActivated will always be false.
 *
 * @property defaultText The item's text
 * @property activeText The item's text when active
 * @constructor
 */
class BandyerMenuItem(val defaultText: String, val activeText: String? = null): AbstractItem<BandyerMenuItem.ViewHolder>() {

    private var itemText: BandyerActivableTextView? = null

    /**
     * Activated state of a menu item. The text will be updated accordingly
     */
    var isActivated = false
        set(value) {
            if(activeText == null) return
            field = value
            itemText?.isActivated = field
        }

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_menu_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_menu_item

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
    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerMenuItem>(view) {

        private val binding: BandyerMenuItemLayoutBinding = BandyerMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: BandyerMenuItem, payloads: List<Any>) = with(binding.bandyerText) {
            item.itemText = this
            this.activeText = item.activeText
            this.defaultText = item.defaultText
            this.isActivated = item.isActivated
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: BandyerMenuItem) = with(binding.bandyerText) {
            item.itemText = null
            this.activeText = null
            this.defaultText = null
            this.isActivated = false
        }
    }
}