package com.kaleyra.collaboration_suite_phone_ui.chat.compose.input

import android.view.ContextThemeWrapper
import android.widget.ImageButton
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId

@Composable
internal fun UserInput(onSendMessage: (String) -> Unit, onTyping: () -> Unit) {
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = {
            val themeResId = it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
            KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
        },
        update = {
            it.callback = object : KaleyraChatInputLayoutEventListener {
                override fun onTextChanged(text: String) = onTyping()
                override fun onSendClicked(text: String) = onSendMessage(text)
            }
        }
    )
}

@Composable
internal fun UserInput(
    onTextChanged: (TextFieldValue) -> Unit,
    onMessageSent: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val textState by remember { mutableStateOf(TextFieldValue()) }

    Surface {
        Row(modifier = modifier) {
            UserInputText(textState, onTextChanged)
            IconButton(
                modifier = Modifier.height(36.dp),
                enabled = textState.text.isNotBlank(),
                onClick = { onMessageSent(textState.text) },
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                contentPadding = PaddingValues(16.dp)
            )
        }
    }
}

@Composable
internal fun UserInputText(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit
) {
    val contentDesc = stringResource(id = R.string.kaleyra_chat_textfield_desc)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .semantics {
                contentDescription = contentDesc
//                 keyboardShownProperty = keyboardShown
            },
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
                        .padding(start = 16.dp)
                        .align(Alignment.CenterStart)
                        .onFocusChanged { focusState = it.isFocused },
                    maxLines = 4,
                    cursorBrush = SolidColor(MaterialTheme.colors.secondary),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f)
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 16.dp),
                        text = stringResource(id = R.string.kaleyra_edit_text_input_placeholder),
                        style = MaterialTheme.typography.body1.copy(color = disableContentColor)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun UserInputTextPreview() {
    val textState by remember { mutableStateOf(TextFieldValue()) }
    UserInputText(textState) {

    }
}
