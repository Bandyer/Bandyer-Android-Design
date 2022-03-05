/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.whiteboard

import android.content.Context
import android.util.AttributeSet
import com.kaleyra.collaboration_suite_phone_ui.R
import com.google.android.material.button.MaterialButton

/**
 * Whiteboard editor cancel button
 *
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class WhiteboardEditorCancelButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialButton(context, attrs, defStyleAttr) {

    /**
     * @suppress
     */
    companion object {
        private val STATE_DISMISS = intArrayOf(R.attr.kaleyra_state_dismiss)
        private val STATE_DISCARD_CHANGES = intArrayOf(R.attr.kaleyra_state_discard_changes)
        private val STATE_CANCEL = intArrayOf(R.attr.kaleyra_state_cancel)
    }

    /**
     * The state of cancel image button
     */
    var state: KaleyraCancelActionButtonState? = KaleyraCancelActionButtonState.DISMISS
        set(value) {
            field = value
            setContentDescription(value)
            refreshDrawableState()
        }

    /**
     * @suppress
     */
    override fun onCreateDrawableState(extraSpace: Int): IntArray {
        val drawableState = super.onCreateDrawableState(extraSpace + 3)

        state ?: return drawableState

        when (state) {
            KaleyraCancelActionButtonState.DISMISS -> mergeDrawableStates(drawableState, STATE_DISMISS)
            KaleyraCancelActionButtonState.DISCARD_CHANGES -> mergeDrawableStates(drawableState, STATE_DISCARD_CHANGES)
            KaleyraCancelActionButtonState.CANCEL -> mergeDrawableStates(drawableState, STATE_CANCEL)
        }

        return drawableState
    }

    private fun setContentDescription(value: KaleyraCancelActionButtonState?) {
        value ?: return
        contentDescription = when (value) {
            KaleyraCancelActionButtonState.DISMISS -> resources.getString(R.string.kaleyra_action_dismiss)
            KaleyraCancelActionButtonState.DISCARD_CHANGES -> resources.getString(R.string.kaleyra_action_discard_changes)
            else -> resources.getString(R.string.kaleyra_action_cancel)
        }
    }

}

