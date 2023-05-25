package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingComponent

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallScreenContent(
    callState: CallStateUi,
    maxWidth: Dp,
    onBackPressed: () -> Unit,
    onStreamFullscreenClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        AnimatedContent(
            targetState = callState,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
            }
        ) { target ->
            when(target) {
                CallStateUi.Ringing -> RingingComponent(onBackPressed = onBackPressed)
                CallStateUi.Dialing -> DialingComponent(onBackPressed = onBackPressed)
                else -> CallComponent(maxWidth = maxWidth, onBackPressed = onBackPressed, onStreamFullscreenClick = onStreamFullscreenClick)
            }
        }
    }
}