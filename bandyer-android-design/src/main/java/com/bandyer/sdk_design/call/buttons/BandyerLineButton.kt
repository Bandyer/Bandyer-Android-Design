/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.sdk_design.call.buttons

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.bandyer.sdk_design.R
import com.google.android.material.button.MaterialButton

/**
 * Bandyer line button
 *
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class BandyerLineButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialButton(context, attrs, defStyleAttr) {

    /**
     * The state of line button
     */
    var state: State? = State.ANCHORED_DOT
        set(value) {
            if (field == value) return
            field = value
            refreshDrawableState()
            if (value == State.HIDDEN) {
                visibility = View.GONE
                isClickable = false
            }
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
     * States of an BandyerLineButton
     * @param value drawable resource of the state
     * @constructor
     */
    enum class State(val value: IntArray) {

        /**
         * H i d d e n
         *
         * @constructor Create empty H i d d e n
         */
        HIDDEN(intArrayOf(R.attr.bandyer_state_hidden)),

        /**
         * C o l l a p s e d
         *
         * @constructor Create empty C o l l a p s e d
         */
        COLLAPSED(intArrayOf(R.attr.bandyer_state_collapsed)),

        /**
         * E x p a n d e d
         *
         * @constructor Create empty E x p a n d e d
         */
        EXPANDED(intArrayOf(R.attr.bandyer_state_expanded)),

        /**
         * A n c h o r e d_d o t
         *
         * @constructor Create empty A n c h o r e d_d o t
         */
        ANCHORED_DOT(intArrayOf(R.attr.bandyer_state_anchored_dot)),

        /**
         * A n c h o r e d_l i n e
         *
         * @constructor Create empty A n c h o r e d_l i n e
         */
        ANCHORED_LINE(intArrayOf(R.attr.bandyer_state_anchored_line))
    }

}