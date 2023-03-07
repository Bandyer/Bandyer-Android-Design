/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.chat.adapter_items.message.abstract_message

import android.text.format.DateUtils
import android.view.View
import com.kaleyra.collaboration_suite_phone_ui.chat.imageviews.KaleyraChatMessageStatusImageView
import com.kaleyra.collaboration_suite_phone_ui.chat.layout.KaleyraBaseChatMessageLayout
import com.kaleyra.collaboration_suite_core_ui.utils.KotlinConstraintSet.Companion.changeConstraints
import com.google.android.material.card.MaterialCardView
import com.mikepenz.fastadapter.FastAdapter

/**
 * Base Chat Message View Holder
 * @property F Layout to use for the message view
 * @property T Type of Message Item to display
 * @constructor
 */
@Suppress("UNCHECKED_CAST")
abstract class KaleyraBaseChatMessageViewHolder<F, T> constructor(view: View) : FastAdapter.ViewHolder<T>(view) where T : KaleyraBaseChatMessageItem<*>, F : KaleyraBaseChatMessageLayout {

    private var messageLayout: F = view as F
    private var messageContent: MaterialCardView = messageLayout.dataViewContainer as MaterialCardView

    /**
     * @suppress
     */
    final override fun bindView(item: T, payloads: List<Any>) {
        bind(item, messageLayout, payloads)

        val pending = item.data.pending
        val seen = item.data.seen.invoke()
        val sent = item.data.sent

        when {
            seen -> messageLayout.statusView?.state = KaleyraChatMessageStatusImageView.State.SEEN
            sent -> messageLayout.statusView?.state = KaleyraChatMessageStatusImageView.State.SENT
            pending -> messageLayout.statusView?.state = KaleyraChatMessageStatusImageView.State.PENDING
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
    abstract fun bind(item: T, itemView: F, payloads: List<Any>)

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