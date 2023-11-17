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

package com.kaleyra.video_sdk.call.countdowntimer

import android.os.SystemClock
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun rememberCountdownTimerState(
    initialMillis: Long,
    step: Long = 1000,
    enable: Boolean = true,
    reset: Boolean = false
): State<Long> {
    val timeLeft = remember(initialMillis, enable) { mutableStateOf(initialMillis) }
    if (enable) {
        LaunchedEffect(initialMillis, step, reset) {
            val startTime = SystemClock.uptimeMillis()
            timeLeft.value = initialMillis
            while (isActive && timeLeft.value > 0) {
                val duration = (SystemClock.uptimeMillis() - startTime).coerceAtLeast(0)
                timeLeft.value = (initialMillis - duration).coerceAtLeast(0)
                delay(step.coerceAtMost(timeLeft.value))
            }
        }
    }
    return timeLeft
}