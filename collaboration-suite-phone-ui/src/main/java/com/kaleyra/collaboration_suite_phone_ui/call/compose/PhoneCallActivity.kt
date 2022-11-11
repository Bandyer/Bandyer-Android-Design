@file:OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class, ExperimentalAnimationApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.audiooutput.AudioOutput
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.FileShare
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.ScreenShare
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.Whiteboard
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.launch

class PhoneCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen()
            }
        }
    }
}

var targetState by mutableStateOf(BottomSheetContent.CallActions)

enum class BottomSheetContent {
    CallActions, AudioRoute, FileShare, ScreenShare, Whiteboard
}

@Composable
fun CallScreen(
//    orientation: StateFlow<Int>
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.HalfExpanded,
        collapsable = true
    )
    val isCollapsed by remember(sheetState) {
        derivedStateOf { sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction >= .9f }
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
    val itemsPerRow by remember {
        derivedStateOf {
            mockCallActions.count.coerceIn(minimumValue = 1, maximumValue = 4)
        }
    }
    val alpha by animateFloatAsState(if (isCollapsed) 0f else 1f)
    LaunchedEffect(sheetState.targetValue) {
        if (sheetState.targetValue == BottomSheetValue.HalfExpanded && targetState != BottomSheetContent.CallActions) {
            targetState = BottomSheetContent.CallActions
        }
    }

    Box {
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            sheetPeekHeight = 48.dp,
            sheetHalfExpandedHeight = 166.dp,
            anchor = { },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = alpha),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    lineState = mapToLineState(sheetState),
                    onLineClick = halfExpand
                ) {
                    AnimatedContent(
                        targetState = targetState
                    ) { target ->
                        when (target) {
                            BottomSheetContent.CallActions -> {
                                CallActions(
                                    items = mockCallActions,
                                    itemsPerRow = itemsPerRow,
//                                    orientation = orientation
                                )
                            }
                            BottomSheetContent.AudioRoute -> {
                                AudioOutput(items = mockAudioDevices, onItemClick = {
                                    scope.launch {
                                        sheetState.halfExpand()
                                    }
                                }, onCloseClick = {
                                    scope.launch {
                                        sheetState.halfExpand()
                                    }
                                })
                            }
                            BottomSheetContent.ScreenShare -> {
                                ScreenShare(
                                    items = ImmutableList(
                                        listOf(
                                            ScreenShare.Device,
                                            ScreenShare.Application
                                        )
                                    ),
                                    onItemClick = {
                                        scope.launch {
                                            sheetState.halfExpand()
                                        }
                                    },
                                    onCloseClick = {
                                        scope.launch {
                                            sheetState.halfExpand()
                                        }
                                    })
                            }
                            BottomSheetContent.FileShare -> {
                                val list = produceState(ImmutableList(listOf())) {
                                    kotlinx.coroutines.delay(3000)
                                    value = ImmutableList(
                                        listOf(
                                            mockDownloadTransfer.copy(state = Transfer.State.Success),
                                            mockUploadTransfer
                                        )
                                    )
                                }
                                FileShare(
                                    items = list.value,
                                    onFabClick = {},
                                    onCloseClick = {
                                        scope.launch {
                                            sheetState.halfExpand()
                                        }
                                    }
                                )
                            }
                            BottomSheetContent.Whiteboard -> {
                                Whiteboard(
                                    loading = true,
                                    offline = true,
                                    fileUpload = WhiteboardUpload.Uploading(.6f),
                                    onCloseClick = {},
                                    onReloadClick = {}
                                )
                            }
                        }
                    }
                }
            }
        ) { sheetPadding ->
            ScreenContent(sheetState, sheetPadding)
        }

        val isDarkTheme = isSystemInDarkTheme()
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(sheetState.offset.value) {
            systemUiController.statusBarDarkContentEnabled =
                if (sheetState.offset.value < 300f) !isDarkTheme else false
        }

        AnimatedVisibility(
            visible = sheetState.offset.value < 300f,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Surface(elevation = AppBarDefaults.TopAppBarElevation) {
                Column {
                    Spacer(
                        Modifier
                            .background(MaterialTheme.colors.primary)
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                    )
                    when (targetState) {
                        BottomSheetContent.FileShare -> {
                            FileShareAppBar(onBackPressed = {})
                        }
                        BottomSheetContent.Whiteboard -> {
                            WhiteboardAppBar(
                                onBackPressed = {},
                                onUploadClick = {}
                            )
                        }
                    }
                }
            }
        }
    }
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

@Preview
@Composable
fun CallScreenPreview() {
    CallScreen()
}