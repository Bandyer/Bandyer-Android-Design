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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalSystemBarsPadding
import kotlinx.coroutines.CoroutineScope
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
        initialSheetSection = BottomSheetSection.CallActions,
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
internal class CallScreenState(
    val sheetState: BottomSheetState,
    val sheetContentState: BottomSheetContentState,
    val systemUiController: SystemUiController,
    val isDarkMode: Boolean,
    private val scope: CoroutineScope,
    private val density: Density
) {

    val isSheetCollapsed by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed && progress.fraction == 1f }
    }

    val isSheetNotDraggableDown by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed || (targetValue == BottomSheetValue.HalfExpanded && !collapsable) }
    }

    val isSheetCollapsing by derivedStateOf {
        with(sheetState) { targetValue == BottomSheetValue.Collapsed && progress.fraction >= TargetStateFractionThreshold }
    }

    val isApproachingFullScreen by derivedStateOf {
        sheetState.offset.value < with(density) { FullScreenThreshold.toPx() }
    }

    val isSheetLeavingExpandedState by derivedStateOf {
        with(sheetState) {
            (currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.HalfExpanded && progress.fraction >= TargetStateFractionThreshold) ||
                    (currentValue == BottomSheetValue.Expanded && targetValue == BottomSheetValue.Collapsed)
        }
    }

    fun halfExpandSheet() {
        scope.launch { sheetState.halfExpand() }
    }

    fun collapseSheet() {
        scope.launch { sheetState.collapse() }
    }

    companion object {
        private const val TargetStateFractionThreshold = .95f
        private val FullScreenThreshold = 64.dp
    }
}

@Composable
internal fun CallScreen(callScreenState: CallScreenState = rememberCallScreenState()) {
    val backgroundAlpha by animateFloatAsState(if (callScreenState.isSheetCollapsing) 0f else 1f)

    LaunchedEffect(callScreenState) {
        snapshotFlow { callScreenState.isApproachingFullScreen }
            .onEach { callScreenState.systemUiController.statusBarDarkContentEnabled = if (it) !callScreenState.isDarkMode else false }
            .launchIn(this)
    }

    LaunchedEffect(callScreenState) {
        snapshotFlow { callScreenState.isSheetLeavingExpandedState }
            .onEach { if (it) callScreenState.sheetContentState.navigateToSection(BottomSheetSection.CallActions) }
            .launchIn(this)
    }

    LaunchedEffect(callScreenState) {
        snapshotFlow { callScreenState.sheetContentState.currentSection }
            .onEach { if (it != BottomSheetSection.CallActions) callScreenState.sheetState.expand() }
            .launchIn(this)
    }

    LaunchedEffect(callScreenState.sheetState, callScreenState.isSheetCollapsed, callScreenState.isSheetNotDraggableDown) {
        when {
            callScreenState.isSheetCollapsed -> callScreenState.sheetContentState.collapseLine(color = Color.White)
            callScreenState.isSheetNotDraggableDown -> callScreenState.sheetContentState.collapseLine()
            else -> callScreenState.sheetContentState.expandLine()
        }
    }

    when {
        callScreenState.sheetContentState.currentSection != BottomSheetSection.CallActions -> BackPressHandler(onBackPressed = { callScreenState.sheetContentState.navigateToSection(BottomSheetSection.CallActions) })
        callScreenState.sheetState.collapsable && !callScreenState.sheetState.isCollapsed -> BackPressHandler(onBackPressed = callScreenState::collapseSheet)
        !callScreenState.sheetState.collapsable && !callScreenState.sheetState.isHalfExpanded -> BackPressHandler(onBackPressed = callScreenState::halfExpandSheet)
    }

    Box(modifier = Modifier.horizontalSystemBarsPadding()) {
        val navBarsBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
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
                    onLineClick = {
                        if (callScreenState.sheetState.isCollapsed) {
                            callScreenState.halfExpandSheet()
                        }
                    },
                    contentVisible = !callScreenState.isSheetCollapsed,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                )
            },
            content = { CallScreenContent(callScreenState.sheetState, it) }
        )

        CallScreenAppBar(
            bottomSheetContentState = callScreenState.sheetContentState,
            visible = callScreenState.isApproachingFullScreen,
            onBackPressed = callScreenState::halfExpandSheet
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