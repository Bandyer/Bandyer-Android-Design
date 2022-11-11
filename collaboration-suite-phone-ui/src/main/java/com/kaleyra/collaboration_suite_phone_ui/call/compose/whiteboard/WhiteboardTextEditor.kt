@file:OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)

package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.chat.input.UserInputText
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import kotlinx.coroutines.launch

private enum class TextEditorState {
    Empty,
    Editing,
    Discard
}

// TODO Add test to check the keyboard is no more shown on dismiss when it was previously opened
@Composable
internal fun WhiteboardTextEditor(
    modalSheetState: ModalBottomSheetState,
    onConfirmClick: (TextFieldValue) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var textState by remember { mutableStateOf(TextFieldValue()) }
    val isTextBlank by remember {
        derivedStateOf {
            textState.text.isBlank()
        }
    }
    var editorState by remember { mutableStateOf(TextEditorState.Empty) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isTextBlank) {
        editorState = when {
            editorState == TextEditorState.Editing && isTextBlank -> TextEditorState.Empty
            editorState == TextEditorState.Empty && !isTextBlank -> TextEditorState.Editing
            else -> editorState
        }
    }

    Column(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 48.dp)
    ) {
        if (editorState == TextEditorState.Discard) {
            Text(
                text = stringResource(id = R.string.kaleyra_data_loss_confirm_message),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            Box(Modifier.weight(1f)) {
                UserInputText(
                    textFieldValue = textState,
                    onTextChanged = { textState = it },
                    onDirectionLeft = { /**TODO**/ }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            AnimatedContent(
                modifier = Modifier.weight(1f),
                targetState = editorState
            ) {
                when (it) {
                    TextEditorState.Empty -> {
                        EditorButton(
                            icon = painterResource(id = R.drawable.ic_kaleyra_close),
                            text = stringResource(id = R.string.kaleyra_action_dismiss),
                            onClick = {
                                scope.launch {
                                    focusManager.clearFocus()
                                    modalSheetState.hide()
                                }
                            }
                        )
                    }
                    TextEditorState.Editing -> {
                        EditorButton(
                            icon = painterResource(id = R.drawable.ic_kaleyra_cancel),
                            text = stringResource(id = R.string.kaleyra_action_discard_changes),
                            onClick = { editorState = TextEditorState.Discard }
                        )
                    }
                    TextEditorState.Discard -> {
                        EditorButton(
                            icon = painterResource(id = R.drawable.ic_kaleyra_cancel),
                            text = stringResource(id = R.string.kaleyra_action_cancel),
                            onClick = { editorState = TextEditorState.Editing }
                        )
                    }
                }
            }
            EditorButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_confirm),
                text = stringResource(id = R.string.kaleyra_action_confirm),
                onClick = { onConfirmClick(textState) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun EditorButton(
    icon: Any,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            icon = icon,
            iconDescription = text,
            onClick = onClick,
        )
        Text(text = text, fontSize = 12.sp)
    }
}

@Preview
@Composable
internal fun TextEditorPreview() {
    KaleyraTheme {
        WhiteboardTextEditor(
            modalSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Expanded),
            onConfirmClick = {}
        )
    }
}