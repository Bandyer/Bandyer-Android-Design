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

        override fun bindView(item: ChatItem, payloads: List<Any>): Unit =
            with(binding.bandyerMessage) {
                val data = item.data
                setAvatar(data.avatar)
                setName(data.name)
                setAvatarBackground(data.userAlias?.parseToColor())
                setMessage(data.message)
                setTime(data.time)
            }

        override fun unbindView(item: ChatItem) = with(binding.bandyerMessage) {
            setAvatar(null)
            setName(null)
            setAvatarBackground(null)
            setTime(null)
            setMessage(null)
        }
    }
}