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

package com.kaleyra.collaboration_suite_core_ui.widget

import android.os.CountDownTimer

/**
 * A HideableWidget is a widget which can be hidden automatically after a preset of millis.
 * It also exposes a function to toggle between states
 *
 * @property hidingTimer define a CountDownTimer to use for the hiding actions
 *
 */
interface HideableWidget {

    var hidingTimer: CountDownTimer?

    /**
     * Milliseconds remaining until the end of the timer
     */
    var millisUntilTimerFinish: Long

    /**
     * Triggers onHidingTimerFinishedCallback after input milliseconds.
     * @param afterMillis hidingTimer duration in milliseconds.
     */
    fun autoHide(afterMillis: Long) {
        when {
            hidingTimer != null -> hidingTimer!!.cancel()
            else -> hidingTimer = object : CountDownTimer(afterMillis, 100) {

                override fun onFinish() {
                    onHidingTimerFinished()
                }

                override fun onTick(millisUntilFinished: Long) {
                    millisUntilTimerFinish = millisUntilFinished
                }
            }
        }
        hidingTimer!!.start()
    }

    /**
     * Disable auto-hide hidingTimer even if has already started.
     */
    fun disableAutoHide() {
        hidingTimer?.cancel()
    }

    /**
     * Called when hidingTimer has finished.
     */
    fun onHidingTimerFinished()
}