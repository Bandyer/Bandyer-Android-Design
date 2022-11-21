package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.submenulayout.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun WhiteboardScreen(
    viewModel: WhiteboardViewModel,
    onBackPressed: () -> Unit,
    onReloadClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WhiteboardScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onReloadClick = onReloadClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardScreen(
    uiState: WhiteboardUiState,
    onBackPressed: () -> Unit,
    onReloadClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { WhiteboardModalBottomSheetContent(sheetState = sheetState) },
        modifier = Modifier.fillMaxSize(),
        content = {
            SubMenuLayout(
                title = stringResource(id = R.string.kaleyra_whiteboard),
                onCloseClick = onBackPressed
            ) {
                if (uiState.isOffline) {
                    WhiteboardOfflineContent(
                        loading = uiState.isLoading,
                        onReloadClick = onReloadClick
                    )
                } else {
                    WhiteboardContent(loading = uiState.isLoading, upload = uiState.upload)
                }
            }
        }
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardScreenPreview() {
    WhiteboardScreenPreview(
        uiState = WhiteboardUiState(
            isLoading = true,
            upload = WhiteboardUploadUi.Uploading(.7f)
        )
    )
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardScreenOfflinePreview() {
    WhiteboardScreenPreview(uiState = WhiteboardUiState(isOffline = true))
}

@Composable
private fun WhiteboardScreenPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardScreen(
                uiState = uiState,
                onBackPressed = {},
                onReloadClick = {}
            )
        }
    }
}