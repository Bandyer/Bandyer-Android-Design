package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContent
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetScaffold
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetValue
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.rememberBottomSheetState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isCollapsed
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isCollapsing
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isExpandingToFullScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isHalfExpanding
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.launch

@Composable
fun CallScreen() {
    val sheetState = rememberBottomSheetState(
        initialValue = BottomSheetValue.HalfExpanded,
        collapsable = true
    )
    val bottomSheetContentState = rememberBottomSheetContentState(
        initialSheetSection = BottomSheetSection.CallActions,
        initialLineState = LineState.Expanded
    )
    CallScreen(
        sheetState = sheetState,
        bottomSheetContentState = bottomSheetContentState
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun CallScreen(
    sheetState: BottomSheetState,
    bottomSheetContentState: BottomSheetContentState
) {
    val isDarkTheme = isSystemInDarkTheme()

    val scope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()

    val isFullScreen = sheetState.isExpandingToFullScreen(LocalDensity.current)
    val isCollapsing = sheetState.isCollapsing()
    val isHalfExpanding = sheetState.isHalfExpanding()

    val backgroundAlpha by animateFloatAsState(if (isCollapsing) 0f else 1f)

    LaunchedEffect(isFullScreen) {
        systemUiController.statusBarDarkContentEnabled = if (isFullScreen) !isDarkTheme else false
    }

    LaunchedEffect(isHalfExpanding) {
        if (bottomSheetContentState.currentSection != BottomSheetSection.CallActions && isHalfExpanding) {
            bottomSheetContentState.navigateToSection(BottomSheetSection.CallActions)
        }
    }

    LaunchedEffect(bottomSheetContentState.currentSection) {
        if (bottomSheetContentState.currentSection != BottomSheetSection.CallActions) sheetState.expand()
    }

    LaunchedEffect(sheetState.targetValue, sheetState.progress.fraction) {
        when {
            sheetState.isCollapsed() -> bottomSheetContentState.collapseLine(color = Color.White)
            sheetState.isNotDraggableDown() -> bottomSheetContentState.collapseLine()
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

    Box {
        BottomSheetScaffold(
            modifier = Modifier.fillMaxSize(),
            sheetState = sheetState,
            sheetPeekHeight = 48.dp,
            sheetHalfExpandedHeight = 166.dp,
            anchor = { },
            sheetBackgroundColor = MaterialTheme.colors.surface.copy(alpha = backgroundAlpha),
            sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            backgroundColor = Color.Black,
            contentColor = Color.White,
            sheetContent = {
                BottomSheetContent(
                    sheetState = sheetState,
                    contentState = bottomSheetContentState,
                    onLineClick = {
                        if (sheetState.isCollapsed) {
                            halfExpandBottomSheet()
                        }
                    },
                )
            },
            content = { CallScreenContent(sheetState, it) }
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