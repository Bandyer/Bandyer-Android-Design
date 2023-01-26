package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.*
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val CallContentTag = "CallContentTag"
private val StatusBarPaddingModifier = Modifier.statusBarsPadding()

@Composable
internal fun rememberCallComponentState(
    callUiState: CallUiState,
    configuration: Configuration,
    maxWidth: Dp
) = remember(callUiState, configuration, maxWidth) {
    CallComponentState(
        callUiState = callUiState,
        configuration = configuration,
        maxWidth = maxWidth
    )
}

internal class CallComponentState(
    val callUiState: CallUiState,
    private val configuration: Configuration,
    private val maxWidth: Dp,
    // Parameters added for testing purpose
    showCallInfo: Boolean = false,
    fullscreenStream: StreamUi? = null
) {

    private val isDevicePortrait: Boolean
        get() = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val columns by derivedStateOf {
        val featuredStreams = callUiState.featuredStreams.value
        when {
            // Smartphone portrait
            isDevicePortrait && maxWidth < 600.dp -> 1
            // Tablet portrait
            isDevicePortrait && featuredStreams.size > 2 -> 2
            // Landscape
            featuredStreams.size > 1 -> 2
            else -> 1
        }
    }

    var showCallInfo by mutableStateOf(showCallInfo)
        private set

    var fullscreenStream by mutableStateOf(fullscreenStream)
        private set

    val isFullscreenStreamRemoved by derivedStateOf {
        val featuredStream = callUiState.featuredStreams.value
        fullscreenStream != null && !featuredStream.contains(fullscreenStream)
    }

    fun enterFullscreenMode(stream: StreamUi) {
        fullscreenStream = stream
    }

    fun exitFullscreenMode() {
        fullscreenStream = null
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallComponent(
    state: CallComponentState,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    var callInfoWidgetHeight by remember { mutableStateOf(0) }
    val streamHeaderOffset by animateIntAsState(targetValue = if (state.showCallInfo) callInfoWidgetHeight else 0)

    LaunchedEffect(state) {
        snapshotFlow { state.isFullscreenStreamRemoved }
            .filter { it }
            .onEach { state.exitFullscreenMode() }
            .launchIn(this)
    }

    var resetCountDown by remember { mutableStateOf(false) }
    val countDown by rememberCountdownTimerState(initialMillis = 5000L, resetFlag = resetCountDown)
    val streamsHeaderAlpha by animateFloatAsState(
        targetValue = if (countDown > 0L) 1f else 0f,
        animationSpec = tween()
    )

    Crossfade(
        targetState = state.fullscreenStream,
        modifier = modifier.testTag(CallContentTag)
    ) { target ->
        if (target == null) {
            // TODO use ReusableContent?
            AdaptiveGrid(
                columns = state.columns,
                modifier = StatusBarPaddingModifier
            ) {
                val streams = state.callUiState.featuredStreams.value
                repeat(streams.count()) { index ->
                    val stream = streams[index]
                    FeaturedStream(
                        stream = stream,
                        isFullscreen = false,
                        onBackPressed = if (index == 0 && !state.showCallInfo) onBackPressed else null,
                        onFullscreenClick = { state.enterFullscreenMode(stream) },
                        modifier = Modifier.streamClickable {
                            resetCountDown = !resetCountDown
                        },
                        headerModifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = if (index < state.columns) streamHeaderOffset else 0
                                )
                            }
                            .graphicsLayer { alpha = streamsHeaderAlpha }
                    )
                }
            }
        } else {
            FeaturedStream(
                stream = target,
                isFullscreen = true,
                onBackPressed = if (!state.showCallInfo) state::exitFullscreenMode else null,
                onFullscreenClick = state::exitFullscreenMode,
                headerModifier = Modifier
                    .offset { IntOffset(x = 0, y = streamHeaderOffset) }
                    .graphicsLayer { alpha = streamsHeaderAlpha },
                modifier = StatusBarPaddingModifier.streamClickable {
                    resetCountDown = !resetCountDown
                }
            )
        }
    }

    with(state) {
        AnimatedVisibility(
            visible = callUiState.shouldShowCallInfo(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CallInfoWidget(
                onBackPressed = if (fullscreenStream != null) ::exitFullscreenMode else onBackPressed,
                title = titleFor(callUiState.callState),
                subtitle = subtitleFor(
                    callState = callUiState.callState,
                    groupCall = callUiState.isGroupCall
                ),
                watermarkInfo = callUiState.watermarkInfo,
                recording = callUiState.isRecording,
                modifier = StatusBarPaddingModifier.onGloballyPositioned {
                    callInfoWidgetHeight = it.size.height
                }
            )
        }
    }
}

private fun CallUiState.shouldShowCallInfo(): Boolean =
    callState is CallState.Reconnecting || callState is CallState.Connecting || callState is CallState.Disconnected

@Composable
private fun titleFor(callState: CallState) =
    when(callState) {
        CallState.Connecting, CallState.Reconnecting -> stringResource(id = R.string.kaleyra_call_status_connecting)
        is CallState.Disconnected -> stringResource(id = R.string.kaleyra_call_status_ended)
        else -> ""
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun subtitleFor(callState: CallState, groupCall: Boolean) =
    when(callState) {
        CallState.Disconnected.Ended.AnsweredOnAnotherDevice -> stringResource(id = R.string.kaleyra_call_status_answered_on_other_device)
        CallState.Disconnected.Ended.Declined -> pluralStringResource(id = R.plurals.kaleyra_call_status_declined, count = if (groupCall) 2 else 1)
        CallState.Disconnected.Ended.Timeout -> pluralStringResource(id = R.plurals.kaleyra_call_status_no_answer, count =  if (groupCall) 2 else 1)
        else -> null
    }

private fun Modifier.streamClickable(onClick: () -> Unit): Modifier =
    composed {
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    }


@Preview
@Composable
fun CallContentPreview() {
    KaleyraTheme {
//        CallContent(
//            state = rememberCallComponentState(
//                streams = ImmutableList(listOf(streamUiMock, streamUiMock)),
//                callState = CallState.Connected,
//                groupCall = true,
//                configuration = LocalConfiguration.current,
//                maxWidth = 400.dp
//            ),
//            onBackPressed = { }
//        )
    }
}