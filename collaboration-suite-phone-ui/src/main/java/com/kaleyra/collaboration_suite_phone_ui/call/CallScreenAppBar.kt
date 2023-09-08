package com.kaleyra.collaboration_suite_phone_ui.call

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kaleyra.collaboration_suite_phone_ui.call.core.view.bottomsheet.BottomSheetComponent
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.view.FileShareAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardAppBar

const val CallScreenAppBarTag = "CallScreenAppBarTag"

@Composable
internal fun CallScreenAppBar(
    currentSheetComponent: BottomSheetComponent,
    visible: Boolean,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
        modifier = modifier.testTag(CallScreenAppBarTag)
    ) {
        Surface(elevation = AppBarDefaults.TopAppBarElevation) {
            Spacer(
                Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
            Box(modifier = Modifier.statusBarsPadding()) {
                when (currentSheetComponent) {
                    BottomSheetComponent.FileShare -> FileShareAppBar(onBackPressed = onBackPressed)
                    BottomSheetComponent.Whiteboard -> WhiteboardAppBar(onBackPressed = onBackPressed)
                    else -> Unit
                }
            }
        }
    }
}

