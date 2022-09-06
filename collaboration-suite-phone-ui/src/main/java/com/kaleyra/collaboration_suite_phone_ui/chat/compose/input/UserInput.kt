package com.kaleyra.collaboration_suite_phone_ui.chat.compose.input

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
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

    Surface(elevation = 4.dp) {
        Row(Modifier.padding(start = 16.dp, top = 4.dp, end = 12.dp, bottom = 4.dp)) {
            UserInputText(
                textFieldValue = textState,
                onTextChanged = {
                    textState = it
                    onTextChanged(it)
                },
                modifier = Modifier.weight(1.0f)
            )
            IconButton(
                modifier = Modifier.height(48.dp),
                enabled = textState.text.isNotBlank(),
                onClick = { onMessageSent(textState.text) },
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_kaleyra_send),
                    tint = if (textState.text.isNotBlank()) MaterialTheme.colors.secondary else MaterialTheme.colors.onSurface.copy(
                        alpha = 0.25f
                    ),
                    modifier = Modifier.size(42.dp),
                    contentDescription = stringResource(id = R.string.kaleyra_back)
                )
            }
        }
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@Composable
internal fun UserInputText(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier
) {
    val contentDesc = stringResource(id = R.string.kaleyra_chat_textfield_desc)
    Row(
        modifier = Modifier
            .height(48.dp)
            .semantics {
                contentDescription = contentDesc
                keyboardShownProperty = true
            }
            .then(modifier),
        horizontalArrangement = Arrangement.End
    ) {
        Surface {
            Box(
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f)
                    .align(Alignment.Bottom)
            ) {
                var focusState by remember { mutableStateOf(false) }
                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterStart)
                        .onFocusChanged { focusState = it.isFocused },
                    maxLines = 4,
                    cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier.align(Alignment.CenterStart),
                        text = stringResource(id = R.string.kaleyra_edit_text_input_placeholder),
                        style = MaterialTheme.typography.subtitle1.copy(color = disableContentColor)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UserInputTextPreview() {
    UserInput({ }, { })
}
