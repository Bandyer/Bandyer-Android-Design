package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingComponent

@Composable
internal fun CallScreenContent(
    callUiState: CallUiState,
    maxWidth: Dp,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        Crossfade(targetState = callUiState.callState) { target ->
            when(target) {
                CallState.Ringing -> RingingComponent(onBackPressed = onBackPressed)
                CallState.Dialing -> DialingComponent(onBackPressed = onBackPressed)
                else -> {
                    val callContentState = rememberCallComponentState(callUiState, LocalConfiguration.current, maxWidth)
                    CallComponent(state = callContentState, onBackPressed = onBackPressed)
                }
            }
        }
    }
}