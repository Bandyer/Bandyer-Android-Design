@file:OptIn(ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.LocalOverScrollConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.utils.OrientationListener
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PhoneCallActivity : ComponentActivity() {

    private lateinit var orientationListener: OrientationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        orientationListener = OrientationListener(this)

        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen(
                    orientation = orientationListener.orientation
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        orientationListener.enable()
    }

    override fun onStop() {
        super.onStop()
        orientationListener.disable()
    }
}

val callActions = ImmutableList(
    listOf<CallAction>(
        CallAction.Microphone(false, true, {}),
        CallAction.Camera(false, true, {}),
        CallAction.SwitchCamera(true, {}),
        CallAction.HangUp(true, {}),
        CallAction.Chat(true, {}),
        CallAction.Whiteboard(true, {}),
        CallAction.Audio(true, {}),
        CallAction.FileSharing(true, {}),
        CallAction.ScreenSharing(true, {})
    )
)

@Composable
fun CallScreen(orientation: StateFlow<Int>) {
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.HalfExpanded,
        collapsable = true
    )
    val isCollapsed by remember(sheetState) {
        derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction == 1f }
    }
    val alpha by animateFloatAsState(if (isCollapsed) 0f else 1f)
    BottomSheetScaffold(
        sheetState = sheetState,
        sheetPeekHeight = 48.dp,
        sheetHalfExpandedHeight = 166.dp,
        anchor = { Anchor() },
        sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = alpha),
        sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        backgroundColor = Color.Black,
        contentColor = Color.White,
        sheetContent = {
            BottomSheetContent(
                sheetState = sheetState,
                callActions = callActions,
                orientation = orientation
            )
        }
    ) { sheetPadding -> ScreenContent(sheetState, sheetPadding) }
}

@Composable
internal fun ScreenContent(sheetState: BottomSheetState, sheetPadding: WindowInsets) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(sheetPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Current value: ${sheetState.currentValue}")
        Text(text = "Target value: ${sheetState.targetValue}")
        Text(text = "Direction: ${sheetState.direction}")
        Text(text = "Fraction: ${sheetState.progress.fraction}")
        Text(text = "Offset: ${sheetState.offset.value}")
        Text(text = "Overflow: ${sheetState.overflow.value}")
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun Anchor() {
    Box {
        CompositionLocalProvider(
            LocalOverScrollConfiguration provides null
        ) {
            LazyRow(reverseLayout = true) {
                items(8) {
                    Text(
                        text = "Anchor",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(56.dp)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun CallScreenPreview() {
    CallScreen(
        orientation = MutableStateFlow(0)
    )
}