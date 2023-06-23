package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.SystemClock
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun rememberCountdownTimerState(
    initialMillis: Long,
    step: Long = 1000,
    enable: Boolean = true
): MutableState<Long> {
    val timeLeft = remember(initialMillis) { mutableStateOf(initialMillis) }
    if (enable) {
        LaunchedEffect(initialMillis, step) {
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