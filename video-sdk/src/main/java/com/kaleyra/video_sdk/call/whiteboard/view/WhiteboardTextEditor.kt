package com.kaleyra.video_sdk.call.whiteboard.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.common.userinput.UserInputText
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Immutable
internal sealed class TextEditorValue {
    object Empty : TextEditorValue()
    data class Editing(val textFieldValue: TextFieldValue) : TextEditorValue()
    object Discard : TextEditorValue()
}

@Stable
internal class TextEditorState(initialValue: TextEditorValue) {
    var currentValue: TextEditorValue by mutableStateOf(initialValue)
        private set

    var textFieldValue: TextFieldValue by mutableStateOf((initialValue as? TextEditorValue.Editing)?.textFieldValue ?: TextFieldValue())
        private set

    fun type(textFieldValue: TextFieldValue) {
        if (currentValue == TextEditorValue.Discard) return
        currentValue = if (textFieldValue.text.isBlank()) TextEditorValue.Empty else TextEditorValue.Editing(
            textFieldValue
        )
        this.textFieldValue = textFieldValue
    }

    fun cancel(): Boolean {
        return when (currentValue) {
            TextEditorValue.Empty -> { clearState(); true }
            is TextEditorValue.Editing -> { currentValue = TextEditorValue.Discard; false }
            TextEditorValue.Discard -> { currentValue = TextEditorValue.Editing(textFieldValue); false }
        }
    }

    fun confirm(): String? {
        val currentText = (currentValue as? TextEditorValue.Editing)?.textFieldValue?.text
        return currentText.also { clearState() }
    }

    fun clearState() {
        currentValue = TextEditorValue.Empty
        textFieldValue = TextFieldValue()
    }
}

@Composable
internal fun rememberTextEditorState(initialValue: TextEditorValue) = remember(initialValue) {
    TextEditorState(initialValue = initialValue)
}

@Composable
internal fun WhiteboardTextEditor(
    textEditorState: TextEditorState = rememberTextEditorState(initialValue = TextEditorValue.Empty),
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(start = 16.dp, end = 16.dp, bottom = 48.dp)) {
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
                    if (textEditorState.cancel()) {
                        onDismiss()
                    }
                },
                modifier = Modifier.weight(1f)
            )
            IconTextButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_confirm),
                text = stringResource(id = R.string.kaleyra_action_confirm),
                onClick = {
                    val text = textEditorState.confirm()
                    if (text != null) onConfirm(text)
                    else onDismiss()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun iconFor(state: TextEditorValue) = painterResource(
    id = when (state) {
        is TextEditorValue.Empty -> R.drawable.ic_kaleyra_close
        else -> R.drawable.ic_kaleyra_cancel
    }
)

@Composable
private fun textFor(state: TextEditorValue) = stringResource(
    id = when (state) {
        is TextEditorValue.Empty -> R.string.kaleyra_action_dismiss
        is TextEditorValue.Editing -> R.string.kaleyra_action_discard_changes
        is TextEditorValue.Discard -> R.string.kaleyra_action_cancel
    }
)

@Composable
internal fun IconTextButton(icon: Painter, text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
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

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun EmptyTextEditorPreview() {
    TextEditorPreview(TextEditorValue.Empty)
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun EditingTextEditorPreview() {
    TextEditorPreview(TextEditorValue.Editing(TextFieldValue(text = "Texting...")))
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun DiscardTextEditorPreview() {
    TextEditorPreview(TextEditorValue.Discard)
}

@Composable
private fun TextEditorPreview(textEditorInitialValue: TextEditorValue) {
    KaleyraTheme {
        Surface {
            WhiteboardTextEditor(
                textEditorState = rememberTextEditorState(initialValue = textEditorInitialValue),
                onDismiss = {},
                onConfirm = {}
            )
        }
    }
}
