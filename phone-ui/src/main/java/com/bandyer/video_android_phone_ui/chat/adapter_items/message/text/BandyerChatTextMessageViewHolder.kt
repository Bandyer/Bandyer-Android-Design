/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.chat.adapter_items.message.text

import android.view.View
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getScreenSize
import com.bandyer.video_android_phone_ui.chat.adapter_items.message.abstract_message.BandyerBaseChatMessageViewHolder
import com.bandyer.video_android_phone_ui.chat.layout.BandyerChatTextMessageLayout

/**
 * Bandyer Chat TextMessage ViewHolder
 * @constructor
 */
class BandyerChatTextMessageViewHolder(view: View) : BandyerBaseChatMessageViewHolder<BandyerChatTextMessageLayout, BandyerChatTextMessageItem>(view) {

    /**
     * @suppress
     */
    override fun bind(item: BandyerChatTextMessageItem, itemView: BandyerChatTextMessageLayout, payloads: List<Any>) {
        itemView.messageTextView?.text = item.data.message
        itemView.messageTextView?.maxWidth = itemView.context.getScreenSize().x / 2
    }

    /**
     * @suppress
     */
    override fun unbind(item: BandyerChatTextMessageItem, itemView: BandyerChatTextMessageLayout) {}
}