package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.view

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalFocusManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun WhiteboardModalBottomSheetContent(sheetState: ModalBottomSheetState) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    WhiteboardTextEditor(
        onDismissClick = {
            scope.launch {
                sheetState.hide()
                focusManager.clearFocus()
            }
        },
        onConfirmClick = {
            scope.launch {
                sheetState.hide()
                focusManager.clearFocus()
            }
        }
    )
}