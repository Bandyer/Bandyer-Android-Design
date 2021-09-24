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

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_chat_menu_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_contact_details_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     *
     *
     * @property binding [@androidx.annotation.NonNull] BandyerChatMenuItemLayoutBinding
     * @constructor
     */
    class ViewHolder(view: View): FastAdapter.ViewHolder<BandyerChatMenuItem>(view) {

        private val binding = BandyerChatMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: BandyerChatMenuItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: BandyerChatMenuItem) {
            binding.bandyerText.text = null
        }
    }
}