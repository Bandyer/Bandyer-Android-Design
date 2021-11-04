package com.bandyer.video_android_glass_ui.participants

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_core_ui.utils.Iso8601
import com.bandyer.video_android_glass_ui.common.UserState
import com.google.android.material.textview.MaterialTextView

/**
 * A TextView defining the user online state
 *
 * @constructor
 */
class ParticipantStateTextView @JvmOverloads constructor(
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