package com.bandyer.sdk_design.new_smartglass.status_bar_views

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the state of the cell signal
 */
class BandyerCellImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ShapeableImageView(context, attrs, defStyleAttr) {

    /**
     * The state of the ImageView. It changes the drawable state
     */
    var state: State? = State.LOW
        set(value) {
            field = value
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 5)
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
         * m i s s i n g
         */
        MISSING(intArrayOf(R.attr.bandyer_state_cell_missing)),

        /**
         * l o w
         */
        LOW(intArrayOf(R.attr.bandyer_state_cell_low)),

        /**
         * m o d e r a t e
         */
        MODERATE(intArrayOf(R.attr.bandyer_state_cell_moderate)),

        /**
         * g o o d
         */
        GOOD(intArrayOf(R.attr.bandyer_state_cell_good)),

        /**
         * f u l l
         */
        FULL(intArrayOf(R.attr.bandyer_state_cell_full)),
    }
}