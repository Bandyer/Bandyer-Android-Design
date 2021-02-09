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

package com.bandyer.sdk_design.textviews

import android.content.Context
import android.util.AttributeSet
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.utils.ConditionalVisibilityModifier
import com.google.android.material.textview.MaterialTextView

/**
 * BandyerTextView that is representing call answer helper text message
 * @constructor
 */
class BandyerAnswerCallHelperText @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_answerHelperTextStyle) :
        MaterialTextView(context, attrs, defStyleAttr), ConditionalVisibilityModifier<MaterialTextView> {

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        hide()
    }
}