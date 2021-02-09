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

package com.bandyer.sdk_design.utils

import android.os.CountDownTimer
import android.view.View
import com.bandyer.android_common.FieldProperty

/**
 * Defines a view that will be shown after certain time if the input predicates matches.
 * @param T generic view type
 */
interface ConditionalVisibilityModifier<T> where T : View {

    /**
     * Starts the countdown timer that will trigger at the end the predicate evaluation to show the view
     * @param shouldShowPredicate Function0<Boolean> the predicate used to eventually display the view if returns true
     * @param countDownDuration Long duration of the countdown till the predicate evaluation
     */
    fun show(shouldShowPredicate: (() -> Boolean), countDownDuration: Long) {
        if (countDownDuration == 0L) {
            onTimerFinished(shouldShowPredicate)
            return
        }
        if (countDownTimer != null) return
        countDownTimer = object : CountDownTimer(countDownDuration, countDownDuration) {
            override fun onFinish() = onTimerFinished(shouldShowPredicate)
            override fun onTick(millisUntilFinished: Long) = Unit
        }.apply {
            this.start()
        }
    }

    /**
     * Cancel the countdown timer and hides the view
     */
    fun hide() {
        countDownTimer?.cancel()
        countDownTimer = null
        (this@ConditionalVisibilityModifier as View).visibility = View.GONE
    }

    private fun onTimerFinished(shouldShowPredicate: (() -> Boolean)) {
        if (!shouldShowPredicate.invoke()) return
        (this@ConditionalVisibilityModifier as View).visibility = View.VISIBLE
    }
}

/**
 * Property specifying the conditional visibility modifier countdown timer
 */
var <T> ConditionalVisibilityModifier<T>.countDownTimer: CountDownTimer? where T : View by FieldProperty { null }