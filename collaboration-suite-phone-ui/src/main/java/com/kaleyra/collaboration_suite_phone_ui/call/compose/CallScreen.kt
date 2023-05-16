package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import android.graphics.Rect
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ConfigurationExtensions.isAtLeastMediumSizeDevice
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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.CameraPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.MultiplePermissionsState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.RecordAudioPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.findActivity
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.rememberMultiplePermissionsState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.Stream
import com.kaleyra.collaboration_suite_phone_ui.call.compose.streams.StreamContainer
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalCutoutPadding
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalSystemBarsPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val PeekHeight = 48.dp
private val HalfExpandedHeight = 166.dp

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
    onFirstStreamPositioned: (Rect) -> Unit,
    onFileShareDisplayed: () -> Unit
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
            when {
                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
            }
        }
    }
    val onBackPressedInternal by remember {
        derivedStateOf {
            if (callUiState.fullscreenStream != null) { { viewModel.fullscreenStream(null) } }
            else onBackPressed
        }
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
        onBackPressed = onBackPressedInternal,
        isInPipMode = isInPipMode,
        onFileShareDisplayed = onFileShareDisplayed,
        onPipStreamPositioned = onFirstStreamPositioned,
        onConfigurationChange = viewModel::updateStreamsArrangement
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
    onPipStreamPositioned: (Rect) -> Unit,
    onFileShareDisplayed: () -> Unit
) {
    if (isInPipMode) {
        val firstFeaturedStream = callUiState.featuredStreams.value.getOrNull(0)
        if (firstFeaturedStream != null) {
            StreamContainer {
                Stream(
                    streamView = firstFeaturedStream.video?.view,
                    avatar = firstFeaturedStream.avatar,
                    avatarSize = 48.dp,
                    avatarVisible = firstFeaturedStream.video == null || !firstFeaturedStream.video.isEnabled
                )
            }
        }
    } else {
        val configuration = LocalConfiguration.current
        val backgroundAlpha by animateFloatAsState(if (callScreenState.isSheetCollapsing) 0f else 1f)

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
                onFileShareDisplayed()
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
                    AnimatedVisibility(
                        visible = !callScreenState.isSheetHidden && callUiState.fullscreenStream == null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        ThumbnailStreams(
                            streams = callUiState.thumbnailStreams,
                            contentPadding = PaddingValues(16.dp),
                            onStreamClick = onThumbnailStreamClick
                        )
                    }
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
                        onStreamFullscreenClick = onFullscreenStreamClick,
                        onPipStreamPositioned = onPipStreamPositioned
                    )
                }
            )

            CallScreenAppBar(
                currentSheetComponent = callScreenState.sheetContentState.currentComponent,
                visible = callScreenState.shouldShowAppBar,
                onBackPressed = callScreenState::navigateToCallActionsComponent,
            )
        }
    }

}

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
            onFileShareDisplayed = {},
            onConfigurationChange = {},
            onBackPressed = {},
            onFullscreenStreamClick = {},
            onThumbnailStreamClick = {},
            onPipStreamPositioned = {}
        )
    }
}