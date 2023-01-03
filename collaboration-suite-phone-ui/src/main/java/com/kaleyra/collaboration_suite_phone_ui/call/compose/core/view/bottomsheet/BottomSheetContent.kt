package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.AudioOutputComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.ScreenShareComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardComponent
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme

const val CallActionsComponentTag = "CallActionsComponentTag"
const val AudioOutputComponentTag = "AudioOutputComponentTag"
const val ScreenShareComponentTag = "ScreenShareComponentTag"
const val FileShareComponentTag = "FileShareComponentTag"
const val WhiteboardComponentTag = "WhiteboardComponentTag"

internal enum class BottomSheetComponent {
    CallActions, AudioOutput, ScreenShare, FileShare, Whiteboard
}

internal class BottomSheetContentState(
    initialComponent: BottomSheetComponent,
    initialLineState: LineState
) {

    var currentComponent: BottomSheetComponent by mutableStateOf(initialComponent)
        private set

    var currentLineState: LineState by mutableStateOf(initialLineState)
        private set

    fun navigateToComponent(component: BottomSheetComponent) {
        currentComponent = component
    }

    fun expandLine() {
        currentLineState = LineState.Expanded
    }

    fun collapseLine(color: Color? = null) {
        currentLineState = LineState.Collapsed(argbColor = color?.toArgb())
    }

    companion object {
        fun Saver(): Saver<BottomSheetContentState, *> = Saver(
            save = { Pair(it.currentComponent, it.currentLineState) },
            restore = { BottomSheetContentState(it.first, it.second) }
        )
    }
}

@Composable
internal fun rememberBottomSheetContentState(
    initialSheetComponent: BottomSheetComponent,
    initialLineState: LineState
) = rememberSaveable(saver = BottomSheetContentState.Saver()) {
    BottomSheetContentState(initialSheetComponent, initialLineState)
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BottomSheetContent(
    contentState: BottomSheetContentState,
    onLineClick: () -> Unit = { },
    onCallActionClick: (CallAction) -> Unit = { },
    onAudioDeviceClick: () -> Unit = { },
    onScreenShareTargetClick: () -> Unit = { },
    contentVisible: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Line(
            state = contentState.currentLineState,
            onClickLabel = stringResource(id = R.string.kaleyra_call_show_buttons),
            onClick = onLineClick
        )
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AnimatedContent(
                targetState = contentState.currentComponent,
                transitionSpec = {
                    fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90))
                }
            ) { target ->
                when (target) {
                    BottomSheetComponent.CallActions -> {
                        CallActionsComponent(
                            onItemClick = { action, toggled ->
                                // TODO move this out to callScreen onCallActionClick
                                contentState.navigateToComponent(
                                    component = when (action) {
                                        is CallAction.Audio -> BottomSheetComponent.AudioOutput
                                        is CallAction.ScreenShare -> BottomSheetComponent.ScreenShare
                                        is CallAction.FileShare -> BottomSheetComponent.FileShare
                                        is CallAction.Whiteboard -> BottomSheetComponent.Whiteboard
                                        else -> BottomSheetComponent.CallActions
                                    }
                                )
                                onCallActionClick(action)
                            },
                            modifier = Modifier.testTag(CallActionsComponentTag)
                        )
                    }
                    BottomSheetComponent.AudioOutput -> {
                        AudioOutputComponent(
                            onItemClick = { onAudioDeviceClick() },
                            onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                            modifier = Modifier.testTag(AudioOutputComponentTag)
                        )
                    }
                    BottomSheetComponent.ScreenShare -> {
                        ScreenShareComponent(
                            onItemClick = { onScreenShareTargetClick() },
                            onCloseClick = { contentState.navigateToComponent(BottomSheetComponent.CallActions) },
                            modifier = Modifier.testTag(ScreenShareComponentTag)
                        )
                    }
                    BottomSheetComponent.FileShare -> {
                        FileShareComponent(
                            onFabClick = { /*TODO*/ },
                            onItemClick = { /*TODO*/ },
                            onItemActionClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .testTag(FileShareComponentTag)
                        )
                    }
                    BottomSheetComponent.Whiteboard -> {
                        WhiteboardComponent(
                            onReloadClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .testTag(WhiteboardComponentTag)
                        )
                    }
                }
            }
        }
    }
//    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BottomSheetContentPreview() {
    KaleyraTheme {
        Surface {
            BottomSheetContent(
                contentState = rememberBottomSheetContentState(
                    initialSheetComponent = BottomSheetComponent.CallActions,
                    initialLineState = LineState.Expanded
                ),
                onLineClick = { }
            )
        }
    }
}
