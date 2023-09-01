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

package com.kaleyra.collaboration_suite_glass_ui.participants

import android.content.Context
import android.util.AttributeSet
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite_core_ui.utils.TimestampUtils

/**
 * A TextView defining the user online state
 *
 * @constructor
 */
internal class ParticipantStateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    private var state: ChatParticipant.State = ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never)

    /**
     * Define the user online state
     *
     * @param state The user state
     */
    fun setUserState(state: ChatParticipant.State) {
        this.state = state
        text = when (state) {
            is ChatParticipant.State.Invited   -> resources.getString(R.string.kaleyra_glass_invited)
            is ChatParticipant.State.Joined.Online    -> resources.getString(R.string.kaleyra_glass_online)
            is ChatParticipant.State.Joined.Offline -> {
                val lastLogin = state.lastLogin
                if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At) resources.getString(
                    R.string.kaleyra_glass_last_seen_pattern,
                    TimestampUtils.parseTimestamp(context, lastLogin.date.time)
                )
                else resources.getString(R.string.kaleyra_glass_last_seen_never)
            }
        }
    }
}