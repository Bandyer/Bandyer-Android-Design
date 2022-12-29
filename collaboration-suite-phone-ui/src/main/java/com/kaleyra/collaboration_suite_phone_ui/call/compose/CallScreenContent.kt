package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamTile
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.isActive

const val CallInfoWidgetTag = "CallInfoWidgetTag"

private val StatusBarPaddingModifier = Modifier.statusBarsPadding()

@Composable
internal fun rememberCallScreenContentState(
    streams: ImmutableList<StreamUi>,
    callInfo: CallInfoUi,
    configuration: Configuration,
    maxWidth: Dp
) = remember(streams, callInfo, configuration, maxWidth) {
    CallScreenContentState(
        streams = streams,
        callInfo = callInfo,
        configuration = configuration,
        maxWidth = maxWidth
    )
}

internal class CallScreenContentState(
    val streams: ImmutableList<StreamUi>,
    val callInfo: CallInfoUi,
    private val configuration: Configuration,
    private val maxWidth: Dp,
    showCallInfo: Boolean = false,
    fullscreenStream: StreamUi? = null
) {

    private val isDevicePortrait: Boolean
        get() = configuration.orientation == Configuration.ORIENTATION_PORTRAIT

    val columns by derivedStateOf {
        when {
            isDevicePortrait && maxWidth < 600.dp -> 1
            isDevicePortrait && streams.count() > 2 -> 2
            streams.count() > 1 -> 2
            else -> 1
        }
    }

    var showCallInfo by mutableStateOf(showCallInfo)
        private set

    var fullscreenStream by mutableStateOf(fullscreenStream)
        private set

    val isFullscreenStreamRemoved by derivedStateOf {
        fullscreenStream != null && !streams.value.contains(fullscreenStream)
    }

    fun enterFullscreenMode(stream: StreamUi) {
        fullscreenStream = stream
    }

    fun exitFullscreenMode() {
        fullscreenStream = null
    }

    fun showCallInfo() {
        showCallInfo = true
    }

    fun hideCallInfo() {
        showCallInfo = false
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CallScreenContent(
    state: CallScreenContentState,
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

//    var hideStreamHeaderMillis by remember { mutableStateOf(5000L) }
//    val countDown = rememberCountdownTimerState(hideStreamHeaderMillis)
//    val streamsHeaderAlpha by animateFloatAsState(
//        targetValue = if (countDown.value > 0L) 1f else 0f,
//        animationSpec = tween()
//    )

    AnimatedContent(
        targetState = state.fullscreenStream,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
        },
        modifier = modifier
    ) { target ->
        if (target == null) {
            AdaptiveGrid(
                columns = state.columns,
                modifier = StatusBarPaddingModifier
            ) {
                val streams = state.streams
                repeat(streams.count()) { index ->
                    val stream = streams.value[index]
                    FeaturedStream(
                        stream = stream,
                        isFullscreen = false,
                        onBackPressed = if (index == 0 && !state.showCallInfo) onBackPressed else null,
                        onFullscreenClick = { state.enterFullscreenMode(stream) },
                        modifier = Modifier.clickable {
//                            hideStreamHeaderMillis = 4000
                        },
                        headerModifier = Modifier
                            .offset {
                                IntOffset(
                                    x = 0,
                                    y = if (index < state.columns) streamHeaderOffset else 0
                                )
                            }
//                            .graphicsLayer { alpha = streamsHeaderAlpha }
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
                    .offset { IntOffset(x = 0, y = streamHeaderOffset) },
//                    .graphicsLayer { alpha = streamsHeaderAlpha },
                modifier = StatusBarPaddingModifier.clickable {
//                    hideStreamHeaderMillis = 4000
                }
            )
        }
    }

    AnimatedVisibility(
        visible = state.showCallInfo,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        CallInfoWidget(
            onBackPressed = if (state.fullscreenStream != null) state::exitFullscreenMode else onBackPressed,
            callInfo = state.callInfo,
            modifier = StatusBarPaddingModifier
                .onGloballyPositioned { callInfoWidgetHeight = it.size.height }
                .testTag(CallInfoWidgetTag)
        )
    }

}

@Preview
@Composable
fun CallScreenContentPreview() {
    KaleyraTheme {
        CallScreenContent(
            state = rememberCallScreenContentState(
                streams = ImmutableList(listOf(streamUiMock, streamUiMock)),
                callInfo = callInfoMock,
                configuration = LocalConfiguration.current,
                maxWidth = 400.dp
            ),
            onBackPressed = { }
        )
    }
}