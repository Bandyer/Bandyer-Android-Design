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

package com.bandyer.sdk_design.chat.adapter_items.message.abstract_message

import androidx.constraintlayout.widget.ConstraintSet
import android.text.format.DateUtils
import android.view.View
import com.bandyer.sdk_design.chat.imageviews.BandyerChatMessageStatusImageView
import com.bandyer.sdk_design.chat.layout.BandyerBaseChatMessageLayout
import com.bandyer.sdk_design.utils.KotlinConstraintSet.Companion.changeConstraints
import com.google.android.material.card.MaterialCardView
import com.mikepenz.fastadapter.FastAdapter

/**
 * Base Chat Message View Holder
 * @property F Layout to use for the message view
 * @property T Type of Message Item to display
 * @constructor
 */
@Suppress("UNCHECKED_CAST")
abstract class BandyerBaseChatMessageViewHolder<F, T> constructor(view: View) : FastAdapter.ViewHolder<T>(view) where T : BandyerBaseChatMessageItem<*>, F : BandyerBaseChatMessageLayout {

    private var messageLayout: F = view as F
    private var messageContent: MaterialCardView = messageLayout.dataViewContainer as MaterialCardView

    /**
     * @suppress
     */
    final override fun bindView(item: T, payloads: MutableList<Any>) {
        bind(item, messageLayout, payloads)

        val pending = item.data.pending
        val seen = item.data.seen.invoke()
        val sent = item.data.sent

        when {
            seen -> messageLayout.statusView?.state = BandyerChatMessageStatusImageView.State.SEEN
            sent -> messageLayout.statusView?.state = BandyerChatMessageStatusImageView.State.SENT
            pending -> messageLayout.statusView?.state = BandyerChatMessageStatusImageView.State.PENDING
        }

        messageLayout.showStatus(item.data.mine && (pending || seen || sent))

        messageLayout.timestampTextView!!.text = DateUtils.getRelativeTimeSpanString(item.data.timestamp)

        messageLayout.changeConstraints {
            if (item.data.mine) messageContent.id endToEndOf messageLayout.id
            else messageContent.id startToStartOf messageLayout.id
        }
    }

    /**
     * On Bind of the message
     * @param item T  Message Item
     * @param itemView F Message Item
     * @param payloads MutableList<Any> of payload
     */
    abstract fun bind(item: T, itemView: F, payloads: MutableList<Any>)

    /**
     * On unBind of the message
     * @param item T Message Item
     * @param itemView F Message Item
     */
    abstract fun unbind(item: T, itemView: F)

    /**
     * @suppress
     */
    final override fun unbindView(item: T) {
        unbind(item, messageLayout)
        messageLayout.changeConstraints { clear(messageContent.id) }
        messageLayout.showStatus(false)
    }
}