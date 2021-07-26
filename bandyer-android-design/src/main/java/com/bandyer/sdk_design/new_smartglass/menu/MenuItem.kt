package com.bandyer.sdk_design.new_smartglass.menu

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class MenuItem(val text: String, val activeText: String? = null): AbstractItem<MenuItem.ViewHolder>() {

    private var itemText: ActivableTextView? = null

    var isActive = false
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

    class ViewHolder(view: View): FastAdapter.ViewHolder<MenuItem>(view) {

        private val binding: BandyerMenuItemLayoutBinding = BandyerMenuItemLayoutBinding.bind(view)

        override fun bindView(item: MenuItem, payloads: List<Any>) {
            item.itemText = binding.itemText
            binding.itemText.activatedText = item.activeText
            binding.itemText.inactivatedText = item.text
            binding.itemText.isActivated = item.isActive
        }

        override fun unbindView(item: MenuItem) {
            item.itemText = null
            binding.itemText.activatedText = null
            binding.itemText.inactivatedText = null
            binding.itemText.isActivated = false
        }
    }
}