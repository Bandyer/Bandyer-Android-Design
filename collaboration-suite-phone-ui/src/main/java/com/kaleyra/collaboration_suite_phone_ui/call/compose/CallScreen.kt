package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_phone_ui.call.compose.callactions.model.CallAction
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
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
        initialValue = BottomSheetValue.HalfExpanded,
        collapsable = true
    ),
    sheetContentState: BottomSheetContentState = rememberBottomSheetContentState(
        initialSheetComponent = BottomSheetComponent.CallActions,
        initialLineState = LineState.Expanded
    ),
    systemUiController: SystemUiController = rememberSystemUiController(),
    isDarkMode: Boolean = isSystemInDarkTheme(),
    scope: CoroutineScope = rememberCoroutineScope(),
    density: Density = LocalDensity.current
) = remember(sheetState, sheetContentState, systemUiController, isDarkMode, scope, density) {
    CallScreenState(
        sheetState = sheetState,
        sheetContentState = sheetContentState,
        systemUiController = systemUiController,
        isDarkMode = isDarkMode,
        scope = scope,
        density = density
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Stable
internal class CallScreenState(
    val sheetState: BottomSheetState,
    val sheetContentState: BottomSheetContentState,
    private val systemUiController: SystemUiController,
    private val isDarkMode: Boolean,
    private val scope: CoroutineScope,
    private val density: Density
) {

    val currentSheetComponent by derivedStateOf {
        sheetContentState.currentComponent
    }

    val shouldShowAppBar by derivedStateOf {
        isSheetFullScreen && hasCurrentSheetComponentAppBar
    }

    val isSheetCollapsed by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed && progress.fraction == 1f }
    }

    val isSheetNotDraggableDown by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed || (targetValue == BottomSheetValue.HalfExpanded && !isCollapsable) }
    }

    val isSheetCollapsing by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed && progress.fraction >= TargetStateFractionThreshold }
    }

    val shouldShowCallActionsComponent by derivedStateOf {
        with(sheetState) {
            (targetValue == BottomSheetValue.HalfExpanded || targetValue == BottomSheetValue.Collapsed) && progress.fraction >= TargetStateFractionThreshold
        }
    }

    val statusBarIconsShouldUseSystemMode by derivedStateOf {
        isSheetFullScreen && hasCurrentSheetComponentAppBar
    }

    private val isSheetFullScreen by derivedStateOf {
        sheetState.offset.value < with(density) { FullScreenThreshold.toPx() }
    }

    private val hasCurrentSheetComponentAppBar by derivedStateOf {
        currentSheetComponent == BottomSheetComponent.FileShare || currentSheetComponent == BottomSheetComponent.Whiteboard
    }

    fun navigateToCallActionsComponent() {
        sheetContentState.navigateToComponent(BottomSheetComponent.CallActions)
    }

    fun halfExpandSheetIfCollapsed() {
        if (isSheetCollapsed) {
            halfExpandSheet()
        }
    }

    fun expandSheetLine() {
        sheetContentState.expandLine()
    }

    fun collapseSheetLine(color: Color? = null) {
        sheetContentState.collapseLine(color)
    }

    fun expandSheet() {
        scope.launch { sheetState.expand() }
    }

    fun halfExpandSheet() {
        scope.launch { sheetState.halfExpand() }
    }

    fun collapseSheet() {
        scope.launch { sheetState.collapse() }
    }

    // TODO add tests to cover all cases
    fun onCallActionClick(action: CallAction) {
        when (action) {
            is CallAction.Audio, is CallAction.ScreenShare, is CallAction.FileShare, is CallAction.Whiteboard -> expandSheet()
            else -> Unit
        }
    }

    fun updateStatusBarIcons(useSystemMode: Boolean) {
        systemUiController.statusBarDarkContentEnabled = if (useSystemMode) !isDarkMode else false
    }

    companion object {
        private const val TargetStateFractionThreshold = .99f
        private val FullScreenThreshold = 64.dp
    }
}

@Composable
internal fun CallScreen(callScreenState: CallScreenState = rememberCallScreenState()) {
    val backgroundAlpha by animateFloatAsState(if (callScreenState.isSheetCollapsing) 0f else 1f)

    LaunchedEffect(callScreenState) {
        combine(
            snapshotFlow { callScreenState.isSheetNotDraggableDown },
            snapshotFlow { callScreenState.isSheetCollapsed }
        ) { isSheetNotDraggableDown, isSheetCollapsed ->
            if (isSheetNotDraggableDown) callScreenState.collapseSheetLine(color = if (isSheetCollapsed) Color.White else null)
            else callScreenState.expandSheetLine()
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

    when {
        callScreenState.currentSheetComponent != BottomSheetComponent.CallActions -> BackPressHandler(onBackPressed = callScreenState::navigateToCallActionsComponent)
        !callScreenState.isSheetNotDraggableDown -> BackPressHandler(onBackPressed = callScreenState::collapseSheet)
    }

    Box(modifier = Modifier.horizontalSystemBarsPadding()) {
        val navBarsBottomPadding =
            WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = callScreenState.sheetState,
            sheetPeekHeight = PeekHeight + navBarsBottomPadding,
            sheetHalfExpandedHeight = HalfExpandedHeight + navBarsBottomPadding,
            anchor = { },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
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
                    contentVisible = !callScreenState.isSheetCollapsed,
                    modifier = Modifier.windowInsetsPadding(
                        WindowInsets.displayCutout.only(
                            WindowInsetsSides.Horizontal
                        )
                    )
                )
            },
            content = { CallScreenContent(callScreenState, it) }
        )

        CallScreenAppBar(
            currentSheetComponent = callScreenState.currentSheetComponent,
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
        CallScreen()
    }
}