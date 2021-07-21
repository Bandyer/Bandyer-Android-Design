package com.bandyer.sdk_design.new_smartglass

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class MenuItem(val text: String): AbstractItem<MenuItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_menu_item_layout

    override val type: Int
        get() = R.id.id_menu_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<MenuItem>(view) {

        private val binding: BandyerMenuItemLayoutBinding = BandyerMenuItemLayoutBinding.bind(view)

        override fun bindView(item: MenuItem, payloads: List<Any>) {
            binding.itemText.text = item.text
        }

        override fun unbindView(item: MenuItem) {
            binding.itemText.text = null
        }
    }
}