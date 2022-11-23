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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.core.view.subfeaturelayout.SubFeatureLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun WhiteboardSection(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onReloadClick: () -> Unit,
    onBackPressed: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WhiteboardSection(
        uiState = uiState,
        onReloadClick = onReloadClick,
        onBackPressed = onBackPressed,
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardSection(
    uiState: WhiteboardUiState,
    onReloadClick: () -> Unit,
    onBackPressed: () -> Unit
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
            SubFeatureLayout(
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
internal fun WhiteboardSectionPreview() {
    WhiteboardSectionPreview(
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
    WhiteboardSectionPreview(uiState = WhiteboardUiState(isOffline = true))
}

@Composable
private fun WhiteboardSectionPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardSection(
                uiState = uiState,
                onReloadClick = {},
                onBackPressed = {}
            )
        }
    }
}