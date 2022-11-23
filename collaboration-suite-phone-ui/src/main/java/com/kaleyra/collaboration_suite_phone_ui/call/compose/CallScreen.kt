package com.kaleyra.collaboration_suite_phone_ui.call.compose

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isCollapsing
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isExpandingToFullScreen
import com.kaleyra.collaboration_suite_phone_ui.call.compose.extensions.isHalfExpanding
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.launch

@Composable
fun CallScreen() {
    val isDarkTheme = isSystemInDarkTheme()

    val scope = rememberCoroutineScope()
    val systemUiController = rememberSystemUiController()
    val sheetState = rememberBottomSheetState(initialValue = BottomSheetValue.HalfExpanded, collapsable = true)
    val bottomSheetContentState = rememberBottomSheetContentState(BottomSheetSection.CallActions)

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
            sheetContent = { BottomSheetContent(contentState = bottomSheetContentState, sheetState = sheetState) },
            content = { CallScreenContent(sheetState, it) }
        )

        CallScreenAppBar(
            bottomSheetContentState = bottomSheetContentState,
            visible = isFullScreen,
            onBackPressed = {
                scope.launch {
                    sheetState.halfExpand()
                }
            }
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
fun CallScreenPreview() {
    KaleyraTheme {
        CallScreen()
    }
}