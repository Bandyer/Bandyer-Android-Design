/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_sdk.call.whiteboard.view

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