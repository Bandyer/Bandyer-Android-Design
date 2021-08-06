package com.bandyer.sdk_design.new_smartglass.chat

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatItemLayoutBinding
import com.bandyer.sdk_design.extensions.parseToColor
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

class ChatItem(val data: SmartGlassChatData) : AbstractItem<ChatItem.ViewHolder>() {

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
                if(data.avatarId != null) setAvatar(data.avatarId)
                else if (data.avatarUrl != null) setAvatar(data.avatarUrl)
                setAvatarBackground(data.userAlias?.parseToColor())
                setMessage(data.message)
                setTime(data.time)
                if (!data.isFirstMessagePage) setNameVisibility(View.GONE)
                else setName(data.name)
            }

        override fun unbindView(item: ChatItem) = with(binding.bandyerChatMessage) {
            setAvatar(null)
            setName(null)
            setNameVisibility(View.VISIBLE)
            setAvatarBackground(null)
            setTime(null)
            setMessage(null)
        }
    }
}