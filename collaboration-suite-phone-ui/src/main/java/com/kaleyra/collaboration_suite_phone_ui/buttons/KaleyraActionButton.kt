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
import android.view.LayoutInflater
import android.widget.LinearLayout


import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraActionButtonBinding
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * Kaleyra Action Button which is a composite of a button and a textView
 * @author kristiyan
 */
open class KaleyraActionButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle) : LinearLayout(context, attrs, defStyleAttr) {

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

    private val binding: KaleyraActionButtonBinding by lazy { KaleyraActionButtonBinding.inflate(LayoutInflater.from(context), this) }

    init {
        button = binding.kaleyraButtonView
        label = binding.kaleyraLabelView
        button!!.tag = BOTTOM_SHEET_TAG
    }

    /**
     * @suppress
     */
    override fun setOnClickListener(l: OnClickListener?) {
        if (!setClickListenerOnChildren || isClickable) {
            super.setOnClickListener(l)
            return
        }
        if (!label!!.hasOnClickListeners()) label!!.setOnClickListener(l)
        if (!button!!.hasOnClickListeners()) button!!.setOnClickListener(l)
    }

    /**
     * @suppress
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        button?.isEnabled = enabled
        label?.isEnabled = enabled
    }

    /**
     * By default only the children will listen for click events
     */
    var setClickListenerOnChildren = true
}