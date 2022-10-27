@file:OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.fadeBelowOfRootBottomBound
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@Composable
internal fun BottomSheetContent(
    sheetState: BottomSheetState,
    callActions: ImmutableList<CallAction>,
    orientation: StateFlow<Int>,
    modifier: Modifier = Modifier
) {
    val rotation by mapToRotationState(orientation)
    val scope = rememberCoroutineScope()
    val columnCount = remember(callActions) {
        callActions.count.coerceAtMost(4)
    }
    val halfExpand = remember {
        {
            if (sheetState.isCollapsed) {
                scope.launch {
                    sheetState.halfExpand()
                }
            }
        }
    }

    Log.e("BottomSheetContent", "KRL-recomposed")

    CompositionLocalProvider(LocalOverScrollConfiguration provides null) {
        Column(modifier = modifier) {
            Line(
                sheetState = sheetState,
                onClickLabel = stringResource(id = R.string.kaleyra_call_show_actions),
                onClick = halfExpand
            )
            LazyVerticalGrid(
                columns = GridCells.Fixed(count = columnCount),
                contentPadding = PaddingValues(bottom = 8.dp),
            ) {
                items(items = callActions.value) { action ->
                    Box(
                        modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        var toggled by remember { mutableStateOf(action is CallAction.Toggleable && action.isToggled) }
                        CallAction(
                            toggled = toggled,
                            onToggle = {
                                when (action) {
                                    is CallAction.Clickable -> action.onClick()
                                    is CallAction.Toggleable -> {
                                        action.onToggle(it)
                                        toggled = it
                                    }
                                }
                            },
                            text = textFor(action),
                            icon = painterFor(action),
                            enabled = action.isEnabled,
                            rotation = rotation,
                            colors = colorsFor(action),
                            modifier = Modifier.fadeBelowOfRootBottomBound()
                        )
                    }
                }
            }
        }
    }

}

@Composable
private fun mapToRotationState(orientation: StateFlow<Int>): State<Float> {
    val orientationValue by orientation.collectAsStateWithLifecycle()
    return remember {
        derivedStateOf {
            when (orientationValue) {
                90 -> -90f
                270 -> 90f
                else -> 0f
            }
        }
    }
}

@Composable
private fun textFor(action: CallAction): String =
    stringResource(
        id = when (action) {
            is CallAction.Camera -> R.string.kaleyra_call_action_video_disable
            is CallAction.Microphone -> R.string.kaleyra_call_action_mic_mute
            is CallAction.SwitchCamera -> R.string.kaleyra_call_action_switch_camera
            is CallAction.HangUp -> R.string.kaleyra_call_hangup
            is CallAction.Chat -> R.string.kaleyra_call_action_chat
            is CallAction.Whiteboard -> R.string.kaleyra_call_action_whiteboard
            is CallAction.FileSharing -> R.string.kaleyra_call_action_file_share
            is CallAction.Audio -> R.string.kaleyra_call_action_audio_route
            is CallAction.ScreenSharing -> R.string.kaleyra_call_action_screen_share
        }
    )


@Composable
private fun painterFor(action: CallAction): Painter =
    painterResource(
        id = when (action) {
            is CallAction.Camera -> R.drawable.ic_kaleyra_camera_off
            is CallAction.Microphone -> R.drawable.ic_kaleyra_mic_off
            is CallAction.SwitchCamera -> R.drawable.ic_kaleyra_switch_camera
            is CallAction.HangUp -> R.drawable.ic_kaleyra_hangup
            is CallAction.Chat -> R.drawable.ic_kaleyra_chat
            is CallAction.Whiteboard -> R.drawable.ic_kaleyra_whiteboard
            is CallAction.FileSharing -> R.drawable.ic_kaleyra_file_share
            is CallAction.Audio -> R.drawable.ic_kaleyra_earpiece
            is CallAction.ScreenSharing -> R.drawable.ic_kaleyra_screen_share
        }
    )

@Composable
private fun colorsFor(action: CallAction): CallActionColors {
    return if (action is CallAction.HangUp) {
        val backgroundColor = colorResource(id = R.color.kaleyra_color_hang_up_button)
        CallActionDefaults.colors(
            backgroundColor = backgroundColor,
            iconColor = Color.White,
            disabledBackgroundColor = backgroundColor.copy(alpha = .12f),
            disabledIconColor = Color.White.copy(alpha = ContentAlpha.disabled)
        )
    } else CallActionDefaults.colors()
}

@Preview
@Composable
fun BottomSheetContentPreview() {
    KaleyraTheme {
        BottomSheetContent(
            sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.HalfExpanded),
            callActions = ImmutableList(
                listOf(
                    CallAction.Microphone(isToggled = true, isEnabled = true) {},
                    CallAction.Camera(isToggled = false, isEnabled = true) {},
                    CallAction.SwitchCamera(true) {},
                    CallAction.HangUp(true) {},
                    CallAction.Chat(true) {},
                    CallAction.Whiteboard(true) {},
                    CallAction.Audio(true) {},
                    CallAction.FileSharing(true) {},
                    CallAction.ScreenSharing(true) {}
                )
            ),
            orientation = MutableStateFlow(0)
        )
    }
}

