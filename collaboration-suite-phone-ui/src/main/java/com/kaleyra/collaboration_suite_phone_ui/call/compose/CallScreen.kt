package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ConfigurationExtensions.isAtLeastMediumSizeDevice
import com.kaleyra.collaboration_suite_phone_ui.call.compose.StreamViewSettings.pipSettings
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetScaffold
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.rememberBottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.rememberBottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.feedback.UserFeedback
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.CameraPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.MultiplePermissionsState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.RecordAudioPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.findActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.rememberMultiplePermissionsState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.precall.view.common.HelperText
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.RecordingDot
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamContainer
import com.kaleyra.collaboration_suite_phone_ui.call.shadow
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalCutoutPadding
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalSystemBarsPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val PeekHeight = 48.dp
private val HalfExpandedHeight = 166.dp

private const val ActivityFinishDelay = 3000L

@Composable
internal fun rememberCallScreenState(
    sheetState: BottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Hidden,
        collapsable = true,
        confirmStateChange = { it != BottomSheetValue.Hidden }
    ),
    sheetContentState: BottomSheetContentState = rememberBottomSheetContentState(
        initialSheetComponent = BottomSheetComponent.CallActions,
        initialLineState = LineState.Expanded
    ),
    shouldShowFileShareComponent: Boolean = false,
    systemUiController: SystemUiController = rememberSystemUiController(),
    isDarkMode: Boolean = isSystemInDarkTheme(),
    scope: CoroutineScope = rememberCoroutineScope(),
    density: Density = LocalDensity.current
) = remember(
    sheetState,
    sheetContentState,
    shouldShowFileShareComponent,
    systemUiController,
    isDarkMode,
    scope,
    density
) {
    CallScreenState(
        sheetState = sheetState,
        sheetContentState = sheetContentState,
        shouldShowFileShareComponent = shouldShowFileShareComponent,
        systemUiController = systemUiController,
        isDarkMode = isDarkMode,
        scope = scope,
        density = density
    )
}

internal class CallScreenState(
    val sheetState: BottomSheetState,
    val sheetContentState: BottomSheetContentState,
    val shouldShowFileShareComponent: Boolean,
    private val systemUiController: SystemUiController,
    private val isDarkMode: Boolean,
    private val scope: CoroutineScope,
    density: Density
) {

    private val hasCurrentSheetComponentAppBar by derivedStateOf {
        sheetContentState.currentComponent == BottomSheetComponent.FileShare || sheetContentState.currentComponent == BottomSheetComponent.Whiteboard
    }

    private val isSheetFullScreen by sheetState.isSheetFullScreen(
        offsetThreshold = FullScreenThreshold,
        density = density
    )

    private val isSheetHalfExpanding by sheetState.isHalfExpanding()

    val isSheetHidden by sheetState.isHidden()

    val isSheetNotDraggableDown by sheetState.isNotDraggableDown()

    val isSheetCollapsed by sheetState.isCollapsed()

    val isSheetCollapsing by sheetState.isCollapsing()

    val shouldShowAppBar by derivedStateOf {
        isSheetFullScreen && hasCurrentSheetComponentAppBar
    }

    val shouldShowCallActionsComponent by derivedStateOf {
        isSheetCollapsing || isSheetHalfExpanding
    }

    val statusBarIconsShouldUseSystemMode by derivedStateOf {
        isSheetFullScreen && hasCurrentSheetComponentAppBar
    }

    val shouldEnableSheetGesture by derivedStateOf {
        sheetContentState.currentComponent != BottomSheetComponent.Whiteboard
    }

    fun collapseSheet() {
        scope.launch {
            sheetState.collapse()
        }
    }

    fun hideSheet() {
        scope.launch {
            sheetState.hide()
        }
    }

    fun halfExpandSheet() {
        scope.launch {
            sheetState.halfExpand()
        }
    }

    fun halfExpandSheetIfCollapsed() {
        if (sheetState.isCollapsed) {
            halfExpandSheet()
        }
    }

    fun navigateToCallActionsComponent() {
        sheetContentState.navigateToComponent(BottomSheetComponent.CallActions)
    }

    fun navigateToFileShareComponent() {
        scope.launch {
            sheetContentState.navigateToComponent(BottomSheetComponent.FileShare)
            sheetState.expand()
        }
    }

    fun onCallActionClick(action: CallAction) {
        when (action) {
            is CallAction.Audio, is CallAction.ScreenShare, is CallAction.FileShare, is CallAction.Whiteboard, is CallAction.VirtualBackground -> {
                scope.launch {
                    sheetState.expand()
                }
            }
            else -> Unit
        }
    }

    fun updateStatusBarIcons(useSystemMode: Boolean) {
        systemUiController.statusBarDarkContentEnabled = if (useSystemMode) !isDarkMode else false
    }

    companion object {
        private val FullScreenThreshold = 64.dp
    }
}

