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

package com.kaleyra.collaboration_suite_phone_ui.whiteboard

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraActionButtonCancelBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * Kaleyra cancel action button
 *
 * @constructor
 *
 * @param context
 * @param attrs
 * @param defStyleAttr
 */
class KaleyraCancelActionButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle)
    : LinearLayout(context, attrs, defStyleAttr) {

    /**
     * KaleyraAction Button companion object.
     */
    companion object {
        /**
         * Constant representing if the action button will be a child of a bottom sheet
         */
        const val BOTTOM_SHEET_TAG = "bottom_sheet"
    }

    /**
     * Base Button component
     */
    var button: MaterialButton? = null

    /**
     * Base Label component
     */
    var label: MaterialTextView? = null

    /**
     * The state of cancel action button
     */
    var state: KaleyraCancelActionButtonState? = KaleyraCancelActionButtonState.CANCEL
        set(value) {
            field = value
            (label as? WhiteboardEditorCancelTextView)?.state = field
            (button as? WhiteboardEditorCancelButton)?.state = field
        }


    private val binding: KaleyraActionButtonCancelBinding by lazy { KaleyraActionButtonCancelBinding.inflate(LayoutInflater.from(context), this) }

    init {
        button = binding.kaleyraButtonView
        label = binding.kaleyraLabelView

        button?.tag = BOTTOM_SHEET_TAG
    }

    /**
     * @suppress
     */
    override fun setOnClickListener(l: OnClickListener?) {
        if (!setClickListenerOnChildren) {
            super.setOnClickListener(l)
            return
        }
        if (!label!!.hasOnClickListeners()) label!!.setOnClickListener(l)
        if (!button!!.hasOnClickListeners()) button!!.setOnClickListener(l)
    }

    /**
     * By default only the children will listen for click events
     */
    var setClickListenerOnChildren = true

}

/**
 * States of an KaleyraCancelActionButton
 */
enum class KaleyraCancelActionButtonState {
    /**
     * D i s m i s s
     *
     * @constructor Create empty D i s m i s s
     */
    DISMISS,

    /**
     * D i s c a r d_c h a n g e s
     *
     * @constructor Create empty D i s c a r d_c h a n g e s
     */
    DISCARD_CHANGES,

    /**
     * C a n c e l
     *
     * @constructor Create empty C a n c e l
     */
    CANCEL
}