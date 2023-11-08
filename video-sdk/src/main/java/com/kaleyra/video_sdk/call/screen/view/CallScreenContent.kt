package com.kaleyra.video_sdk.call.screen.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kaleyra.video_sdk.call.dialing.DialingComponent
import com.kaleyra.video_sdk.call.ringing.RingingComponent
import com.kaleyra.video_sdk.call.screen.model.CallStateUi

@Composable
internal fun CallScreenContent(
    callState: CallStateUi,
    maxWidth: Dp,
    onBackPressed: () -> Unit,
    onStreamFullscreenClick: (String?) -> Unit,
    shouldShowUserMessages: Boolean = true,
    isDarkTheme: Boolean = true,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        val targetState by remember(callState) {
            derivedStateOf {
                when (callState) {
                    is CallStateUi.Ringing -> 0
                    CallStateUi.Dialing -> 1
                    else -> 2
                }
            }
        }
        when (targetState) {
            0 -> RingingComponent(onBackPressed = onBackPressed, isDarkTheme = isDarkTheme)
            1 -> DialingComponent(onBackPressed = onBackPressed)
            else -> CallComponent(
                shouldShowUserMessages = shouldShowUserMessages,
                maxWidth = maxWidth,
                onBackPressed = onBackPressed,
                onStreamFullscreenClick = onStreamFullscreenClick
            )
        }
    }
}