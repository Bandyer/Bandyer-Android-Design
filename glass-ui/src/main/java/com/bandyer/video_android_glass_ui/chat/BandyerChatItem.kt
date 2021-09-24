package com.bandyer.video_android_glass_ui.chat

import android.view.View
import com.bandyer.video_android_core_ui.extensions.StringExtensions.parseToColor
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerChatItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A chat item
 *
 * @property data The data related to a chat message
 * @constructor
 */
class BandyerChatItem(val data: BandyerGlassMessageData) : AbstractItem<BandyerChatItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.bandyer_chat_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_chat_item

    /**
     * This method returns the ViewHolder for our item, using the provided View.
     *
     * @return the ViewHolder for this Item
     */
    override fun getViewHolder(v: View) = ViewHolder(v)

    /**
     * The view holder for a chat item
     *
     * @constructor
     */
    class ViewHolder(view: View) : FastAdapter.ViewHolder<BandyerChatItem>(view) {

        private val binding: BandyerChatItemLayoutBinding = BandyerChatItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: BandyerChatItem, payloads: List<Any>) =
            with(binding.bandyerChatMessage) {
                itemView.isClickable = false
                val data = item.data
                if(data.userAvatarId != null) setAvatar(data.userAvatarId)
                else if (data.userAvatarUrl != null) setAvatar(data.userAvatarUrl)
                setAvatarBackground(data.userAlias?.parseToColor())
                setMessage(data.message)
                setTime(data.time)
                if (!data.isFirstPage) hideName()
                else setName(data.sender)
            }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: BandyerChatItem) = with(binding.bandyerChatMessage) {
            itemView.isClickable = true
            setAvatar(null)
            setName(null)
            showName()
            setAvatarBackground(null)
            setTime(null)
            setMessage(null)
        }
    }
}