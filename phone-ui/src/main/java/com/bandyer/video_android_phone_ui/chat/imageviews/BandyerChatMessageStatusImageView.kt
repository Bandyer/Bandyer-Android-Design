/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bandyer.video_android_phone_ui.chat.imageviews

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.bandyer.video_android_phone_ui.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * Bandyer Chat MessageStatus ImageView used to display an image representing a pending, sent or seen message
 * @author kristiyan
 */
class BandyerChatMessageStatusImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * The state of the chat message status image view
     */
    var state: State? = State.PENDING
        set(value) {
            field = value
            setContentDescription(value)
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)
        val state = state ?: return drawableState
        return mergeDrawableStates(drawableState, state.value)
    }

    private fun setContentDescription(value: State?) {
        value ?: return
        contentDescription = when (value) {
            State.PENDING -> resources.getString(R.string.bandyer_chat_msg_status_pending)
            State.SEEN -> resources.getString(R.string.bandyer_chat_msg_status_seen)
            else -> resources.getString(R.string.bandyer_chat_msg_status_sent)
        }
    }

    /**
     * States of an BandyerChatMessageStatusImageView
     * @param value drawable resource of the state
     * @constructor
     */
    enum class State(val value: IntArray) {
        /**
         * P e n d i n g
         *
         * @constructor Create empty P e n d i n g
         */
        PENDING(intArrayOf(R.attr.bandyer_state_pending)),

        /**
         * S e e n
         *
         * @constructor Create empty S e e n
         */
        SEEN(intArrayOf(R.attr.bandyer_state_seen)),

        /**
         * S e n t
         *
         * @constructor Create empty S e n t
         */
        SENT(intArrayOf(R.attr.bandyer_state_sent))
    }
}