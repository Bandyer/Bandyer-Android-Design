package com.kaleyra.collaboration_suite_phone_ui.call.whiteboard

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import com.kaleyra.collaboration_suite.whiteboard.WhiteboardView
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.common.spacer.NavigationBarsSpacer
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.model.WhiteboardUiState
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.TextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.TextEditorValue
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardContent
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardModalBottomSheetContent
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.WhiteboardOfflineContent
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.view.rememberTextEditorState
import com.kaleyra.collaboration_suite_phone_ui.call.whiteboard.viewmodel.WhiteboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardComponent(
    viewModel: WhiteboardViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = WhiteboardViewModel.provideFactory(::requestConfiguration, WhiteboardView(LocalContext.current))
    ),
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(initialValue = null)

    WhiteboardComponent(
        uiState = uiState,
        editorSheetState = sheetState,
        textEditorState = textEditorState,
        userMessage = userMessage,
        onReloadClick = viewModel::onReloadClick,
        onTextDismissed = viewModel::onTextDismissed,
        onTextConfirmed = viewModel::onTextConfirmed,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardComponent(
    uiState: WhiteboardUiState,
    editorSheetState: ModalBottomSheetState,
    textEditorState: TextEditorState,
    userMessage: UserMessage? = null,
    onReloadClick: () -> Unit,
    onTextDismissed: () -> Unit,
    onTextConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val shouldShowTextEditor by rememberUpdatedState(newValue = uiState.text != null)
    LaunchedEffect(shouldShowTextEditor, editorSheetState) {
        if (shouldShowTextEditor) {
            textEditorState.type(TextFieldValue(uiState.text ?: ""))
            editorSheetState.show()
        } else {
            editorSheetState.hide()
            textEditorState.clearState()
        }
    }

    when {
        textEditorState.currentValue != TextEditorValue.Empty -> BackHandler(onBack = { if (textEditorState.cancel()) onTextDismissed() })
        editorSheetState.currentValue != ModalBottomSheetValue.Hidden -> BackHandler(onBack = onTextDismissed)
    }

    ModalBottomSheetLayout(
        sheetState = editorSheetState,
        modifier = modifier.statusBarsPadding(),
        sheetContent = {
            WhiteboardModalBottomSheetContent(
                textEditorState = textEditorState,
                onTextDismissed = onTextDismissed,
                onTextConfirmed = onTextConfirmed,
                modifier = Modifier
                    .navigationBarsPadding()
                    // Disable gestures on the modal bottom sheet
                    .pointerInput(Unit) {
                        detectDragGestures { _, _ -> }
                    }
            )
        },
        content = {
            Box {
                Column {
                    val contentModifier = Modifier
                        .weight(1f)
                        .background(color = colorResource(id = R.color.kaleyra_color_loading_whiteboard_background))
                    when {
                        uiState.isOffline -> {
                            WhiteboardOfflineContent(
                                loading = uiState.isLoading,
                                onReloadClick = onReloadClick,
                                modifier = contentModifier
                            )
                        }

                        uiState.whiteboardView != null -> {
                            WhiteboardContent(
                                whiteboardView = uiState.whiteboardView,
                                loading = uiState.isLoading,
                                upload = uiState.upload,
                                modifier = contentModifier
                            )
                        }
                    }

                    NavigationBarsSpacer()
                }

                UserMessageSnackbarHandler(userMessage = userMessage)
            }
        },
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun WhiteboardComponentPreview(uiState: WhiteboardUiState) {
    KaleyraTheme {
        Surface {
            WhiteboardComponent(
                uiState = uiState,
                editorSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
                textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty),
                onReloadClick = {},
                onTextDismissed = {},
                onTextConfirmed = {}
            )
        }
    }
}