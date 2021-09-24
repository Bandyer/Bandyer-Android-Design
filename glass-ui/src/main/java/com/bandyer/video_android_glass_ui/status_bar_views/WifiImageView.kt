package com.bandyer.video_android_glass_ui.status_bar_views

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the state of the wifi signal
 */
internal class WifiImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * The state of the ImageView. It changes the drawable state
     */
    var state: State? = State.DISABLED
        set(value) {
            field = value
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 4)
        val state = state ?: return drawableState
        return mergeDrawableStates(drawableState, state.value)
    }

    /**
     * Enum representing wifi state
     *
     * @param value state drawable resource
     * @constructor
     */
    enum class State(val value: IntArray) {

        /**
         * d i s a b l e d
         */
        DISABLED(intArrayOf(R.attr.bandyer_state_wifi_disabled)),

        /**
         * l o w
         */
        LOW(intArrayOf(R.attr.bandyer_state_wifi_low)),

        /**
         * m o d e r a t e
         */
        MODERATE(intArrayOf(R.attr.bandyer_state_wifi_moderate)),

        /**
         * f u l l
         */
        FULL(intArrayOf(R.attr.bandyer_state_wifi_full)),
    }
}