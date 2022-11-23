package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.appbar.CallAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardAppBar

@Composable
internal fun CallScreenAppBar(
    bottomSheetContentState: BottomSheetContentState,
    visible: Boolean,
    onBackPressed: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Surface(elevation = AppBarDefaults.TopAppBarElevation) {
            Spacer(
                Modifier
                    .background(MaterialTheme.colors.primary)
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
            )
            Column(Modifier.statusBarsPadding()) {
                when (bottomSheetContentState.currentSection) {
                    BottomSheetSection.FileShare -> FileShareAppBar(onBackPressed = onBackPressed)
                    BottomSheetSection.Whiteboard -> WhiteboardAppBar(onBackPressed = onBackPressed, onUploadClick = { /*TODO*/ })
                    else -> CallAppBar(onBackPressed = onBackPressed, title = "")
                }
            }
        }
    }
}