@Composable
internal fun CallScreen(
    viewModel: CallViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallViewModel.provideFactory(::requestConfiguration)
    ),
    shouldShowFileShareComponent: Boolean,
    isInPipMode: Boolean,
    onBackPressed: () -> Unit,
    onPipAspectRatio: (Rational) -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onActivityFinish: () -> Unit
) {
    val activity = LocalContext.current.findActivity() as FragmentActivity
    val callUiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Hidden,
        collapsable = !callUiState.isAudioOnly,
        confirmStateChange = { it != BottomSheetValue.Hidden }
    )
    val callScreenState = rememberCallScreenState(
        sheetState = sheetState,
        shouldShowFileShareComponent = shouldShowFileShareComponent
    )
    val permissions by remember(callScreenState) { getPermissions(callUiState) }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions) { permissionsResult ->
        permissionsResult.forEach { (permission, isGranted) ->
            if (!isGranted) {
                viewModel.hangUp()
                return@forEach
            }
            when (permission) {
                RecordAudioPermission -> viewModel.startMicrophone(activity)
                CameraPermission -> viewModel.startCamera(activity)
            }
        }
    }
    val onBackPressedInternal by remember {
        derivedStateOf {
            if (callUiState.fullscreenStream != null) { { viewModel.fullscreenStream(null) } }
            else onBackPressed
        }
    }

    val onFeedbackDismiss = remember(activity) {
        {
            onActivityFinish()
            activity.finishAndRemoveTask()
        }
    }

    LaunchedEffect(onActivityFinish) {
        viewModel.setOnCallEnded {
            onActivityFinish()
            when {
                isInPipMode -> activity.finishAndRemoveTask()
                !callUiState.showFeedback && activity.isAtLeastResumed() -> {
                    delay(ActivityFinishDelay)
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    LaunchedEffect(onPipAspectRatio) {
        viewModel.setOnPipAspectRatio(onPipAspectRatio)
    }

    LaunchedEffect(permissionsState) {
        permissionsState.launchMultiplePermissionRequest()
    }

    CallScreen(
        callUiState = callUiState,
        callScreenState = callScreenState,
        permissionsState = permissionsState,
        onThumbnailStreamClick = viewModel::swapThumbnail,
        onFullscreenStreamClick = viewModel::fullscreenStream,
        onUserFeedback = viewModel::sendUserFeedback,
        onConfigurationChange = viewModel::updateStreamsArrangement,
        onBackPressed = onBackPressedInternal,
        onFeedbackDismiss = onFeedbackDismiss,
        isInPipMode = isInPipMode,
        onFileShareVisibility = onFileShareVisibility,
        onWhiteboardVisibility = onWhiteboardVisibility
    )
}

private fun getPermissions(callUiState: CallUiState): State<List<String>> {
    return derivedStateOf {
        with(callUiState) {
            listOfNotNull(
                if (isMicPermissionRequired) RecordAudioPermission else null,
                if (isCameraPermissionRequired) CameraPermission else null
            )
        }
    }
}

@Composable
internal fun CallScreen(
    callUiState: CallUiState,
    callScreenState: CallScreenState,
    permissionsState: MultiplePermissionsState?,
    onBackPressed: () -> Unit,
    isInPipMode: Boolean = false,
    onConfigurationChange: (Boolean) -> Unit,
    onThumbnailStreamClick: (StreamUi) -> Unit,
    onFullscreenStreamClick: (String?) -> Unit,
    onUserFeedback: (Float, String) -> Unit,
    onFeedbackDismiss: () -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit
) {
    if (isInPipMode) {
        PipScreen(
            stream = callUiState.featuredStreams.value.firstOrNull(),
            callState = callUiState.callState,
            isGroupCall = callUiState.isGroupCall,
            isRecording = callUiState.recording?.isRecording() ?: false
        )
    } else {
        DefaultCallScreen(
            callUiState = callUiState,
            callScreenState = callScreenState,
            permissionsState = permissionsState,
            onBackPressed = onBackPressed,
            onConfigurationChange = onConfigurationChange,
            onThumbnailStreamClick = onThumbnailStreamClick,
            onFullscreenStreamClick = onFullscreenStreamClick,
            onUserFeedback = onUserFeedback,
            onFeedbackDismiss = onFeedbackDismiss,
            onFileShareVisibility = onFileShareVisibility,
            onWhiteboardVisibility = onWhiteboardVisibility
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PipScreen(
    stream: StreamUi?,
    callState: CallStateUi,
    isGroupCall: Boolean,
    isRecording: Boolean
) {
    Box {
        if (stream != null) {
            StreamContainer {
                Stream(
                    streamView = stream.video?.view?.pipSettings(),
                    avatar = stream.avatar,
                    avatarSize = 48.dp,
                    avatarVisible = stream.video == null || !stream.video.isEnabled
                )
            }
        }

        val shadowTextStyle = LocalTextStyle.current.shadow()
        if (callState is CallStateUi.Dialing) {
            EllipsizeText(
                text = stringResource(id = R.string.kaleyra_call_status_ringing),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                ellipsize = Ellipsize.Marquee,
                shadow = shadowTextStyle.shadow,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }

        if (callState is CallStateUi.Ringing) {
            EllipsizeText(
                text = pluralStringResource(id = R.plurals.kaleyra_call_incoming_status_ringing, count = if (isGroupCall) 2 else 1),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                ellipsize = Ellipsize.Marquee,
                shadow = shadowTextStyle.shadow,
                modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp)
            )
        }

        if (callState is CallStateUi.Reconnecting) {
            EllipsizeText(
                text = stringResource(id = R.string.kaleyra_call_offline),
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                ellipsize = Ellipsize.Marquee,
                shadow = shadowTextStyle.shadow,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        if (isRecording) {
            RecordingDot(
                modifier = Modifier
                    .padding(12.dp)
                    .align(Alignment.TopEnd)
            )
        }
    }
}

@Composable
internal fun DefaultCallScreen(
    callUiState: CallUiState,
    callScreenState: CallScreenState,
    permissionsState: MultiplePermissionsState?,
    onBackPressed: () -> Unit,
    onConfigurationChange: (Boolean) -> Unit,
    onThumbnailStreamClick: (StreamUi) -> Unit,
    onFullscreenStreamClick: (String?) -> Unit,
    onUserFeedback: (Float, String) -> Unit,
    onFeedbackDismiss: () -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit
) {
    val configuration = LocalConfiguration.current
    val backgroundAlpha by animateFloatAsState(if (callScreenState.isSheetCollapsing) 0f else 1f)

    FileShareVisibilityObserver(callScreenState, onFileShareVisibility)

    WhiteboardVisibilityObserver(callScreenState, onWhiteboardVisibility)

    LaunchedEffect(callScreenState) {
        combine(
            snapshotFlow { callScreenState.isSheetNotDraggableDown },
            snapshotFlow { callScreenState.isSheetCollapsed }
        ) { isSheetNotDraggableDown, isSheetCollapsed ->
            with(callScreenState.sheetContentState) {
                if (isSheetNotDraggableDown) collapseLine(color = if (isSheetCollapsed) androidx.compose.ui.graphics.Color.White else null) else expandLine()
            }
        }.launchIn(this)
    }

    LaunchedEffect(callScreenState) {
        snapshotFlow { callScreenState.statusBarIconsShouldUseSystemMode }
            .onEach { callScreenState.updateStatusBarIcons(useSystemMode = it) }
            .launchIn(this)
    }

    LaunchedEffect(callScreenState) {
        snapshotFlow { callScreenState.shouldShowCallActionsComponent }
            .filter { it }
            .onEach { callScreenState.navigateToCallActionsComponent() }
            .launchIn(this)
    }

    if (callScreenState.shouldShowFileShareComponent) {
        LaunchedEffect(Unit) {
            callScreenState.navigateToFileShareComponent()
        }
    }

    LaunchedEffect(callUiState) {
        when {
            callUiState.callState != CallStateUi.Dialing && callUiState.callState != CallStateUi.Connected && callUiState.callState != CallStateUi.Reconnecting -> callScreenState.hideSheet()
            callScreenState.isSheetHidden -> callScreenState.halfExpandSheet()
        }
    }

    when {
        callScreenState.sheetState.isHidden && callUiState.callState is CallStateUi.Disconnected.Ended -> BackHandler(onBack = onBackPressed)
        callScreenState.sheetContentState.currentComponent != BottomSheetComponent.CallActions -> BackHandler(onBack = callScreenState::navigateToCallActionsComponent)
        !callScreenState.isSheetNotDraggableDown -> BackHandler(onBack = callScreenState::collapseSheet)
    }

    BoxWithConstraints(modifier = Modifier.horizontalSystemBarsPadding()) {

        LaunchedEffect(configuration, maxWidth, onConfigurationChange) {
            onConfigurationChange(isAtLeastMediumSizeDevice(maxWidth, maxHeight))
        }

        val navBarsBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetGesturesEnabled = callScreenState.shouldEnableSheetGesture,
            sheetState = callScreenState.sheetState,
            sheetPeekHeight = PeekHeight + navBarsBottomPadding,
            sheetHalfExpandedHeight = HalfExpandedHeight + navBarsBottomPadding,
            sheetElevation = 0.dp,
            anchor = {
                BottomSheetAnchor(callUiState, callScreenState, onThumbnailStreamClick)
            },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    contentState = callScreenState.sheetContentState,
                    permissionsState = permissionsState,
                    onLineClick = callScreenState::halfExpandSheetIfCollapsed,
                    onCallActionClick = callScreenState::onCallActionClick,
                    onAudioDeviceClick = callScreenState::halfExpandSheet,
                    onScreenShareTargetClick = callScreenState::halfExpandSheet,
                    onVirtualBackgroundClick = callScreenState::halfExpandSheet,
                    contentVisible = !callScreenState.isSheetCollapsed,
                    modifier = Modifier.horizontalCutoutPadding()
                )
            },
            content = {
                CallScreenContent(
                    callState = callUiState.callState,
                    maxWidth = maxWidth,
                    onBackPressed = onBackPressed,
                    onStreamFullscreenClick = onFullscreenStreamClick
                )
            }
        )

        CallScreenAppBar(
            currentSheetComponent = callScreenState.sheetContentState.currentComponent,
            visible = callScreenState.shouldShowAppBar,
            onBackPressed = callScreenState::navigateToCallActionsComponent,
        )

        if (callUiState.showFeedback && callUiState.callState is CallStateUi.Disconnected.Ended) {
            val activity = LocalContext.current.findActivity() as ComponentActivity
            if (activity.isAtLeastResumed()) {
                UserFeedback(onUserFeedback = onUserFeedback, onDismiss = onFeedbackDismiss)
            }
        }
    }
}

@Composable
internal fun BottomSheetAnchor(
    callUiState: CallUiState,
    callScreenState: CallScreenState,
    onThumbnailStreamClick: (StreamUi) -> Unit
) {
    val shouldShowThumbnailStreams = !callScreenState.isSheetHidden && callUiState.fullscreenStream == null
    val shouldShowRecordingHint = callUiState.recording != null && callUiState.recording.type != RecordingTypeUi.Never && callUiState.callState == CallStateUi.Dialing

    AnimatedVisibility(
        visible = shouldShowThumbnailStreams,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ThumbnailStreams(
            streams = callUiState.thumbnailStreams,
            contentPadding = PaddingValues(16.dp),
            onStreamClick = onThumbnailStreamClick
        )
    }

    if (shouldShowRecordingHint) {
        HelperText(
            text = stringResource(id = if (callUiState.recording?.type == RecordingTypeUi.OnConnect) R.string.kaleyra_automatic_recording_disclaimer else R.string.kaleyra_manual_recording_disclaimer),
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
internal fun FileShareVisibilityObserver(
    callScreenState: CallScreenState,
    onFileShareVisibility: (Boolean) -> Unit
) {
    val isFileShareDisplayed by remember(callScreenState) {
        derivedStateOf {
            callScreenState.sheetContentState.currentComponent == BottomSheetComponent.FileShare
        }
    }
    LaunchedEffect(callScreenState) {
        snapshotFlow { isFileShareDisplayed }
            .onEach { onFileShareVisibility(it) }
            .launchIn(this)
    }
}

@Composable
internal fun WhiteboardVisibilityObserver(
    callScreenState: CallScreenState,
    onWhiteboardVisibility: (Boolean) -> Unit
) {
    val isWhiteboardDisplayed by remember(callScreenState) {
        derivedStateOf {
            callScreenState.sheetContentState.currentComponent == BottomSheetComponent.Whiteboard
        }
    }
    LaunchedEffect(callScreenState) {
        snapshotFlow { isWhiteboardDisplayed }
            .onEach { onWhiteboardVisibility(it) }
            .launchIn(this)
    }
}

private fun ComponentActivity.isAtLeastResumed() =
    lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CallScreenPreview() {
    KaleyraTheme {
        CallScreen(
            callUiState = CallUiState(
                callState = CallStateUi.Connected,
                featuredStreams = ImmutableList(listOf(streamUiMock)),
                thumbnailStreams = ImmutableList(listOf(streamUiMock))
            ),
            callScreenState = rememberCallScreenState(),
            permissionsState = null,
            onFileShareVisibility = {},
            onWhiteboardVisibility = {},
            onConfigurationChange = {},
            onBackPressed = {},
            onFullscreenStreamClick = {},
            onThumbnailStreamClick = {},
            onFeedbackDismiss = {},
            onUserFeedback = { _,_ -> }
        )
    }
}