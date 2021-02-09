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

package com.bandyer.sdk_design.chat.adapter_items.message.text

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.chat.adapter_items.message.abstract_message.BandyerBaseChatMessageItem

/**
 * Bandyer Chat TextMessage Item
 * @param chatTextMessage TextView for the message
 * @constructor
 * @author kristiyan
 */
class BandyerChatTextMessageItem(val chatTextMessage: BandyerChatTextMessage) : BandyerBaseChatMessageItem<BandyerChatTextMessage>(chatTextMessage) {

    /**
     * @suppress
     */
    override fun createView(ctx: Context, parent: ViewGroup?): View {
        val style = chatTextMessage.style ?: if(chatTextMessage.mine) R.style.BandyerSDKDesign_ChatMessage_LoggedUser else R.style.BandyerSDKDesign_ChatMessage_OtherUser
        return LayoutInflater.from(ContextThemeWrapper(ctx, style)).inflate(layoutRes, parent, false)
    }

    /**
     * @suppress
     */
    override fun getViewHolder(v: View) = BandyerChatTextMessageViewHolder(v)

    /**
     * @suppress
     */
    override fun getLayoutRes(): Int = R.layout.bandyer_chat_message_text

}
