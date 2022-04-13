/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.chat

import android.net.Uri
import android.view.View
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassChatMessageItemLayoutBinding
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.items.AbstractItem

/**
 * A chat item
 *
 * @property data The data related to a chat message
 * @constructor
 */
internal class ChatMessageItem(val data: ChatMessage) : AbstractItem<ChatMessageItem.ViewHolder>() {

    /**
     * The layout for the given item
     */
    override val layoutRes: Int
        get() = R.layout.kaleyra_glass_chat_message_item_layout

    /**
     * The type of the Item. Can be a hardcoded INT, but preferred is a defined id
     */
    override val type: Int
        get() = R.id.id_glass_chat_message_item

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
    class ViewHolder(view: View) : FastAdapter.ViewHolder<ChatMessageItem>(view) {

        private val binding: KaleyraGlassChatMessageItemLayoutBinding = KaleyraGlassChatMessageItemLayoutBinding.bind(view)

        /**
         * Binds the data of this item onto the viewHolder
         */
        override fun bindView(item: ChatMessageItem, payloads: List<Any>) =
            with(binding.kaleyraChatMessage) {
                itemView.isClickable = false
                val data = item.data
                if (data.avatar != Uri.EMPTY) kaleyraAvatar.setImage(data.avatar)
                kaleyraAvatar.setBackground(data.sender.parseToColor())
                kaleyraAvatar.setText(data.sender.first().toString())
                kaleyraMessage.text = data.message
                kaleyraTime.text = Iso8601.parseTimestamp(itemView.context, data.time)
                if (!data.isFirstPage) kaleyraName.visibility = View.GONE
                else kaleyraName.text = data.sender
            }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: ChatMessageItem) = with(binding.kaleyraChatMessage) {
            itemView.isClickable = true
            kaleyraName.text = null
            kaleyraMessage.text = null
            kaleyraTime.text = null
            kaleyraAvatar.setImage(null)
            kaleyraAvatar.setBackground(color = null)
            kaleyraAvatar.setText(null)
            kaleyraName.visibility = View.VISIBLE
        }
    }
}