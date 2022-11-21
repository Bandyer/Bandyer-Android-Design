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
import com.kaleyra.collaboration_suite_phone_ui.call.compose.SubMenuLayout
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUpload
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
        onCloseClick = onBackPressed,
        onReloadClick = onReloadClick
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardScreen(
    uiState: WhiteboardUiState,
    onCloseClick: () -> Unit,
    onReloadClick: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetContent = { WhiteboardModalBottomSheetContent(sheetState = sheetState) },
        modifier = Modifier.fillMaxSize()
    ) {
        SubMenuLayout(
            title = stringResource(id = R.string.kaleyra_whiteboard),
            onCloseClick = onCloseClick
        ) {
            WhiteboardContent(
                offline = uiState.isOffline,
                loading = uiState.isLoading,
                upload = uiState.upload,
                onReloadClick = onReloadClick
            )
        }
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardScreenPreview() {
    KaleyraTheme {
        Surface {
            WhiteboardScreen(
                uiState = WhiteboardUiState(isLoading = true, upload = WhiteboardUpload.Uploading(.7f)),
                onCloseClick = {},
                onReloadClick = {}
            )
        }
    }
}