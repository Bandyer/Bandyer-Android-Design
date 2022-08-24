@file:OptIn(ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.compose

import android.os.Bundle
import android.view.ContextThemeWrapper
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
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.ClickableAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation.Messages
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.topappbar.TopAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.CallType
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.ChatUiViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel.PhoneChatViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
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
        fetchMessages = { viewModel.fetchMessages() },
        readAllMessages = { viewModel.readAllMessages() },
        call = { viewModel.call(it) },
        showCall = { viewModel.showCall() },
        sendMessage = { viewModel.sendMessage(it) },
    )
}

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onResetMessagesScroll: () -> Unit,
    fetchMessages: () -> Unit,
    readAllMessages: () -> Unit,
    call: (CallType) -> Unit,
    showCall: () -> Unit,
    sendMessage: (String) -> Unit
) {
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.conversationState.conversationItems) {
        readAllMessages()
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
            actions = uiState.actions.mapToClickableAction(makeCall = { call(it) })
        )

        Messages(
            uiState = uiState.conversationState,
            onMessageScrolled = onMessageScrolled,
            onApproachingTop = fetchMessages,
            onResetScroll = onResetMessagesScroll,
            scrollState = scrollState,
            modifier = Modifier.weight(1f).fillMaxWidth()
        )

        if (uiState.isInCall) OngoingCallLabel(onClick = { showCall() })

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                val themeResId = it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
                KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
            },
            update = {
                it.callback = object : KaleyraChatInputLayoutEventListener {
                    override fun onTextChanged(text: String) = Unit
                    override fun onSendClicked(text: String) {
                        scope.launch {
                            sendMessage(text)
                            scrollState.scrollToItem(0)
                        }
                    }
                }
            }
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

