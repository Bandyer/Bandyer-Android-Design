package com.kaleyra.collaboration_suite_phone_ui.chat.compose.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R

@Composable
internal fun UserInput(
    onTextChanged: (TextFieldValue) -> Unit,
    onMessageSent: (String) -> Unit
) {
    var textState by remember { mutableStateOf(TextFieldValue()) }

    Surface {
        Row(Modifier.padding(start = 16.dp, top = 4.dp, end = 12.dp, bottom = 4.dp)) {
            UserInputText(
                textFieldValue = textState,
                onTextChanged = {
                    textState = it
                    onTextChanged(it)
                },
                modifier = Modifier.weight(1.0f)
            )
            SendButton(
                enabled = textState.text.isNotBlank(),
                onClick = { onMessageSent(textState.text) }
            )
        }
    }
}

@Composable
internal fun SendButton(enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        modifier = Modifier.height(48.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_kaleyra_send),
            tint = if (enabled) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(
                alpha = 0.25f
            ),
            modifier = Modifier.size(42.dp),
            contentDescription = stringResource(id = R.string.kaleyra_chat_send)
        )
    }
}

@Composable
internal fun UserInputText(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val description = stringResource(id = R.string.kaleyra_chat_textfield_desc)
    Row(
        modifier = Modifier
            .height(48.dp)
            .semantics { contentDescription = description }
            .then(modifier)
    ) {
        Surface {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart),
                    maxLines = 4,
                    cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val hintColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                if (textFieldValue.text.isEmpty()) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = stringResource(id = R.string.kaleyra_edit_text_input_placeholder),
                        style = MaterialTheme.typography.subtitle1.copy(color = hintColor)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UserInputTextPreview() {
    UserInput(onTextChanged = { }, onMessageSent = { })
}
