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

@file:OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)

package com.kaleyra.video_sdk.chat.input

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.common.button.IconButton
import com.kaleyra.video_sdk.common.userinput.UserInputText
import com.kaleyra.video_sdk.theme.KaleyraTheme

internal const val TextFieldTag = "TextFieldTag"

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun ChatUserInput(
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
                maxLines = 4,
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

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatUserInputPreview() = KaleyraTheme {
    ChatUserInput(onTextChanged = { }, onMessageSent = { })
}
