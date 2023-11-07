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

package com.kaleyra.video_common_ui.textview

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.View
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.video_common_ui.widget.HideableWidget

/**
 * Auto hide Kaleyra text view
 * @constructor
 */
class KaleyraAutoHideTextView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        MaterialTextView(context, attrs, defStyleAttr), HideableWidget {

    override var hidingTimer: CountDownTimer? = null

    override var millisUntilTimerFinish: Long = 0

    override fun onHidingTimerFinished() {
        visibility = View.GONE
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (millisUntilTimerFinish == 0L) return
        autoHide(millisUntilTimerFinish)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        disableAutoHide()
    }
}