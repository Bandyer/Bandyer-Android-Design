package com.kaleyra.collaboration_suite_phone_ui.call.component.whiteboard.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager

@Composable
internal fun WhiteboardModalBottomSheetContent(
    textEditorState: TextEditorState,
    onTextDismissed: () -> Unit,
    onTextConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val clearFocus = remember {
        { focusManager.clearFocus() }
    }
    val currentOnDismiss by rememberUpdatedState(newValue = onTextDismissed)
    val currentOnConfirm by rememberUpdatedState(newValue = onTextConfirmed)

    WhiteboardTextEditor(
        textEditorState = textEditorState,
        onDismiss = {
            clearFocus.invoke()
            currentOnDismiss.invoke()
        },
        onConfirm = { newText ->
            clearFocus.invoke()
            currentOnConfirm.invoke(newText)
        },
        modifier = modifier
    )
}