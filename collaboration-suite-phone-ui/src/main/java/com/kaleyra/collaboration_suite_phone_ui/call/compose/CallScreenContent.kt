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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.view.RingingComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoUi
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallScreenContent(
    streams: ImmutableList<StreamUi>,
    callInfo: CallInfoUi,
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
        val targetContent by remember(callInfo) {
            derivedStateOf {
                when (callInfo.callState) {
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
                    DialingContent(
                        stream = streams.getOrNull(0),
                        callInfo = callInfo,
                        groupCall = groupCall,
                        onBackPressed = onBackPressed
                    )
                }
                2 -> {
                    val callContentState = rememberCallContentState(
                        streams = streams,
                        callInfo = callInfo,
                        configuration = LocalConfiguration.current,
                        maxWidth = maxWidth
                    )

                    LaunchedEffect(callInfo) {
                        snapshotFlow { callInfo.callState }
                            .onEach {
                                if (it is CallState.Reconnecting || it is CallState.Connecting || it is CallState.Disconnected) callContentState.showCallInfo()
                                else callContentState.hideCallInfo()
                            }
                            .launchIn(this)
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