@file:OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class)

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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.model.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.Whiteboard
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PhoneCallActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                CallScreen(
                    callActions = mockCallActions,
//                    screenShareTargets = ImmutableList(
//                        listOf(
//                            ScreenShareTargetUi.Device,
//                            ScreenShareTarget.Application
//                        )
//                    ),
//                    audioDevices = mockAudioDevices,
//                    transfers = ImmutableList(
//                        listOf(
//                            mockDownloadTransfer.copy(state = Transfer.State.Success),
//                            mockUploadTransfer
//                        )
                )
            }
        }
    }
}

enum class BottomSheetScreen {
    CallActions, AudioOutput, ScreenShare, FileShare, Whiteboard
}

internal object CallScreenDefaults {
    val AppBarVisibilityThreshold = 64.dp

    const val TargetStateFractionThreshold = .9f
}

internal class CallScreenState(
    initialSheetScreen: BottomSheetScreen,
    val sheetState: BottomSheetState,
    private val scope: CoroutineScope,
    density: Density
) {

    private val appBarThresholdPx =
        with(density) { CallScreenDefaults.AppBarVisibilityThreshold.toPx() }

    var currentScreen: BottomSheetScreen by mutableStateOf(initialSheetScreen)
        private set

    val shouldShowAppBar: Boolean by derivedStateOf {
        sheetState.offset.value < appBarThresholdPx
    }

    val shouldHideBackground by derivedStateOf {
        sheetState.targetValue == BottomSheetValue.Collapsed && sheetState.progress.fraction >= CallScreenDefaults.TargetStateFractionThreshold
    }

    val shouldShowCallActions by derivedStateOf {
        currentScreen != BottomSheetScreen.CallActions && (sheetState.targetValue == BottomSheetValue.HalfExpanded && sheetState.progress.fraction >= .95f || sheetState.targetValue == BottomSheetValue.Collapsed)
    }

    fun navigateToBottomSheetScreen(screen: BottomSheetScreen) {
        currentScreen = screen
    }

    fun halfExpandBottomSheet() {
        scope.launch {
            sheetState.halfExpand()
        }
    }
}

@Composable
internal fun BottomSheetContent(
    callScreenState: CallScreenState,
    callActions: @Composable () -> Unit,
    audioOutput: @Composable () -> Unit,
    screenShare: @Composable () -> Unit,
    fileShare: @Composable () -> Unit,
    whiteboard: @Composable () -> Unit
) {
    BottomSheetContentLayout(
        lineState = mapToLineState(callScreenState.sheetState),
        onLineClick = {
            if (callScreenState.sheetState.isCollapsed) {
                callScreenState.halfExpandBottomSheet()
            }
        }
    ) {
        AnimatedContent(callScreenState.currentScreen) { target ->
            when (target) {
                BottomSheetScreen.CallActions -> callActions()
                BottomSheetScreen.AudioOutput -> audioOutput()
                BottomSheetScreen.ScreenShare -> screenShare()
                BottomSheetScreen.FileShare -> fileShare()
                BottomSheetScreen.Whiteboard -> whiteboard()
            }
        }
    }
}

@Composable
fun CallScreen(
    callActions: ImmutableList<CallAction>,
//    screenShareTargets: ImmutableList<ScreenShareTarget>,
//    audioDevices: ImmutableList<AudioDevice>,
//    transfers: ImmutableList<TransferUi>
) {
    val isDarkTheme = isSystemInDarkTheme()
    val systemUiController = rememberSystemUiController()

    val callScreenState = CallScreenState(
        initialSheetScreen = BottomSheetScreen.CallActions,
        sheetState = rememberBottomSheetState(
            initialValue = BottomSheetValue.HalfExpanded,
            collapsable = true
        ),
        scope = rememberCoroutineScope(),
        density = LocalDensity.current
    )

    val backgroundAlpha by animateFloatAsState(if (callScreenState.shouldHideBackground) 0f else 1f)

//    LaunchedEffect(callScreenState.shouldShowAppBar) {
//        systemUiController.statusBarDarkContentEnabled = if (callScreenState.shouldShowAppBar) !isDarkTheme else false
//    }

    LaunchedEffect(callScreenState.shouldShowCallActions) {
        if (callScreenState.shouldShowCallActions) {
            callScreenState.navigateToBottomSheetScreen(BottomSheetScreen.CallActions)
        }
    }

    Box {
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = callScreenState.sheetState,
            sheetPeekHeight = 48.dp,
            sheetHalfExpandedHeight = 166.dp,
            anchor = { },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    callScreenState = callScreenState,
                    callActions = {
                        CallActions(
                            items = callActions,
                            itemsPerRow = callActions.count().coerceIn(
                                minimumValue = 1,
                                maximumValue = 4
                            ),
                            onItemClick = {
                                callScreenState.navigateToBottomSheetScreen(
                                    screen = when (it) {
                                        is CallAction.Audio -> BottomSheetScreen.AudioOutput
                                        is CallAction.ScreenShare -> BottomSheetScreen.ScreenShare
                                        is CallAction.FileShare -> BottomSheetScreen.FileShare
                                        is CallAction.Whiteboard -> BottomSheetScreen.Whiteboard
                                        else -> BottomSheetScreen.CallActions
                                    }
                                )
                            }
                        )
                    },
                    audioOutput = {
//                        AudioOutput(
//                            items = audioDevices,
//                            onItemClick = { callScreenState.halfExpandBottomSheet() },
//                            onCloseClick = { callScreenState.halfExpandBottomSheet() }
//                        )
                    },
                    screenShare = {
//                        ScreenShare(
//                            items = screenShareTargets,
//                            onItemClick = { callScreenState.halfExpandBottomSheet() },
//                            onCloseClick = { callScreenState.halfExpandBottomSheet() }
//                        )
                    },
                    fileShare = {
//                        FileShare(
//                            items = transfers,
//                            onFabClick = { },
//                            onCloseClick = { callScreenState.halfExpandBottomSheet() }
//                        )
                    },
                    whiteboard = {
                        Whiteboard(
                            loading = true,
                            offline = true,
                            fileUpload = WhiteboardUpload.Uploading(.6f),
                            onCloseClick = {},
                            onReloadClick = {}
                        )
                    })
            },
            content = {
                ScreenContent(callScreenState.sheetState, it)
            })

        CallScreenAppBar(callScreenState = callScreenState)
    }
}

@Composable
internal fun CallScreenAppBar(callScreenState: CallScreenState) {
    AnimatedVisibility(
        visible = callScreenState.shouldShowAppBar,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(elevation = AppBarDefaults.TopAppBarElevation) {
            Spacer(
                Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
            Column(Modifier.statusBarsPadding()) {
                when (callScreenState.currentScreen) {
                    BottomSheetScreen.FileShare -> FileShareAppBar(onBackPressed = { callScreenState.halfExpandBottomSheet() })
                    BottomSheetScreen.Whiteboard -> WhiteboardAppBar(
                        onBackPressed = { callScreenState.halfExpandBottomSheet() },
                        onUploadClick = {}
                    )
                    else -> Unit
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
//    CallScreen()
}