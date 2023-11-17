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

package com.kaleyra.video_sdk.chat.appbar.view

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_common_ui.utils.TimestampUtils
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.model.mockActions
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.theme.KaleyraTheme

@Composable
internal fun OneToOneAppBar(
    connectionState: ConnectionState,
    recipientDetails: ChatParticipantDetails,
    isInCall: Boolean,
    actions: ImmutableSet<ChatAction>,
    onBackPressed: () -> Unit = { }
) {
    ChatAppBar(actions = actions, isInCall = isInCall, onBackPressed = onBackPressed) {
        val recipientState by recipientDetails.state.collectAsStateWithLifecycle(ChatParticipantState.Unknown)
        val (username, image) = recipientDetails
        ChatAppBarContent(
            image = image,
            title = username,
            subtitle = textFor(connectionState = connectionState, recipientState = recipientState),
            typingDots = recipientState is ChatParticipantState.Typing
        )
    }
}

@Composable
private fun textFor(
    connectionState: ConnectionState,
    recipientState: ChatParticipantState
): String {
    val context = LocalContext.current
    return when {
        connectionState is ConnectionState.Offline -> stringResource(R.string.kaleyra_chat_state_waiting_for_network)
        connectionState is ConnectionState.Connecting -> stringResource(R.string.kaleyra_chat_state_connecting)
        recipientState is ChatParticipantState.Online -> stringResource(R.string.kaleyra_chat_user_status_online)
        recipientState is ChatParticipantState.Offline -> {
            val timestamp = recipientState.timestamp
            if (timestamp == null) stringResource(R.string.kaleyra_chat_user_status_offline)
            else stringResource(
                R.string.kaleyra_chat_user_status_last_login,
                TimestampUtils.parseTimestamp(context, timestamp)
            )
        }

        recipientState is ChatParticipantState.Typing -> stringResource(R.string.kaleyra_chat_user_status_typing)
        else -> ""
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun OneToOneAppBarPreview() = KaleyraTheme {
    OneToOneAppBar(
        connectionState = ConnectionState.Connecting,
        recipientDetails = ChatParticipantDetails(username = "John Smith"),
        actions = mockActions,
        isInCall = false,
        onBackPressed = { }
    )
}