package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.TextFieldValue
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardModalBottomSheetContent(
    text: String,
    sheetState: ModalBottomSheetState,
    onTextEditorDismiss: () -> Unit,
    onTextConfirmed: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val closeSheet = remember {
        {
            scope.launch {
                sheetState.hide()
                focusManager.clearFocus()
            }
        }
    }
    val currentOnDismiss by rememberUpdatedState(newValue = onTextEditorDismiss)
    val currentOnConfirm by rememberUpdatedState(newValue = onTextConfirmed)
    val textEditorState = rememberTextEditorState(
        initialValue = if (text.isBlank()) TextEditorValue.Empty else TextEditorValue.Editing(
            TextFieldValue(text)
        )
    )

    WhiteboardTextEditor(
        textEditorState = textEditorState,
        onDismissClick = {
            closeSheet.invoke()
            currentOnDismiss.invoke()
            textEditorState.type(TextFieldValue())
        },
        onConfirmClick = { newText ->
            closeSheet.invoke()
            currentOnConfirm.invoke(newText)
            textEditorState.type(TextFieldValue())
        }
    )
}