package com.bandyer.video_android_glass_ui.participants

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_core_ui.utils.Iso8601
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

    private var state: State = State.LAST_SEEN

    /**
     * Define the contact online state
     *
     * @param state The user state
     * @param lastSeenTime The last time the user was online. Needed only if the state value is State.LastSeen.
     */
    fun setContactState(state: State, lastSeenTime: Long = 0) {
        this.state = state
        text = when (state) {
            State.ONLINE    -> resources.getString(R.string.bandyer_glass_online)
            State.INVITED   -> resources.getString(R.string.bandyer_glass_invited)
            State.LAST_SEEN -> resources.getString(
                R.string.bandyer_glass_last_seen_pattern,
                Iso8601.parseTimestamp(context, lastSeenTime)
            )
        }
    }

    /**
     * The contact user online state
     */
    enum class State {
        /**
         * o n l i n e
         */
        ONLINE,

        /**
         * i n v i t e d
         */
        INVITED,

        /**
         * l a s t_s e e n
         */
        LAST_SEEN
    }
}