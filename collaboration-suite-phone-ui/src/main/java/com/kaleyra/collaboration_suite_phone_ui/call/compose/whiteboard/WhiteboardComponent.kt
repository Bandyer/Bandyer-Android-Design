package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite.phonebox.WhiteboardView
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle

@Composable
internal fun WhiteboardComponent(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestConfiguration)
    ),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    WhiteboardComponent(
        uiState = uiState,
        onReloadClick = viewModel::onReloadClick,
        onTextDismiss = viewModel::onTextDismiss,
        onTextConfirm = viewModel::onTextConfirm,
        onWhiteboardViewCreated = viewModel::onWhiteboardViewCreated,
        onWhiteboardViewDispose = viewModel::onWhiteboardViewDispose,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardComponent(
    uiState: WhiteboardUiState,
    onReloadClick: () -> Unit,
    onTextDismiss: () -> Unit,
    onTextConfirm: (String) -> Unit,
    onWhiteboardViewCreated: (WhiteboardView) -> Unit,
    onWhiteboardViewDispose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    LaunchedEffect(uiState) {
        if (uiState.text != null) sheetState.show()
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        modifier = modifier.statusBarsPadding(),
        sheetContent = {
            WhiteboardModalBottomSheetContent(
                text = uiState.text ?: "",
                sheetState = sheetState,
                onTextEditorDismiss = onTextDismiss,
                onTextConfirmed = onTextConfirm
            )
        },
        content = {
            Column {
                val contentModifier = Modifier
                    .weight(1f)
                    .background(color = colorResource(id = R.color.kaleyra_color_loading_whiteboard_background))
                if (uiState.isOffline) {
                    WhiteboardOfflineContent(
                        loading = uiState.isLoading,
                        onReloadClick = onReloadClick,
                        modifier = contentModifier
                    )
                } else {
                    WhiteboardContent(
                        loading = uiState.isLoading,
                        upload = uiState.upload,
                        onWhiteboardViewCreated = onWhiteboardViewCreated,
                        onWhiteboardViewDispose = onWhiteboardViewDispose,
                        modifier = contentModifier
                    )
                }
                NavigationBarsSpacer()
            }
        }
    )
}


@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun WhiteboardComponentPreview() {
    WhiteboardComponentPreview(
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
    WhiteboardComponentPreview(uiState = WhiteboardUiState(isOffline = true))
}

@Composable
private fun WhiteboardComponentPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardComponent(
                uiState = uiState,
                onReloadClick = {},
                onTextDismiss = {},
                onTextConfirm = {},
                onWhiteboardViewCreated = {},
                onWhiteboardViewDispose = {}
            )
        }
    }
}