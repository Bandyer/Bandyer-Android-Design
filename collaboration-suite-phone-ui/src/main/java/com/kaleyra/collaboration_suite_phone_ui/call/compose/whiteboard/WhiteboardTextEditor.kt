package com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard

import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.*
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

internal sealed class TextEditorValue {
    object Empty : TextEditorValue()
    data class Editing(val textFieldValue: TextFieldValue) : TextEditorValue()
    object Discard : TextEditorValue()
}

internal class TextEditorState(initialValue: TextEditorValue) {
    var currentValue: TextEditorValue by mutableStateOf(initialValue)
        private set

    var textFieldValue: TextFieldValue by mutableStateOf(TextFieldValue())
        private set

    init {
        textFieldValue = (initialValue as? TextEditorValue.Editing)?.textFieldValue ?: TextFieldValue()
    }

    fun type(textFieldValue: TextFieldValue) {
        if (currentValue == TextEditorValue.Discard) return
        currentValue = if (textFieldValue.text.isBlank()) TextEditorValue.Empty else TextEditorValue.Editing(textFieldValue)
        this.textFieldValue = textFieldValue
    }

    fun dismiss(): Boolean {
        return when (currentValue) {
            TextEditorValue.Empty -> true
            is TextEditorValue.Editing -> {
                currentValue = TextEditorValue.Discard; false
            }
            TextEditorValue.Discard -> {
                currentValue = TextEditorValue.Editing(textFieldValue); false
            }
        }
    }

    fun confirm(): String? {
        val currentValue = currentValue
        return if (currentValue is TextEditorValue.Editing) currentValue.textFieldValue.text else null
    }
}

@Composable
internal fun rememberTextEditorState(initialValue: TextEditorValue) = remember(initialValue) {
    TextEditorState(initialValue = initialValue)
}

@Composable
internal fun WhiteboardTextEditor(
    textEditorState: TextEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty),
    onDismissClick: () -> Unit,
    onConfirmClick: (String) -> Unit
) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 48.dp)) {
        if (textEditorState.currentValue != TextEditorValue.Discard) {
            Box(Modifier.weight(1f)) {
                UserInputText(
                    textFieldValue = textEditorState.textFieldValue,
                    onTextChanged = { textEditorState.type(it) },
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
                icon = iconFor(textEditorState.currentValue),
                text = textFor(textEditorState.currentValue),
                onClick = {
                    if (textEditorState.dismiss()) {
                        onDismissClick()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            IconTextButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_confirm),
                text = stringResource(id = R.string.kaleyra_action_confirm),
                onClick = {
                    val text = textEditorState.confirm()
                    if (text != null) onConfirmClick(text)
                    else onDismissClick()
                },
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
        is TextEditorValue.Editing -> R.string.kaleyra_action_discard_changes
        TextEditorValue.Discard -> R.string.kaleyra_action_cancel
    }
)

@Composable
internal fun IconTextButton(icon: Any, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally
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
            textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty),
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
            textEditorState = rememberTextEditorState(
                initialValue = TextEditorValue.Editing(TextFieldValue(text = "Texting..."))
            ),
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
            textEditorState = rememberTextEditorState(initialValue = TextEditorValue.Discard),
            onDismissClick = {},
            onConfirmClick = {}
        )
    }
}