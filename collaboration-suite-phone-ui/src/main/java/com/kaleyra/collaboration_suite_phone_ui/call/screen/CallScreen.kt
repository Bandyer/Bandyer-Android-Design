package com.kaleyra.collaboration_suite_phone_ui.call.screen

import android.util.Rational
import android.view.View
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetContent
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetScaffold
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.LineState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.rememberBottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.bottomsheet.rememberBottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.countdowntimer.rememberCountdownTimerState
import com.kaleyra.collaboration_suite_phone_ui.call.feedback.UserFeedbackDialog
import com.kaleyra.collaboration_suite_phone_ui.call.helpertext.HelperText
import com.kaleyra.collaboration_suite_phone_ui.call.kicked.KickedMessageDialog
import com.kaleyra.collaboration_suite_phone_ui.call.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.recording.view.RecordingLabel
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallStateUi
import com.kaleyra.collaboration_suite_phone_ui.call.screen.model.CallUiState
import com.kaleyra.collaboration_suite_phone_ui.call.screen.view.CallScreenAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.screen.view.CallScreenContent
import com.kaleyra.collaboration_suite_phone_ui.call.screen.viewmodel.CallViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.stream.view.core.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.stream.view.core.StreamContainer
import com.kaleyra.collaboration_suite_phone_ui.call.stream.view.thumbnail.ThumbnailStreams
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isCollapsed
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isCollapsing
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isHalfExpanding
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isHidden
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isNotDraggableDown
import com.kaleyra.collaboration_suite_phone_ui.call.utils.BottomSheetStateExtensions.isSheetFullScreen
import com.kaleyra.collaboration_suite_phone_ui.call.utils.CameraPermission
import com.kaleyra.collaboration_suite_phone_ui.call.utils.ConfigurationExtensions.isAtLeastMediumSizeDevice
import com.kaleyra.collaboration_suite_phone_ui.call.utils.RecordAudioPermission
import com.kaleyra.collaboration_suite_phone_ui.call.utils.StreamViewSettings.pipSettings
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.text.Ellipsize
import com.kaleyra.collaboration_suite_phone_ui.common.text.EllipsizeText
import com.kaleyra.collaboration_suite_phone_ui.extensions.ContextExtensions.findActivity
import com.kaleyra.collaboration_suite_phone_ui.extensions.ModifierExtensions.horizontalCutoutPadding
import com.kaleyra.collaboration_suite_phone_ui.extensions.ModifierExtensions.horizontalSystemBarsPadding
import com.kaleyra.collaboration_suite_phone_ui.extensions.TextStyleExtensions.shadow
import com.kaleyra.collaboration_suite_phone_ui.theme.CollaborationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val PeekHeight = 48.dp
private val HalfExpandedHeight = 166.dp

private const val ActivityFinishDelay = 1100L
private const val ActivityFinishErrorDelay = 1500L

