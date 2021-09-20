package com.bandyer.video_android_glass_ui.chat.menu

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerChatMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A participant details item.
 *
 * @property text The action text
 * @constructor
 */
class BandyerChatMenuItem(val text: String): AbstractItem<BandyerChatMenuItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_chat_menu_item_layout

    override val type: Int
        get() = R.id.id_glass_contact_details_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerChatMenuItem>(view) {

        private val binding = BandyerChatMenuItemLayoutBinding.bind(view)

        override fun bindView(item: BandyerChatMenuItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        override fun unbindView(item: BandyerChatMenuItem) {
            binding.bandyerText.text = null
        }
    }
}