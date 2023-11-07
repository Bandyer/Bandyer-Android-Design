package com.kaleyra.video_sdk.call.callactions.utility

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember

@Composable
internal fun mapToRotationState(orientation: State<Int>): Float {
    return remember {
        derivedStateOf {
            when (orientation.value) {
                90 -> -90f
                270 -> 90f
                else -> 0f
            }
        }
    }.value
}