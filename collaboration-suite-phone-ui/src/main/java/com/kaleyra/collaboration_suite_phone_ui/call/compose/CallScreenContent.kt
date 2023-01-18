package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.dialing.DialingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.ringing.RingingComponent
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallScreenContent(
    streams: ImmutableList<StreamUi>,
    callState: CallState,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit,
    onAnswerClick: () -> Unit,
    onDeclineClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        val targetContent by remember(callState) {
            derivedStateOf {
                when (callState) {
                    CallState.Ringing -> 0
                    CallState.Dialing -> 1
                    else -> 2
                }
            }
        }

        AnimatedContent(targetState = targetContent) { target ->
            when(target) {
                0 -> RingingComponent(onBackPressed = onBackPressed)
                1 -> {
                    DialingComponent(onBackPressed = onBackPressed)
                }
                2 -> {
                    val callContentState = rememberCallContentState(
                        streams = streams,
                        callState = callState,
                        groupCall = groupCall,
                        configuration = LocalConfiguration.current,
                        maxWidth = maxWidth
                    )

                    LaunchedEffect(callState) {
                        if (callState is CallState.Reconnecting || callState is CallState.Connecting || callState is CallState.Disconnected) callContentState.showCallInfo()
                        else callContentState.hideCallInfo()
                    }

                    CallContent(
                        state = callContentState,
                        onBackPressed = onBackPressed
                    )
                }
                else -> Unit
            }
        }
    }
}