const val BottomSheetAutoHideMs = 9000L

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
        if (sheetState.isCollapsed) return
        scope.launch {
            sheetState.collapse()
        }
    }

    fun hideSheet() {
        if (sheetState.isHidden) return
        scope.launch {
            sheetState.hide()
        }
    }

    fun halfExpandSheet() {
        if (sheetState.isHalfExpanded) return
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

            else                                                                                                                               -> Unit
        }
    }

    fun updateStatusBarIcons(useSystemMode: Boolean) {
        systemUiController.statusBarDarkContentEnabled = if (useSystemMode) !isDarkMode else false
    }

    companion object {
        private val FullScreenThreshold = 64.dp
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun CallScreen(
    viewModel: CallViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = CallViewModel.provideFactory(::requestConfiguration)
    ),
    shouldShowFileShareComponent: Boolean,
    isInPipMode: Boolean,
    enterPip: () -> Unit,
    onPipAspectRatio: (Rational) -> Unit,
    onDisplayMode: (CallUI.DisplayMode) -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    onActivityFinishing: () -> Unit
) {
    val theme by viewModel.theme.collectAsStateWithLifecycle(CompanyUI.Theme())
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
    // Needed this to handle properly a sequence of multiple permission
    // Cannot call micPermissionState.launchPermission followed by cameraPermissionState.launchPermission, or vice versa
    val inputPermissionsState = rememberMultiplePermissionsState(permissions = listOf(RecordAudioPermission, CameraPermission)) { permissionsResult ->
        permissionsResult.forEach { (permission, isGranted) ->
            when {
                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
            }
        }
    }
    val micPermissionState = rememberPermissionState(permission = RecordAudioPermission) { isGranted ->
        if (isGranted) viewModel.startMicrophone(activity)
    }
    val cameraPermissionState = rememberPermissionState(permission = CameraPermission) { isGranted ->
        if (isGranted) viewModel.startCamera(activity)
    }
    val finishActivity = remember(activity) {
        {
            onActivityFinishing()
            activity.finishAndRemoveTask()
        }
    }
    // code executed when pressing the back button in the call ui
    // the system back gesture or system back button are handled using the BackHandler composable
    val onBackPressed by remember(finishActivity, enterPip) {
        derivedStateOf {
            {
                when {
                    callUiState.callState is CallStateUi.Disconnected.Ended -> finishActivity()
                    callUiState.fullscreenStream != null                    -> {
                        viewModel.fullscreenStream(null)
                    }

                    else                                                    -> enterPip()
                }
            }
        }
    }

    LaunchedEffect(isInPipMode, onActivityFinishing) {
        viewModel.setOnCallEnded { hasFeedback, hasErrorOccurred, hasBeenKicked ->
            onActivityFinishing()
            when {
                isInPipMode || !activity.isAtLeastResumed() -> activity.finishAndRemoveTask()
                !hasFeedback && !hasBeenKicked              -> {
                    val delayMs = if (hasErrorOccurred) ActivityFinishErrorDelay else ActivityFinishDelay
                    delay(delayMs)
                    activity.finishAndRemoveTask()
                }
            }
        }
    }

    LaunchedEffect(onPipAspectRatio) {
        viewModel.setOnPipAspectRatio(onPipAspectRatio)
    }

    LaunchedEffect(onDisplayMode) {
        viewModel.setOnDisplayMode(onDisplayMode)
    }

    LaunchedEffect(inputPermissionsState) {
        viewModel.setOnAudioOrVideoChanged { isAudioEnabled, isVideoEnabled ->
            when {
                isAudioEnabled && isVideoEnabled -> inputPermissionsState.launchMultiplePermissionRequest()
                isAudioEnabled -> micPermissionState.launchPermissionRequest()
                isVideoEnabled -> cameraPermissionState.launchPermissionRequest()
            }
        }
    }

    CollaborationTheme(theme = theme, transparentSystemBars = true) { isDarkTheme ->
        CallScreen(
            callUiState = callUiState,
            callScreenState = callScreenState,
            onThumbnailStreamClick = viewModel::swapThumbnail,
            onThumbnailStreamDoubleClick = viewModel::fullscreenStream,
            onFullscreenStreamClick = viewModel::fullscreenStream,
            onUserFeedback = viewModel::sendUserFeedback,
            onConfigurationChange = viewModel::updateStreamsArrangement,
            onBackPressed = onBackPressed,
            onCallEndedBack = finishActivity,
            isInPipMode = isInPipMode,
            isDarkTheme = isDarkTheme,
            onFileShareVisibility = onFileShareVisibility,
            onWhiteboardVisibility = onWhiteboardVisibility
        )
    }
}

