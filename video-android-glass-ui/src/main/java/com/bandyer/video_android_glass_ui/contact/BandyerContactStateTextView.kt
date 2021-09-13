package com.bandyer.video_android_glass_ui.contact

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.utils.Iso8601
import com.google.android.material.textview.MaterialTextView

/**
 * A TextView defining the user online status
 *
 * @constructor
 */
class BandyerContactStateTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialTextView(context, attrs, defStyleAttr) {

    private var state: State = State.LAST_SEEN

    /**
     * Define the contact online status
     *
     * @param state The user status
     * @param lastSeenTime The last time the user was online. Needed only if the state value is State.LastSeen.
     */
    fun setContactState(state: State, lastSeenTime: Long = 0) {
        this.state = state
        text = when (state) {
            State.ONLINE    -> resources.getString(R.string.bandyer_smartglass_online)
            State.INVITED   -> resources.getString(R.string.bandyer_smartglass_invited)
            State.LAST_SEEN -> resources.getString(
                R.string.bandyer_smartglass_last_seen_pattern,
                Iso8601.parseTimestamp(context, lastSeenTime)
            )
        }
    }

    enum class State {
        ONLINE,
        INVITED,
        LAST_SEEN
    }
}