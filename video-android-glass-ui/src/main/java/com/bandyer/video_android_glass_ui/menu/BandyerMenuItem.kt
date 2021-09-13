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

    var isActivated = false
        set(value) {
            if(activeText == null) return
            field = value
            itemText?.isActivated = field
        }

    override val layoutRes: Int
        get() = R.layout.bandyer_menu_item_layout

    override val type: Int
        get() = R.id.id_menu_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerMenuItem>(view) {

        private val binding: BandyerMenuItemLayoutBinding = BandyerMenuItemLayoutBinding.bind(view)

        override fun bindView(item: BandyerMenuItem, payloads: List<Any>) = with(binding.bandyerText) {
            item.itemText = this
            this.activeText = item.activeText
            this.defaultText = item.defaultText
            this.isActivated = item.isActivated
        }

        override fun unbindView(item: BandyerMenuItem) = with(binding.bandyerText) {
            item.itemText = null
            this.activeText = null
            this.defaultText = null
            this.isActivated = false
        }
    }
}