@Composable
internal fun CallScreen(
    callUiState: CallUiState,
    callScreenState: CallScreenState,
    onBackPressed: () -> Unit,
    isInPipMode: Boolean = false,
    isDarkTheme: Boolean = false,
    onConfigurationChange: (Boolean) -> Unit,
    onThumbnailStreamClick: (String) -> Unit,
    onThumbnailStreamDoubleClick: (String) -> Unit,
    onFullscreenStreamClick: (String?) -> Unit,
    onUserFeedback: (Float, String) -> Unit,
    onCallEndedBack: () -> Unit,
    onFileShareVisibility: (Boolean) -> Unit,
    onWhiteboardVisibility: (Boolean) -> Unit,
    isTesting: Boolean = false
) {
    FileShareVisibilityObserver(callScreenState, onFileShareVisibility)

    WhiteboardVisibilityObserver(callScreenState, onWhiteboardVisibility)

    LaunchedEffect(callScreenState) {
        combine(
            snapshotFlow { callScreenState.isSheetNotDraggableDown },
            snapshotFlow { callScreenState.isSheetCollapsed }
        ) { isSheetNotDraggableDown, isSheetCollapsed ->
            with(callScreenState.sheetContentState) {
                if (isSheetNotDraggableDown) collapseLine(color = if (isSheetCollapsed) Color.White else null) else expandLine()
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
            callUiState.callState != CallStateUi.Dialing && callUiState.callState != CallStateUi.Connected && callUiState.callState != CallStateUi.Reconnecting && callUiState.callState != CallStateUi.Ringing(
                true
            ) -> callScreenState.hideSheet()
            callScreenState.isSheetHidden                                                                                                                                                                             -> callScreenState.halfExpandSheet()
        }
    }

    if (callUiState.shouldAutoHideSheet) {
        val enable by remember(callScreenState) {
            derivedStateOf {
                callScreenState.sheetContentState.currentComponent == BottomSheetComponent.CallActions && callScreenState.sheetState.isHalfExpanded
            }
        }
        val timer by rememberCountdownTimerState(initialMillis = BottomSheetAutoHideMs, enable = enable)
        LaunchedEffect(Unit) {
            snapshotFlow { timer }
                .onEach { timer ->
                    if (timer != 0L) return@onEach
                    callScreenState.collapseSheet()
                }.launchIn(this)
        }
    }

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
            onBackPressed = onBackPressed,
            onConfigurationChange = onConfigurationChange,
            onThumbnailStreamClick = onThumbnailStreamClick,
            onThumbnailStreamDoubleClick = onThumbnailStreamDoubleClick,
            onFullscreenStreamClick = onFullscreenStreamClick,
            onUserFeedback = onUserFeedback,
            onCallEndedBack = onCallEndedBack,
            isDarkTheme = isDarkTheme,
            isTesting = isTesting
        )
    }
}

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
                    streamView = stream.video?.view?.pipSettings() ?: ImmutableView(View(LocalContext.current)),
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
            RecordingLabel(
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
    onBackPressed: () -> Unit,
    onConfigurationChange: (Boolean) -> Unit,
    onThumbnailStreamClick: (String) -> Unit,
    onThumbnailStreamDoubleClick: (String) -> Unit,
    onFullscreenStreamClick: (String?) -> Unit,
    onUserFeedback: (Float, String) -> Unit,
    onCallEndedBack: () -> Unit,
    isDarkTheme: Boolean,
    isTesting: Boolean = false
) {
    val configuration = LocalConfiguration.current
    val backgroundAlpha by animateFloatAsState(if (callScreenState.isSheetCollapsing) 0f else 1f)

    when {
        callUiState.callState is CallStateUi.Disconnected.Ended -> BackHandler(onBack = onCallEndedBack)
        callScreenState.sheetContentState.currentComponent != BottomSheetComponent.CallActions -> BackHandler(onBack = callScreenState::navigateToCallActionsComponent)
        !callScreenState.isSheetNotDraggableDown -> BackHandler(onBack = callScreenState::collapseSheet)
        callUiState.fullscreenStream != null -> BackHandler(onBack = { onFullscreenStreamClick(null) })
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
                BottomSheetAnchor(callUiState, callScreenState, onThumbnailStreamClick, onThumbnailStreamDoubleClick, modifier = Modifier.horizontalCutoutPadding())
            },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
            sheetContentColor = MaterialTheme.colors.onSurface,
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    contentState = callScreenState.sheetContentState,
                    onLineClick = callScreenState::halfExpandSheetIfCollapsed,
                    onCallActionClick = callScreenState::onCallActionClick,
                    onAudioDeviceClick = callScreenState::halfExpandSheet,
                    onScreenShareTargetClick = callScreenState::halfExpandSheet,
                    onVirtualBackgroundClick = callScreenState::halfExpandSheet,
                    contentVisible = !callScreenState.isSheetCollapsed,
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier.horizontalCutoutPadding(),
                    isTesting = isTesting
                )
            },
            content = {
                val shouldShowUserMessages by remember(callScreenState) {
                    derivedStateOf {
                        callScreenState.sheetContentState.currentComponent.let {
                            it != BottomSheetComponent.FileShare && it != BottomSheetComponent.Whiteboard
                        }
                    }
                }
                CallScreenContent(
                    callState = callUiState.callState,
                    maxWidth = maxWidth,
                    onBackPressed = onBackPressed,
                    onStreamFullscreenClick = onFullscreenStreamClick,
                    shouldShowUserMessages = shouldShowUserMessages,
                    isDarkTheme = isDarkTheme
                )
            }
        )

        CallScreenAppBar(
            currentSheetComponent = callScreenState.sheetContentState.currentComponent,
            visible = callScreenState.shouldShowAppBar,
            onBackPressed = callScreenState::navigateToCallActionsComponent
        )

        val callState = callUiState.callState
        if (callUiState.showFeedback && (callState is CallStateUi.Disconnected.Ended.HungUp || callState is CallStateUi.Disconnected.Ended.Error)) {
            val activity = LocalContext.current.findActivity() as ComponentActivity
            if (activity.isAtLeastResumed()) {
                UserFeedbackDialog(onUserFeedback = onUserFeedback, onDismiss = onCallEndedBack)
            }
        }

        if (callState is CallStateUi.Disconnected.Ended.Kicked) {
            val activity = LocalContext.current.findActivity() as ComponentActivity
            if (activity.isAtLeastResumed()) {
                KickedMessageDialog(adminName = callState.adminName, onDismiss = onCallEndedBack)
            }
        }
    }
}

@Composable
internal fun BottomSheetAnchor(
    callUiState: CallUiState,
    callScreenState: CallScreenState,
    onThumbnailStreamClick: (String) -> Unit,
    onThumbnailStreamDoubleClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowThumbnailStreams = !callScreenState.isSheetHidden && callUiState.fullscreenStream == null
    val shouldShowRecordingHint = callUiState.recording != null && callUiState.recording.type != RecordingTypeUi.Never && callUiState.callState == CallStateUi.Dialing

    AnimatedVisibility(
        visible = shouldShowThumbnailStreams,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        ThumbnailStreams(
            streams = if (callUiState.callState !is CallStateUi.Ringing && callUiState.callState !is CallStateUi.Dialing) callUiState.thumbnailStreams else ImmutableList(),
            contentPadding = PaddingValues(16.dp),
            onStreamClick = onThumbnailStreamClick,
            onStreamDoubleClick = onThumbnailStreamDoubleClick
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