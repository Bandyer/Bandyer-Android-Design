package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.AudioOutputSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.ScreenShareSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardSection
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalInsetsPadding

const val CallActionsSection = "CallActionsSection"
const val AudioOutputSection = "AudioOutputSection"
const val ScreenShareSection = "ScreenShareSection"
const val FileShareSection = "FileShareSection"
const val WhiteboardSection = "WhiteboardSection"

internal enum class BottomSheetSection {
    CallActions, AudioOutput, ScreenShare, FileShare, Whiteboard
}

internal class BottomSheetContentState(
    initialSection: BottomSheetSection,
    initialLineState: LineState
) {

    var currentSection: BottomSheetSection by mutableStateOf(initialSection)
        private set

    var currentLineState: LineState by mutableStateOf(initialLineState)
        private set

    fun navigateToSection(section: BottomSheetSection) {
        currentSection = section
    }

    fun expandLine() { currentLineState = LineState.Expanded }

    fun collapseLine(color: Color? = null) { currentLineState = LineState.Collapsed(color = color) }

    companion object {
        fun Saver(): Saver<BottomSheetContentState, *> = Saver(
            save = { Pair(it.currentSection, it.currentLineState) },
            restore = { BottomSheetContentState(it.first, it.second) }
        )
    }
}

@Composable
internal fun rememberBottomSheetContentState(
    initialSheetSection: BottomSheetSection,
    initialLineState: LineState
) = rememberSaveable(saver = BottomSheetContentState.Saver()) {
    BottomSheetContentState(initialSheetSection, initialLineState)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun BottomSheetContent(
    contentState: BottomSheetContentState,
    onLineClick: () -> Unit
) {
    CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
        Column(Modifier.horizontalInsetsPadding()) {
            Line(
                state = contentState.currentLineState,
                onClickLabel = stringResource(id = R.string.kaleyra_call_show_buttons),
                onClick = onLineClick
            )
            AnimatedContent(
                targetState = contentState.currentSection,
                transitionSpec = { fadeIn(animationSpec = tween(220, delayMillis = 90)) with fadeOut(animationSpec = tween(90)) }
            ) { target ->
                when (target) {
                    BottomSheetSection.CallActions -> {
                        CallActionsSection(
                            onItemClick = { action, toggled ->
                                contentState.navigateToSection(
                                    section = when (action) {
                                        is CallAction.Audio -> BottomSheetSection.AudioOutput
                                        is CallAction.ScreenShare -> BottomSheetSection.ScreenShare
                                        is CallAction.FileShare -> BottomSheetSection.FileShare
                                        is CallAction.Whiteboard -> BottomSheetSection.Whiteboard
                                        else -> BottomSheetSection.CallActions
                                    }
                                )
                            },
                            modifier = Modifier.testTag(CallActionsSection)
                        )
                    }
                    BottomSheetSection.AudioOutput -> {
                        AudioOutputSection(
                            onItemClick = { },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) },
                            modifier = Modifier.testTag(AudioOutputSection)
                        )
                    }
                    BottomSheetSection.ScreenShare -> {
                        ScreenShareSection(
                            onItemClick = { },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) },
                            modifier = Modifier.testTag(ScreenShareSection)
                        )
                    }
                    BottomSheetSection.FileShare -> {
                        FileShareSection(
                            onFabClick = { /*TODO*/ },
                            onItemClick = { /*TODO*/ },
                            onItemActionClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .testTag(FileShareSection)
                        )
                    }
                    BottomSheetSection.Whiteboard -> {
                        WhiteboardSection(
                            onReloadClick = { /*TODO*/ },
                            modifier = Modifier
                                .padding(top = 20.dp)
                                .testTag(WhiteboardSection)
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun BottomSheetContentPreview() {
    KaleyraTheme {
        Surface {
            BottomSheetContent(
                contentState = rememberBottomSheetContentState(initialSheetSection = BottomSheetSection.CallActions, initialLineState = LineState.Expanded),
                onLineClick = { }
            )
        }
    }
}
