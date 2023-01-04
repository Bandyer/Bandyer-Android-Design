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
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.CallInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

const val InCallContentTag = "InCallContentTag"
private val StatusBarPaddingModifier = Modifier.statusBarsPadding()

@Composable
internal fun rememberInCallContentState(
    streams: ImmutableList<StreamUi>,
    callInfo: CallInfoUi,
    configuration: Configuration,
    maxWidth: Dp
) = remember(streams, callInfo, configuration, maxWidth) {
    InCallContentState(
        streams = streams,
        callInfo = callInfo,
        configuration = configuration,
        maxWidth = maxWidth
    )
}

internal class InCallContentState(
    val streams: ImmutableList<StreamUi>,
    val callInfo: CallInfoUi,
    private val configuration: Configuration,
    private val maxWidth: Dp,
    // Parameters added for testing purpose
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
internal fun InCallContent(
    state: InCallContentState,
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

    AnimatedContent(
        targetState = state.fullscreenStream,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
        },
        modifier = modifier.testTag(InCallContentTag)
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
        )
    }

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
fun InCallContentPreview() {
    KaleyraTheme {
        InCallContent(
            state = rememberInCallContentState(
                streams = ImmutableList(listOf(streamUiMock, streamUiMock)),
                callInfo = callInfoMock,
                configuration = LocalConfiguration.current,
                maxWidth = 400.dp
            ),
            onBackPressed = { }
        )
    }
}