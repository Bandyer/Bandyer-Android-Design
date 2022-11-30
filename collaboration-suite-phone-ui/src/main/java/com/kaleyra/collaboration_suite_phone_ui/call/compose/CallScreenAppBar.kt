package com.kaleyra.collaboration_suite_phone_ui.call.compose

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetContentState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.bottomsheet.BottomSheetSection
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.fileshare.view.FileShareAppBarTag
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardAppBar
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.WhiteboardAppBarTag
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalCutoutPadding
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.horizontalSystemBarsPadding

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
            Box(modifier = Modifier.statusBarsPadding()) {
                val appBarsModifier = Modifier.horizontalCutoutPadding()
                when (bottomSheetContentState.currentSection) {
                    BottomSheetSection.FileShare -> FileShareAppBar(
                        onBackPressed = onBackPressed,
                        modifier = appBarsModifier.testTag(FileShareAppBarTag)
                    )
                    BottomSheetSection.Whiteboard -> WhiteboardAppBar(
                        onBackPressed = onBackPressed,
                        onUploadClick = { /*TODO*/ },
                        modifier = appBarsModifier.testTag(WhiteboardAppBarTag)
                    )
                }
            }
        }
    }
}