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

package com.kaleyra.collaboration_suite_glass_ui.status_bar_views

import android.content.Context
import android.util.AttributeSet
import com.kaleyra.collaboration_suite_glass_ui.R
import com.google.android.material.imageview.ShapeableImageView

/**
 * This ImageView defines the state of the cell signal
 */
internal class CellImageView @JvmOverloads constructor(
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