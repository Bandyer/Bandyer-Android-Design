@file:OptIn(ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation.Messages
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.input.UserInput
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.CallType
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ClickableAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.TopAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.PhoneChatViewModel
import kotlinx.coroutines.launch

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { finishAndRemoveTask() }, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: ChatUiViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    ChatScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onMessageScrolled = { viewModel.onMessageScrolled(it) },
        onResetMessagesScroll = { viewModel.onAllMessagesScrolled() },
        onFetchMessages = { viewModel.fetchMessages() },
        onReadAllMessages = { viewModel.readAllMessages() },
        onCall = { viewModel.call(it) },
        onShowCall = { viewModel.showCall() },
        onSendMessage = { viewModel.sendMessage(it) },
        onTyping = { viewModel.typing() }
    )
}

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onResetMessagesScroll: () -> Unit,
    onFetchMessages: () -> Unit,
    onReadAllMessages: () -> Unit,
    onCall: (CallType) -> Unit,
    onShowCall: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.conversationState.conversationItems) {
        onReadAllMessages()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                testTagsAsResourceId = true
            }) {
        TopAppBar(
            state = uiState.state,
            info = uiState.info,
            onBackPressed = onBackPressed,
            actions = uiState.actions.mapToClickableAction(makeCall = { onCall(it) })
        )

        Messages(
            uiState = uiState.conversationState,
            onMessageScrolled = onMessageScrolled,
            onApproachingTop = onFetchMessages,
            onResetScroll = onResetMessagesScroll,
            scrollState = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )

        if (uiState.isInCall) OngoingCallLabel(onClick = { onShowCall() })

        UserInput(
            onTextChanged = { onTyping() },
            onMessageSent = { text ->
                scope.launch {
                    onSendMessage(text)
                    scrollState.scrollToItem(0)
                }
            },
        )
    }
}

private fun Set<ChatAction>.mapToClickableAction(makeCall: (CallType) -> Unit): Set<ClickableAction> {
    return map {
        when (it) {
            is ChatAction.AudioCall -> ClickableAction(it) { makeCall(CallType.Audio) }
            is ChatAction.AudioUpgradableCall -> ClickableAction(it) { makeCall(CallType.AudioUpgradable) }
            else -> ClickableAction(it) { makeCall(CallType.Video) }
        }
    }.toSet()
}

@Composable
internal fun OngoingCallLabel(onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = onClick, role = Role.Button)
            .fillMaxWidth()
            .background(
                shape = RectangleShape,
                color = colorResource(id = R.color.kaleyra_color_answer_button)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_ongoing_call_label),
            color = Color.White,
            style = MaterialTheme.typography.body2,
        )
    }
}

