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

package com.kaleyra.collaboration_suite_phone_ui.buttons

import android.content.Context
import android.util.AttributeSet
import com.kaleyra.collaboration_suite_phone_ui.utils.ToggleableVisibilityInterface
import com.kaleyra.collaboration_suite_phone_ui.utils.VisibilityToggle
import com.google.android.material.button.MaterialButton

/**
 * Hideable Kaleyra button
 * @property visibilityToggle VisibilityToggle
 * @constructor
 */
class KaleyraHideableButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        MaterialButton(context, attrs, defStyleAttr), ToggleableVisibilityInterface {

    private val visibilityToggle = VisibilityToggle(this)

    override fun toggleVisibility(show: Boolean, animationEndCallback: (shown: Boolean) -> Unit) {
        visibilityToggle.toggleVisibility(show, animationEndCallback)
    }

    override fun cancelTimer() {
        visibilityToggle.cancelTimer()
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelTimer()
    }
}