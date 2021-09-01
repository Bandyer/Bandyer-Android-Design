package com.bandyer.sdk_design.new_smartglass.chat

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatItemLayoutBinding
import com.bandyer.sdk_design.extensions.parseToColor
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A chat item
 *
 * @property data The data related to a chat message
 * @constructor
 */
class ChatItem(val data: SmartGlassMessageData) : AbstractItem<ChatItem.ViewHolder>() {

    override val layoutRes: Int
        get() = R.layout.bandyer_chat_item_layout

    override val type: Int
        get() = R.id.id_chat_item

    override fun getViewHolder(v: View) = ViewHolder(v)

    class ViewHolder(view: View) : FastAdapter.ViewHolder<ChatItem>(view) {

        private val binding: BandyerChatItemLayoutBinding = BandyerChatItemLayoutBinding.bind(view)

        override fun bindView(item: ChatItem, payloads: List<Any>) =
            with(binding.bandyerChatMessage) {
                val data = item.data
                if(data.userAvatarId != null) setAvatar(data.userAvatarId)
                else if (data.userAvatarUrl != null) setAvatar(data.userAvatarUrl)
                setAvatarBackground(data.userAlias?.parseToColor())
                setMessage(data.message)
                setTime(data.time)
                if (!data.isFirstPage) hideName()
                else setName(data.sender)
            }

        override fun unbindView(item: ChatItem) = with(binding.bandyerChatMessage) {
            setAvatar(null)
            setName(null)
            showName()
            setAvatarBackground(null)
            setTime(null)
            setMessage(null)
        }
    }
}