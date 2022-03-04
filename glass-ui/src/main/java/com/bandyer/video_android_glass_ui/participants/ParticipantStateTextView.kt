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

package com.bandyer.video_android_glass_ui.participants

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_core_ui.utils.Iso8601
import com.bandyer.video_android_glass_ui.model.internal.UserState
import com.google.android.material.textview.MaterialTextView

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

    private var state: UserState = UserState.Offline

    /**
     * Define the user online state
     *
     * @param state The user state
     * @param lastSeenTime The last time the user was online. Needed only if the state value is UserState.Offline.
     */
    fun setUserState(state: UserState, lastSeenTime: Long = 0) {
        this.state = state
        text = when (state) {
            UserState.Online    -> resources.getString(R.string.bandyer_glass_online)
            UserState.Offline -> resources.getString(
                R.string.bandyer_glass_last_seen_pattern,
                Iso8601.parseTimestamp(context, lastSeenTime)
            )
            else   -> resources.getString(R.string.bandyer_glass_invited)
        }
    }
}