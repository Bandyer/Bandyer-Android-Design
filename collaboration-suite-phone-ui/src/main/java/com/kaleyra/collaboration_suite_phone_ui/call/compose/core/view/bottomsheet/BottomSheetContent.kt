package com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet

import android.content.res.Configuration
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.AudioOutputSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.CallActionsSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShareSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.ScreenShareSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.WhiteboardSection
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.launch

internal enum class BottomSheetSection {
    CallActions, AudioOutput, ScreenShare, FileShare, Whiteboard
}

internal class BottomSheetContentState(initialSheetSection: BottomSheetSection) {

    var currentSection: BottomSheetSection by mutableStateOf(initialSheetSection)
        private set

    fun navigateToSection(section: BottomSheetSection) {
        currentSection = section
    }

    companion object {
        fun Saver(): Saver<BottomSheetContentState, *> = Saver(
            save = { it.currentSection },
            restore = { BottomSheetContentState(it) }
        )
    }
}

@Composable
internal fun rememberBottomSheetContentState(
    initialSheetSection: BottomSheetSection
) = rememberSaveable(saver = BottomSheetContentState.Saver()) {
    BottomSheetContentState(initialSheetSection)
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalAnimationApi::class)
@Composable
internal fun BottomSheetContent(
    contentState: BottomSheetContentState,
    sheetState: BottomSheetState
) {
    val scope = rememberCoroutineScope()
    val halfExpandBottomSheet = remember {
        {
            scope.launch {
                sheetState.halfExpand()
            }
        }
    }

    CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
        Column {
            Line(
                state = toLineState(sheetState),
                onClickLabel = stringResource(id = R.string.kaleyra_call_show_buttons),
                onClick = {
                    if (sheetState.isCollapsed) {
                        halfExpandBottomSheet()
                    }
                }
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
                            }
                        )
                    }
                    BottomSheetSection.AudioOutput -> {
                        AudioOutputSection(
                            onItemClick = { halfExpandBottomSheet() },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) }
                        )
                    }
                    BottomSheetSection.ScreenShare -> {
                        ScreenShareSection(
                            onItemClick = { halfExpandBottomSheet() },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) }
                        )
                    }
                    BottomSheetSection.FileShare -> {
                        FileShareSection(
                            onFabClick = { /*TODO*/ },
                            onItemClick = { /*TODO*/ },
                            onItemActionClick = { /*TODO*/ },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) }
                        )
                    }
                    BottomSheetSection.Whiteboard -> {
                        WhiteboardSection(
                            onReloadClick = { /*TODO*/ },
                            onBackPressed = { contentState.navigateToSection(BottomSheetSection.CallActions) }
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
                contentState = rememberBottomSheetContentState(initialSheetSection = BottomSheetSection.CallActions),
                sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
            )
        }
    }
}
