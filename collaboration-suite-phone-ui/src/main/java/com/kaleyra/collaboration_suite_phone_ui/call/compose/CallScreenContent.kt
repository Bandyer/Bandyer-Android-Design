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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallScreenContent(
    callState: CallState,
    streams: ImmutableList<StreamUi>,
    callInfo: CallInfoUi,
    groupCall: Boolean = false,
    onBackPressed: () -> Unit,
    onAnswerClick: () -> Unit,
    onDeclineClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        AnimatedContent(targetState = callState) { target ->
            when(target) {
                CallState.Ringing -> {
                    RingingContent(
                        stream = streams.getOrNull(0),
                        callInfo = callInfo,
                        groupCall = groupCall,
                        onBackPressed = onBackPressed,
                        onAnswerClick = onAnswerClick,
                        onDeclineClick = onDeclineClick
                    )
                }
                CallState.Dialing -> {
                    DialingContent(
                        stream = streams.getOrNull(0),
                        callInfo = callInfo,
                        groupCall = groupCall,
                        onBackPressed = onBackPressed
                    )
                }
                CallState.InCall -> {
                    val inCallContentState = rememberInCallContentState(
                        streams = streams,
                        callInfo = callInfo,
                        configuration = LocalConfiguration.current,
                        maxWidth = maxWidth
                    )
                    InCallContent(
                        state = inCallContentState,
                        onBackPressed = onBackPressed
                    )
                }
            }
        }
    }
}