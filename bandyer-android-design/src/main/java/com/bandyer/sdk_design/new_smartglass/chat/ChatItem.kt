package com.bandyer.sdk_design.new_smartglass.chat

import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatItemLayoutBinding
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

        override fun bindView(item: ChatItem, payloads: List<Any>) {
            binding.message.setAvatar(item.data.avatar)
            binding.message.setName(item.data.name)
            binding.message.setMessage(item.data.message)
            binding.message.setTime(item.data.time)
        }

        override fun unbindView(item: ChatItem) {
            binding.message.setAvatar(null)
            binding.message.setName(null)
            binding.message.setTime(null)
            binding.message.setMessage(null)
        }
    }
}