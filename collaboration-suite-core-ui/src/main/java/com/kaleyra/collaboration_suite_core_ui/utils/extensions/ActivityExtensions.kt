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

package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.view.WindowManager

/**
 * ActivityExtensions
 */
object ActivityExtensions {

    /**
     * Turn and keep the screen on
     *
     * @receiver Activity
     */
    @Suppress("DEPRECATION")
    fun Activity.turnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Remove the turn and keep the screen on setting
     *
     * @receiver Activity
     */
    @Suppress("DEPRECATION")
    fun Activity.turnScreenOff() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) setTurnScreenOn(false)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    fun Activity.moveToFront() =
        startActivity(intent.apply { flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT })
}