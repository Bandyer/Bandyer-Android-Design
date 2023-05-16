package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.graphics.Rect
import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ConfigurationExtensions.isAtLeastMediumSizeWidth
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ConfigurationExtensions.isOrientationPortrait
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.*
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

const val CallComponentTag = "CallComponentTag"
private val StatusBarPaddingModifier = Modifier.statusBarsPadding()

@Composable
internal fun rememberCallComponentState(
    featuredStreamsCount: Int,
    configuration: Configuration,
    maxWidth: Dp,
) = remember(featuredStreamsCount, configuration, maxWidth) {
    CallComponentState(
        featuredStreamsCount = featuredStreamsCount,
        configuration = configuration,
        maxWidth = maxWidth
    )
}

internal class CallComponentState(
    private val featuredStreamsCount: Int,
    private val configuration: Configuration,
    private val maxWidth: Dp
) {

    val columns by derivedStateOf {
        when {
            // Smartphone portrait
            configuration.isOrientationPortrait() && !maxWidth.isAtLeastMediumSizeWidth() -> 1
            // Tablet portrait
            configuration.isOrientationPortrait() && featuredStreamsCount > 2 -> 2
            // Landscape
            featuredStreamsCount > 1 -> 2
            else -> 1
        }
    }
}

@Composable
internal fun CallComponent(
    viewModel: CallViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallViewModel.provideFactory(::requestConfiguration)
    ),
    maxWidth: Dp,
    onBackPressed: () -> Unit,
    onStreamFullscreenClick: (String?) -> Unit,
    onPipStreamPositioned: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val callUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val callComponentState = rememberCallComponentState(
        featuredStreamsCount = callUiState.featuredStreams.count(),
        configuration = LocalConfiguration.current,
        maxWidth = maxWidth
    )

    CallComponent(
        callUiState = callUiState,
        callComponentState = callComponentState,
        onBackPressed = onBackPressed,
        onStreamFullscreenClick = onStreamFullscreenClick,
        onPipStreamPositioned = onPipStreamPositioned,
        modifier = modifier
    )
}

@Composable
internal fun CallComponent(
    callUiState: CallUiState,
    callComponentState: CallComponentState,
    onBackPressed: () -> Unit,
    onStreamFullscreenClick: (String?) -> Unit,
    onPipStreamPositioned: (Rect) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowCallInfo by remember(callUiState) { callUiState.shouldShowCallInfo() }
    val shouldHideWatermark by remember(callUiState) { callUiState.shouldHideWatermark() }
    var callInfoWidgetHeight by remember { mutableStateOf(0) }
    val streamHeaderOffset by animateIntAsState(targetValue = if (shouldShowCallInfo) callInfoWidgetHeight else 0)

//    var resetCountDown by remember { mutableStateOf(false) }
//    val countDown by rememberCountdownTimerState(initialMillis = 5000L, resetFlag = resetCountDown)
//    val streamsHeaderAlpha by animateFloatAsState(
//        targetValue = if (countDown > 0L) 1f else 0f,
//        animationSpec = tween()
//    )
//

    Box(modifier.testTag(CallComponentTag)) {
        AdaptiveGrid(
            columns = if (callUiState.fullscreenStream == null) callComponentState.columns else 1,
            modifier = StatusBarPaddingModifier
        ) {
            val streams = callUiState.fullscreenStream?.let { listOf(it) } ?: callUiState.featuredStreams.value
            repeat(streams.count()) { index ->
                val stream = streams[index]
                key(stream.id) {
                    FeaturedStream(
                        stream = stream,
                        isFullscreen = callUiState.fullscreenStream != null,
                        onBackPressed = if (index == 0 && !shouldShowCallInfo) onBackPressed else null,
                        // TODO optimize recomposition
                        onFullscreenClick = remember(onStreamFullscreenClick, callUiState.fullscreenStream) {
                            { if (callUiState.fullscreenStream != null) onStreamFullscreenClick(null) else onStreamFullscreenClick(stream.id) }
                        },
                        onStreamPositioned = remember(onPipStreamPositioned) { { if (index == 0) onPipStreamPositioned(it) } },
                        modifier = remember(onStreamFullscreenClick) {
                            Modifier.pointerInput(onStreamFullscreenClick) {
                                detectTapGestures(onDoubleTap = { onStreamFullscreenClick(stream.id) })
                            }

//                        Modifier.streamClickable { resetCountDown = !resetCountDown }
                        },
                        headerModifier = remember(index, callComponentState, streamHeaderOffset) {
                            Modifier
                                .offset { IntOffset(x = 0, y = if (index < callComponentState.columns) streamHeaderOffset else 0) }
//                                    .graphicsLayer { alpha = streamsHeaderAlpha }
                        }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = shouldShowCallInfo,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CallInfoWidget(
                onBackPressed = onBackPressed,
                title = titleFor(callUiState.callState),
                subtitle = subtitleFor(
                    callState = callUiState.callState,
                    groupCall = callUiState.isGroupCall
                ),
                watermarkInfo = if (!shouldHideWatermark) callUiState.watermarkInfo else null,
                recording = callUiState.isRecording,
                modifier = StatusBarPaddingModifier.onGloballyPositioned {
                    callInfoWidgetHeight = it.size.height
                }
            )
        }
    }
}

private fun CallUiState.shouldShowCallInfo(): State<Boolean> {
    return derivedStateOf {
        callState is CallStateUi.Reconnecting || callState is CallStateUi.Connecting || callState is CallStateUi.Disconnected || isRecording
    }
}

private fun CallUiState.shouldHideWatermark(): State<Boolean> {
    return derivedStateOf {
        callState is CallStateUi.Connected && isRecording
    }
}

@Composable
private fun titleFor(callState: CallStateUi) =
    when (callState) {
        CallStateUi.Connecting, CallStateUi.Reconnecting -> stringResource(id = R.string.kaleyra_call_status_connecting)
        is CallStateUi.Disconnected -> stringResource(id = R.string.kaleyra_call_status_ended)
        else -> ""
    }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun subtitleFor(callState: CallStateUi, groupCall: Boolean) =
    when (callState) {
        CallStateUi.Disconnected.Ended.AnsweredOnAnotherDevice -> stringResource(id = R.string.kaleyra_call_status_answered_on_other_device)
        CallStateUi.Disconnected.Ended.Declined -> pluralStringResource(
            id = R.plurals.kaleyra_call_status_declined,
            count = if (groupCall) 2 else 1
        )

        CallStateUi.Disconnected.Ended.Timeout -> pluralStringResource(
            id = R.plurals.kaleyra_call_status_no_answer,
            count = if (groupCall) 2 else 1
        )

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
        CallComponent(
            callUiState = CallUiState(
                featuredStreams = ImmutableList(listOf(streamUiMock, streamUiMock)),
                callState = CallStateUi.Connected,
                isGroupCall = true
            ),
            callComponentState = rememberCallComponentState(
                featuredStreamsCount = 2,
                configuration = LocalConfiguration.current,
                maxWidth = 400.dp
            ),
            onBackPressed = { },
            onStreamFullscreenClick = {  },
            onPipStreamPositioned = { }
        )
    }
}