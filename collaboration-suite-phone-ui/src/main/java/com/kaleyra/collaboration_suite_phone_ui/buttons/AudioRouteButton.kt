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

package com.kaleyra.collaboration_suite_phone_ui.buttons

import android.content.Context
import android.util.AttributeSet
import com.kaleyra.collaboration_suite_phone_ui.R
import com.google.android.material.button.MaterialButton

/**
 * It represents the audio route button. The icon changes for each state as defined in the drawable resource
 */
class AudioRouteButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialButton(context, attrs, defStyleAttr) {

    /**
     * The state of the audio route button
     */
    var state: State? = State.LOUDSPEAKER
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
     * States of an AudioRouteButton
     * @property value the drawable state
     * @constructor Create Audio route state
     */
    enum class State(val value: IntArray) {
        /**
         * B l u e t o o t h
         *
         * @constructor Create B l u e t o o t h
         */
        BLUETOOTH(intArrayOf(R.attr.kaleyra_state_bluetooth)),

        /**
         * L o u d s p e a k e r
         *
         * @constructor Create empty L o u d s p e a k e r
         */
        LOUDSPEAKER(intArrayOf(R.attr.kaleyra_state_loudspeaker)),

        /**
         * W i r e d_h e a d s e t
         *
         * @constructor Create empty W i r e d_h e a d s e t
         */
        WIRED_HEADSET(intArrayOf(R.attr.kaleyra_state_wired_headset)),

        /**
         * E a r p i e c e
         *
         * @constructor Create empty E a r p i e c e
         */
        EARPIECE(intArrayOf(R.attr.kaleyra_state_earpiece)),

        /**
         * M u t e d
         *
         * @constructor Create empty M u t e d
         */
        MUTED(intArrayOf(R.attr.kaleyra_state_muted))
    }
}

