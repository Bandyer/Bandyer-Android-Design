/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.screen.view

import android.content.res.Configuration
import android.view.MotionEvent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.call.callinfowidget.CallInfoWidget
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isAtLeastMediumSizeWidth
import com.kaleyra.video_sdk.call.utils.ConfigurationExtensions.isOrientationPortrait
import com.kaleyra.video_sdk.call.countdowntimer.rememberCountdownTimerState
import com.kaleyra.video_sdk.call.screen.model.CallStateUi
import com.kaleyra.video_sdk.call.screen.model.CallUiState
import com.kaleyra.video_sdk.call.screen.viewmodel.CallViewModel
import com.kaleyra.video_sdk.call.stream.model.streamUiMock
import com.kaleyra.video_sdk.call.stream.*
import com.kaleyra.video_sdk.call.stream.view.core.DefaultStreamAvatarSize
import com.kaleyra.video_sdk.call.stream.view.featured.FeaturedStream
import com.kaleyra.video_sdk.call.stream.view.featured.HeaderAutoHideMs
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.requestConfiguration
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.video_sdk.extensions.TextStyleExtensions.shadow
import com.kaleyra.video_sdk.theme.KaleyraTheme
import com.kaleyra.video_sdk.R

const val CallComponentTag = "CallComponentTag"
const val StreamsGridTag = "StreamsGridTag"
private val StatusBarPaddingModifier = Modifier.statusBarsPadding()
val YouAreAloneAvatarPadding = DefaultStreamAvatarSize / 2 + 24.dp
val SnackbarPadding = 16.dp
val FeaturedStreamHeaderHeight = 48.dp
const val FullScreenMessageMs = 1000L

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
    shouldShowUserMessages: Boolean = true,
    modifier: Modifier = Modifier
) {
    val callUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val callComponentState = rememberCallComponentState(
        featuredStreamsCount = callUiState.featuredStreams.count(),
        configuration = LocalConfiguration.current,
        maxWidth = maxWidth
    )

    // Interrupt the collection of user messages when there is an another ui component
    // collecting the one time events. These events can be received only by one collector at a time.
    val userMessage = if (shouldShowUserMessages) viewModel.userMessage.collectAsStateWithLifecycle(initialValue = null).value else null

    CallComponent(
        callUiState = callUiState,
        callComponentState = callComponentState,
        userMessage = userMessage,
        onBackPressed = onBackPressed,
        onStreamFullscreenClick = onStreamFullscreenClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun CallComponent(
    callUiState: CallUiState,
    callComponentState: CallComponentState,
    onBackPressed: () -> Unit,
    userMessage: UserMessage? = null,
    onStreamFullscreenClick: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowCallInfo by remember(callUiState) { callUiState.shouldShowCallInfo() }
    val shouldHideWatermark by remember(callUiState) { callUiState.shouldHideWatermark() }
    val shouldShowFullscreenToast by remember(callUiState) {
        derivedStateOf { callUiState.fullscreenStream != null }
    }

    val density = LocalDensity.current
    val insets = WindowInsets.statusBars
    var callInfoWidgetHeight by remember { mutableStateOf(0) }
    val streamHeaderHeight = remember { with(density) { FeaturedStreamHeaderHeight.toPx() } }
    val snackbarTopPadding = remember { with(density) { SnackbarPadding.toPx() } }
    val statusBarPadding = remember {
        with(density) { insets.asPaddingValues(this).calculateTopPadding().toPx() }
    }

    var streamsHeaderAutoHideResetFlag by remember { mutableStateOf(true) }

    val shouldAddSnackbarHeaderPadding by remember(callUiState, callComponentState) {
        derivedStateOf {
            callUiState.featuredStreams.value.take(callComponentState.columns).any { it.video?.view == null || !it.video.isEnabled }
        }
    }
    val snackbarHeaderPaddingTimer by rememberCountdownTimerState(initialMillis = HeaderAutoHideMs, enable = !shouldAddSnackbarHeaderPadding)
    val snackbarOffsetValue by remember(snackbarTopPadding, callInfoWidgetHeight, snackbarHeaderPaddingTimer, streamHeaderHeight, shouldShowCallInfo) {
        derivedStateOf {
            statusBarPadding + when {
                shouldShowCallInfo -> callInfoWidgetHeight.toFloat()
                snackbarHeaderPaddingTimer > 0L -> streamHeaderHeight
                else -> 0f
            }
        }
    }
    val streamHeaderOffset by animateIntAsState(targetValue = if (shouldShowCallInfo) callInfoWidgetHeight else 0, label = "headerOffset")
    val snackbarOffset by animateIntAsState(targetValue = snackbarOffsetValue.toInt(), label = "snackbarOffset")

    CompositionLocalProvider(LocalContentColor provides Color.White) {
        Box(modifier.testTag(CallComponentTag)) {
            if (callUiState.callState !is CallStateUi.Ringing && callUiState.callState !is CallStateUi.Dialing && callUiState.callState !is CallStateUi.Disconnected.Ended) {
                AdaptiveGrid(
                    columns = if (callUiState.fullscreenStream == null) callComponentState.columns else 1,
                    modifier = Modifier
                        .pointerInteropFilter {
                            if (it.action == MotionEvent.ACTION_DOWN) {
                                streamsHeaderAutoHideResetFlag = !streamsHeaderAutoHideResetFlag
                            }
                            false
                        }
                        .testTag(StreamsGridTag)
                ) {
                    val streams = callUiState.fullscreenStream?.let { listOf(it) } ?: callUiState.featuredStreams.value
                    repeat(streams.count()) { index ->
                        val stream = streams[index]
                        key(stream.id) {
                            Box {
                                val autoHideHeaderTimer by rememberCountdownTimerState(
                                    initialMillis = HeaderAutoHideMs,
                                    reset = streamsHeaderAutoHideResetFlag,
                                    enable = stream.video?.view != null && stream.video.isEnabled
                                )
                                val headerAlpha by animateFloatAsState(targetValue = if (autoHideHeaderTimer > 0L) 1f else 0f, label = "headerAlpha")
                                FeaturedStream(
                                    stream = stream,
                                    isFullscreen = callUiState.fullscreenStream != null,
                                    showOverlay = with(callUiState) { featuredStreams.count() + thumbnailStreams.count() } < 2,
                                    fullscreenVisible = callUiState.callState is CallStateUi.Connected,
                                    onBackPressed = if (index == 0 && !(shouldShowCallInfo)) onBackPressed else null,
                                    onFullscreenClick = remember(onStreamFullscreenClick, callUiState.fullscreenStream) {
                                        { if (callUiState.fullscreenStream != null) onStreamFullscreenClick(null) else onStreamFullscreenClick(stream.id) }
                                    },
                                    headerModifier = remember(index, callComponentState, streamHeaderOffset) {
                                        Modifier
                                            .offset {
                                                IntOffset(
                                                    x = 0,
                                                    y = if (index < callComponentState.columns) streamHeaderOffset else 0
                                                )
                                            }
                                            .statusBarsPadding()
                                            .graphicsLayer {
                                                alpha = headerAlpha
                                            }
                                    }
                                )

                                if (callUiState.amILeftAlone) {
                                    val padding by animateDpAsState(targetValue = if (stream.video?.view == null || !stream.video.isEnabled) YouAreAloneAvatarPadding else 0.dp, label = "avatarPadding")
                                    Text(
                                        text = stringResource(id = R.string.kaleyra_call_left_alone),
                                        style = LocalTextStyle.current.shadow(),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .offset {
                                                val offset = padding
                                                    .toPx()
                                                    .toInt()
                                                IntOffset(0, offset)
                                            }
                                    )
                                }
                            }
                        }
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
                    recording = callUiState.recording?.isRecording() ?: false,
                    modifier = StatusBarPaddingModifier.onGloballyPositioned {
                        callInfoWidgetHeight = it.size.height
                    }
                )
            }

            UserMessageSnackbarHandler(
                userMessage = userMessage,
                modifier = modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, snackbarOffset) }
            )

            if (shouldShowFullscreenToast) {
                val timer by rememberCountdownTimerState(initialMillis = FullScreenMessageMs)
                val visible by remember(timer) {
                    derivedStateOf { timer != 0L }
                }
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(),
                    exit = fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 100.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.kaleyra_fullscreen_info),
                        fontSize = 16.sp,
                        modifier = Modifier
                            .background(Color.Black, shape = CircleShape)
                            .padding(vertical = 8.dp, horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

private fun CallUiState.shouldShowCallInfo(): State<Boolean> {
    return derivedStateOf {
        callState is CallStateUi.Reconnecting || callState is CallStateUi.Disconnected || recording?.isRecording() ?: false
    }
}

private fun CallUiState.shouldHideWatermark(): State<Boolean> {
    return derivedStateOf {
        callState is CallStateUi.Connected && recording?.isRecording() ?: false
    }
}

@Composable
private fun titleFor(callState: CallStateUi) =
    when (callState) {
        CallStateUi.Reconnecting -> stringResource(id = R.string.kaleyra_call_status_connecting)
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
            onStreamFullscreenClick = {  }
        )
    }
}