package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

internal enum class TextEditorState {
    Empty,
    Editing,
    Discard
}

@Composable
internal fun WhiteboardTextEditor(
    state: TextEditorState,
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 48.dp)) {
        if (state != TextEditorState.Discard) {
            Box(Modifier.weight(1f)) {
                UserInputText(
                    textFieldValue = textFieldValue,
                    onTextChanged = onTextChanged,
                    onDirectionLeft = { /**TODO**/ }
                )
            }
        } else {
            Text(
                text = stringResource(id = R.string.kaleyra_data_loss_confirm_message),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            IconTextButton(
                icon = iconFor(state),
                text = textFor(state),
                onClick = onDismissClick,
                modifier = Modifier.weight(1f)
            )
            IconTextButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_confirm),
                text = stringResource(id = R.string.kaleyra_action_confirm),
                onClick = onConfirmClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun iconFor(state: TextEditorState) = painterResource(
    id = when (state) {
        TextEditorState.Empty -> R.drawable.ic_kaleyra_close
        else -> R.drawable.ic_kaleyra_cancel
    }
)

@Composable
private fun textFor(state: TextEditorState) = stringResource(
    id = when (state) {
        TextEditorState.Empty -> R.string.kaleyra_action_dismiss
        TextEditorState.Editing -> R.string.kaleyra_action_discard_changes
        TextEditorState.Discard -> R.string.kaleyra_action_cancel
    }
)

@Composable
internal fun IconTextButton(
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
internal fun EmptyTextEditorPreview() {
    KaleyraTheme {
        WhiteboardTextEditor(
            state = TextEditorState.Empty,
            textFieldValue = TextFieldValue(),
            onTextChanged = {},
            onDismissClick = {},
            onConfirmClick = {}
        )
    }
}

@Preview
@Composable
internal fun EditingTextEditorPreview() {
    KaleyraTheme {
        WhiteboardTextEditor(
            state = TextEditorState.Editing,
            textFieldValue = TextFieldValue(),
            onTextChanged = {},
            onDismissClick = {},
            onConfirmClick = {}
        )
    }
}

@Preview
@Composable
internal fun DiscardTextEditorPreview() {
    KaleyraTheme {
        WhiteboardTextEditor(
            state = TextEditorState.Discard,
            textFieldValue = TextFieldValue(),
            onTextChanged = {},
            onDismissClick = {},
            onConfirmClick = {}
        )
    }
}