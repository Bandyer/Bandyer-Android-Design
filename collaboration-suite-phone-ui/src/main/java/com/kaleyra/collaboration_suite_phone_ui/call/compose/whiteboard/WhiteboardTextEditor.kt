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

internal enum class TextEditorValue {
    Empty,
    Editing,
    Discard
}

@Composable
internal fun WhiteboardTextEditor(state: TextEditorState) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 48.dp)) {
        if (state.currentValue != TextEditorValue.Discard) {
            Box(Modifier.weight(1f)) {
                UserInputText(
                    textFieldValue = state.textFieldValue,
                    onTextChanged = { state.textFieldValue(it) },
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
                icon = iconFor(state.currentValue),
                text = textFor(state.currentValue),
                onClick = state::dismiss,
                modifier = Modifier.weight(1f)
            )
            IconTextButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_confirm),
                text = stringResource(id = R.string.kaleyra_action_confirm),
                onClick = state::confirm,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun iconFor(state: TextEditorValue) = painterResource(
    id = when (state) {
        TextEditorValue.Empty -> R.drawable.ic_kaleyra_close
        else -> R.drawable.ic_kaleyra_cancel
    }
)

@Composable
private fun textFor(state: TextEditorValue) = stringResource(
    id = when (state) {
        TextEditorValue.Empty -> R.string.kaleyra_action_dismiss
        TextEditorValue.Editing -> R.string.kaleyra_action_discard_changes
        TextEditorValue.Discard -> R.string.kaleyra_action_cancel
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
//        WhiteboardTextEditor(
//            state = TextEditorValue.Empty,
//            textFieldValue = TextFieldValue(),
//            onTextChanged = {},
//            onDismissClick = {},
//            onConfirmClick = {}
//        )
    }
}

@Preview
@Composable
internal fun EditingTextEditorPreview() {
    KaleyraTheme {
//        WhiteboardTextEditor(
//            state = TextEditorValue.Editing,
//            textFieldValue = TextFieldValue(),
//            onTextChanged = {},
//            onDismissClick = {},
//            onConfirmClick = {}
//        )
    }
}

@Preview
@Composable
internal fun DiscardTextEditorPreview() {
    KaleyraTheme {
//        WhiteboardTextEditor(
//            state = TextEditorValue.Discard,
//            textFieldValue = TextFieldValue(),
//            onTextChanged = {},
//            onDismissClick = {},
//            onConfirmClick = {}
//        )
    }
}