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

import android.view.View
import com.kaleyra.collaboration_suite_core_ui.extensions.StringExtensions.parseToColor
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
internal class ChatMessageItem(val data: ChatMessageData) : AbstractItem<ChatMessageItem.ViewHolder>() {

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
                if (data.userAvatarId != null) setAvatar(data.userAvatarId)
                else if (data.userAvatarUrl != null) setAvatar(data.userAvatarUrl)
                setAvatarBackground(data.userId?.parseToColor())
                setMessage(data.message)
                setTime(data.time)
                if (!data.isFirstPage) hideName()
                else setName(data.sender)
            }

        /**
         * View needs to release resources when its recycled
         */
        override fun unbindView(item: ChatMessageItem) = with(binding.kaleyraChatMessage) {
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