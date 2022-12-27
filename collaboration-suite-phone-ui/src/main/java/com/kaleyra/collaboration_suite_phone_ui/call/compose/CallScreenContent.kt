package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.os.SystemClock
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamTile
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.reflect.jvm.internal.impl.descriptors.Visibilities.Local
import kotlin.time.Duration.Companion.seconds

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
    private val maxWidth: Dp
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

    var showCallInfo by mutableStateOf(false)
        private set

    var fullscreenStream by mutableStateOf<StreamUi?>(null)
        private set

    val exitFullscreen by derivedStateOf {
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
    onFullscreenClick: (StreamUi) -> Unit,
    modifier: Modifier = Modifier
) {
    var callInfoWidgetHeight by remember { mutableStateOf(0) }
    val streamHeaderOffset by animateIntAsState(targetValue = if (state.showCallInfo) callInfoWidgetHeight else 0)

    AnimatedContent(
        targetState = state.fullscreenStream,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(
                animationSpec = tween(
                    90
                )
            )
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
                    StreamTile(
                        stream = stream,
                        isFullscreen = false,
                        onBackPressed = if (index == 0 && !state.showCallInfo) onBackPressed else null,
                        onFullscreenClick = { onFullscreenClick(stream) },
//                        modifier = Modifier.clickable {
//                            hideStreamHeaderMillis = 4000
//                        },
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
            StreamTile(
                stream = target,
                isFullscreen = true,
                onBackPressed = if (!state.showCallInfo) state::exitFullscreenMode else null,
                onFullscreenClick = { onFullscreenClick(target) },
                headerModifier = Modifier
                    .offset { IntOffset(x = 0, y = streamHeaderOffset) },
//                    .graphicsLayer { alpha = streamsHeaderAlpha },
                modifier = StatusBarPaddingModifier
//                    .clickable {
//                    hideStreamHeaderMillis = 4000
//                }
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
            modifier = StatusBarPaddingModifier.onGloballyPositioned {
                callInfoWidgetHeight = it.size.height
            }
        )
    }

}

@Preview
@Composable
fun CallScreenContentPreview() {
    KaleyraTheme {
        CallScreenContent(
            state = CallScreenContentState(
                streams = ImmutableList(listOf(streamUiMock, streamUiMock)),
                callInfo = callInfoMock,
                configuration = LocalConfiguration.current,
                maxWidth = 400.dp
            ),
            onBackPressed = { },
            onFullscreenClick = { }
        )
    }
}