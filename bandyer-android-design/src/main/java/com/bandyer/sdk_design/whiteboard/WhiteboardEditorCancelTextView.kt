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

package com.bandyer.sdk_design.whiteboard

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.google.android.material.textview.MaterialTextView

/**
 * Whiteboard editor cancel text view
 *
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class WhiteboardEditorCancelTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialTextView(context, attrs, defStyleAttr) {

    /**
     * The state of cancel text view
     */
    var state: BandyerCancelActionButtonState? = BandyerCancelActionButtonState.DISMISS
        set(value) {
        field = value
        updateText(field)
    }

    private fun updateText(state: BandyerCancelActionButtonState?) {
        text = when(state) {
            BandyerCancelActionButtonState.CANCEL -> resources.getString(R.string.bandyer_action_cancel)
            BandyerCancelActionButtonState.DISCARD_CHANGES -> resources.getString(R.string.bandyer_action_discard_changes)
            else -> resources.getString(R.string.bandyer_action_dismiss)
        }
    }

}