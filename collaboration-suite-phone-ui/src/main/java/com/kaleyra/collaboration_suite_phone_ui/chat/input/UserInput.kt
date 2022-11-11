@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.input

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.compose.IconButton
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus

internal const val TextFieldTag = "TextFieldTag"

@Composable
internal fun UserInput(
    onTextChanged: () -> Unit,
    onMessageSent: (String) -> Unit,
    onDirectionLeft: (() -> Unit) = { }
) {
    val interactionSource = remember { MutableInteractionSource() }
    var textState by remember { mutableStateOf(TextFieldValue()) }

    Surface {
        Row(
            modifier = Modifier
                .focusGroup()
                .highlightOnFocus(interactionSource)
                .padding(start = 16.dp, top = 4.dp, end = 12.dp, bottom = 4.dp)
        ) {
            UserInputText(
                textFieldValue = textState,
                onTextChanged = {
                    textState = it
                    onTextChanged()
                },
                onDirectionLeft = onDirectionLeft,
                modifier = Modifier.weight(1.0f),
                interactionSource = interactionSource
            )
            IconButton(
                icon = painterResource(id = R.drawable.ic_kaleyra_send),
                iconDescription = stringResource(id = R.string.kaleyra_chat_send),
                iconTint = MaterialTheme.colors.secondary,
                iconSize = 42.dp,
                enabled = textState.text.isNotBlank(),
                supportRtl = true,
                onClick = {
                    onMessageSent(textState.text)
                    textState = TextFieldValue()
                }
            )
        }
    }
}

// TODO move in a common package for call and chat
@Composable
internal fun UserInputText(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onDirectionLeft: () -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .height(48.dp)
            .then(modifier),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = textFieldValue,
            onValueChange = { onTextChanged(it) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(TextFieldTag)
                .onPreviewKeyEvent {
                    if (it.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (it.key) {
                        Key.Tab -> {
                            focusManager.moveFocus(FocusDirection.Next); true
                        }
                        Key.DirectionUp -> {
                            focusManager.moveFocus(FocusDirection.Up); true
                        }
                        Key.DirectionDown -> {
                            focusManager.moveFocus(FocusDirection.Down); true
                        }
                        Key.DirectionRight -> {
                            focusManager.moveFocus(FocusDirection.Right); true
                        }
                        Key.DirectionLeft -> {
                            onDirectionLeft.invoke(); true
                        }
                        else -> false
                    }
                },
            interactionSource = interactionSource,
            maxLines = 4,
            cursorBrush = SolidColor(MaterialTheme.colors.secondary)
        )

        val hintColor = LocalContentColor.current.copy(alpha = 0.5f)
        if (textFieldValue.text.isEmpty()) {
            Text(
                text = stringResource(id = R.string.kaleyra_edit_text_input_placeholder),
                style = MaterialTheme.typography.subtitle1.copy(color = hintColor)
            )
        }
    }
}

@Preview
@Composable
internal fun UserInputPreview() = KaleyraTheme {
    UserInput(onTextChanged = { }, onMessageSent = { })
}

@Preview
@Composable
internal fun UserInputDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    UserInput(onTextChanged = { }, onMessageSent = { })
}
