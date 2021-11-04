package com.bandyer.video_android_glass_ui.chat.menu

import android.view.View
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassChatMenuItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A menu option item in chat fragment.
 *
 * @property text The action text
 * @constructor
 */
internal class ChatMenuItem(val text: String): AbstractItem<ChatMenuItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_glass_chat_menu_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_chat_menu_item

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
    class ViewHolder(view: View): FastAdapter.ViewHolder<ChatMenuItem>(view) {

        private val binding = BandyerGlassChatMenuItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: ChatMenuItem, payloads: List<Any>) {
            binding.bandyerText.text = item.text
        }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: ChatMenuItem) {
            binding.bandyerText.text = null
        }
    }
}