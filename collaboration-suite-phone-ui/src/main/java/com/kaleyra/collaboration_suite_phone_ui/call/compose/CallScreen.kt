package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalSystemBarsPadding
import kotlinx.coroutines.launch

private val PeekHeight = 48.dp
private val HalfExpandedHeight = 166.dp

@Composable
internal fun CallScreen(
    sheetState: BottomSheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.HalfExpanded,
        collapsable = true
    ),
    bottomSheetContentState: BottomSheetContentState = rememberBottomSheetContentState(
        initialSheetSection = BottomSheetSection.CallActions,
        initialLineState = LineState.Expanded
    ),
    systemUiController: SystemUiController = rememberSystemUiController()
) {
    val isDarkTheme = isSystemInDarkTheme()

    val scope = rememberCoroutineScope()

    val isCollapsed = sheetState.isCollapsed()
    val isNotDraggableDown = sheetState.isNotDraggableDown()
    val isFullScreen = sheetState.isExpandingToFullScreen(LocalDensity.current)
    val isCollapsing = sheetState.isCollapsing()
    val isHalfExpanding = sheetState.isExitingExpandedState()

    val backgroundAlpha by animateFloatAsState(if (isCollapsing) 0f else 1f)

    LaunchedEffect(sheetState, isFullScreen) {
        systemUiController.statusBarDarkContentEnabled = if (isFullScreen) !isDarkTheme else false
    }

    LaunchedEffect(sheetState, isHalfExpanding) {
        if (isHalfExpanding) {
            bottomSheetContentState.navigateToSection(BottomSheetSection.CallActions)
        }
    }

    LaunchedEffect(bottomSheetContentState, bottomSheetContentState.currentSection) {
        if (bottomSheetContentState.currentSection != BottomSheetSection.CallActions) sheetState.expand()
    }

    LaunchedEffect(sheetState, isCollapsed, isNotDraggableDown) {
        when {
            isCollapsed -> bottomSheetContentState.collapseLine(color = Color.White)
            isNotDraggableDown -> bottomSheetContentState.collapseLine()
            else -> bottomSheetContentState.expandLine()
        }
    }

    val halfExpandBottomSheet = remember { { scope.launch { sheetState.halfExpand() } } }

    val collapseBottomSheet = remember { { scope.launch { sheetState.collapse() } } }

    when {
        bottomSheetContentState.currentSection != BottomSheetSection.CallActions -> BackPressHandler(onBackPressed = { bottomSheetContentState.navigateToSection(BottomSheetSection.CallActions) })
        sheetState.collapsable && !sheetState.isCollapsed -> BackPressHandler(onBackPressed = { collapseBottomSheet() })
        !sheetState.collapsable && !sheetState.isHalfExpanded -> BackPressHandler(onBackPressed = { halfExpandBottomSheet() })
    }

    Box(modifier = Modifier.horizontalSystemBarsPadding()) {
        val navBarsBottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            sheetPeekHeight = PeekHeight + navBarsBottomPadding,
            sheetHalfExpandedHeight = HalfExpandedHeight + navBarsBottomPadding,
            anchor = { },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    contentState = bottomSheetContentState,
                    onLineClick = {
                        if (sheetState.isCollapsed) {
                            halfExpandBottomSheet()
                        }
                    },
                    contentVisible = !sheetState.isCollapsed(),
                    modifier = Modifier.windowInsetsPadding(WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal))
                )
            },
            content = {
                CallScreenContent(sheetState, it)
            }
        )

        CallScreenAppBar(
            bottomSheetContentState = bottomSheetContentState,
            visible = isFullScreen,
            onBackPressed = { halfExpandBottomSheet() }
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