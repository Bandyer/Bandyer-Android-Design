package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.RecordAudioPermission
import com.kaleyra.collaboration_suite_phone_ui.call.compose.permission.findActivity
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
    callUiState: CallUiState,
    sheetState: BottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Hidden,
        collapsable = true,
        confirmStateChange = { it != BottomSheetValue.Hidden }
    ),
    sheetContentState: BottomSheetContentState = rememberBottomSheetContentState(
        initialSheetComponent = BottomSheetComponent.CallActions,
        initialLineState = LineState.Expanded
    ),
    systemUiController: SystemUiController = rememberSystemUiController(),
    isDarkMode: Boolean = isSystemInDarkTheme(),
    scope: CoroutineScope = rememberCoroutineScope(),
    density: Density = LocalDensity.current
) = remember(
    callUiState,
    sheetState,
    sheetContentState,
    systemUiController,
    isDarkMode,
    scope,
    density
) {
    CallScreenState(
        callUiState = callUiState,
        sheetState = sheetState,
        sheetContentState = sheetContentState,
        systemUiController = systemUiController,
        isDarkMode = isDarkMode,
        scope = scope,
        density = density
    )
}

internal class CallScreenState(
    val callUiState: CallUiState,
    val sheetState: BottomSheetState,
    val sheetContentState: BottomSheetContentState,
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

    val isSheetNotDraggableDown by sheetState.isNotDraggableDown()

    val isSheetCollapsed by sheetState.isCollapsed()

    val isSheetHidden by sheetState.isHidden()

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

    fun collapseSheet() {
        scope.launch {
            sheetState.collapse()
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

    fun onCallActionClick(action: CallAction) {
        when (action) {
            is CallAction.Audio, is CallAction.ScreenShare, is CallAction.FileShare, is CallAction.Whiteboard -> {
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
    onBackPressed: () -> Unit
) {
    val activity = LocalContext.current.findActivity() as FragmentActivity
    val callUiState by viewModel.uiState.collectAsStateWithLifecycle()
    // TODO link collapsable flag to call's type
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.Hidden,
        collapsable = true,
        confirmStateChange = { it != BottomSheetValue.Hidden }
    )
    val callScreenState = rememberCallScreenState(
        callUiState = callUiState,
        sheetState = sheetState
    )
    val permissions by remember(callScreenState) { getPermissions(callScreenState) }
    val permissionsState = rememberMultiplePermissionsState(permissions = permissions) { permissionsResult ->
        permissionsResult.forEach { (permission, isGranted) ->
            when {
                permission == RecordAudioPermission && isGranted -> viewModel.startMicrophone(activity)
                permission == CameraPermission && isGranted -> viewModel.startCamera(activity)
            }
        }
    }

    LaunchedEffect(permissionsState) {
        permissionsState.launchMultiplePermissionRequest()
    }

    CallScreen(
        callScreenState = callScreenState,
        permissionsState = permissionsState,
        onThumbnailStreamClick = viewModel::swapThumbnail,
        onBackPressed = onBackPressed
    )
}

private fun getPermissions(callScreenState: CallScreenState): State<List<String>> {
    return derivedStateOf {
        with(callScreenState.callUiState) {
            listOfNotNull(
                if (isMicPermissionRequired) RecordAudioPermission else null,
                if (isCameraPermissionRequired) CameraPermission else null
            )
        }
    }
}

@Composable
internal fun CallScreen(
    callScreenState: CallScreenState,
    permissionsState: MultiplePermissionsState,
    onThumbnailStreamClick: (StreamUi) -> Unit,
    onBackPressed: () -> Unit
) {
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

    LaunchedEffect(callScreenState) {
        if (callScreenState.callUiState.callState == CallState.Connected)
            callScreenState.halfExpandSheet()
    }

    when {
        callScreenState.sheetContentState.currentComponent != BottomSheetComponent.CallActions -> BackPressHandler(onBackPressed = callScreenState::navigateToCallActionsComponent)
        !callScreenState.isSheetNotDraggableDown -> BackPressHandler(onBackPressed = callScreenState::collapseSheet)
    }

    // TODO check if I can remove this
    BoxWithConstraints(modifier = Modifier.horizontalSystemBarsPadding()) {
        val navBarsBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = callScreenState.sheetState,
            sheetPeekHeight = PeekHeight + navBarsBottomPadding,
            sheetHalfExpandedHeight = HalfExpandedHeight + navBarsBottomPadding,
            sheetElevation = 0.dp,
            anchor = {
                AnimatedVisibility(
                    visible = !callScreenState.isSheetHidden,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ThumbnailStreams(
                        streams = callScreenState.callUiState.thumbnailStreams,
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
                    contentVisible = !callScreenState.isSheetCollapsed,
                    modifier = Modifier.horizontalCutoutPadding()
                )
            },
            content = {
                CallScreenContent(
                    callState = callScreenState.callUiState.callState,
                    maxWidth = maxWidth,
                    onBackPressed = onBackPressed
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

// TODO add these insets anchor's composable
// val anchorInsets = navigationBarsInsets.only(WindowInsetsSides.Horizontal).add(cutOutInsets)

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CallScreenPreview() {
    KaleyraTheme {
//        CallScreen()
    }
}