package com.kaleyra.video_sdk.chat.screen

import android.content.res.Configuration
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaleyra.video_sdk.R
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.appbar.view.GroupAppBar
import com.kaleyra.video_sdk.chat.appbar.view.OneToOneAppBar
import com.kaleyra.video_sdk.chat.conversation.ConversationComponent
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.input.ChatUserInput
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.chat.screen.model.mockChatUiState
import com.kaleyra.video_sdk.chat.screen.viewmodel.PhoneChatViewModel
import com.kaleyra.video_sdk.common.spacer.StatusBarsSpacer
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.view.UserMessageSnackbarHandler
import com.kaleyra.video_sdk.extensions.ContextExtensions.findActivity
import com.kaleyra.video_sdk.extensions.ModifierExtensions.highlightOnFocus
import com.kaleyra.video_sdk.theme.CollaborationTheme
import com.kaleyra.video_sdk.theme.KaleyraTheme
import kotlinx.coroutines.launch

internal const val ConversationComponentTag = "ConversationComponentTag"

@Composable
internal fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: PhoneChatViewModel
) {
    val activity = LocalContext.current.findActivity()
    val theme by viewModel.theme.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle(initialValue = null)

    if (!uiState.isUserConnected || uiState.connectionState is ConnectionState.Error) {
        LaunchedEffect(Unit) {
            activity.finishAndRemoveTask()
        }
    }

    CollaborationTheme(theme = theme) {
        ChatScreen(
            uiState = uiState,
            userMessage = userMessage,
            onBackPressed = onBackPressed,
            onMessageScrolled = viewModel::onMessageScrolled,
            onResetMessagesScroll = viewModel::onAllMessagesScrolled,
            onFetchMessages = viewModel::fetchMessages,
            onShowCall = viewModel::showCall,
            onSendMessage = viewModel::sendMessage,
            onTyping = viewModel::typing
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    userMessage: UserMessage? = null,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.Message) -> Unit,
    onResetMessagesScroll: () -> Unit,
    onFetchMessages: () -> Unit,
    onShowCall: () -> Unit,
    onSendMessage: (String) -> Unit,
    onTyping: () -> Unit
) {
    val topBarRef = remember { FocusRequester() }
    val scrollState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val onMessageSent: ((String) -> Unit) = remember(scope, scrollState) {
        { text ->
            scope.launch {
                onSendMessage(text)
                scrollState.scrollToItem(0)
            }
        }
    }

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colors.background)
            .fillMaxSize()
            .navigationBarsPadding()
            .imePadding()
            .semantics {
                testTagsAsResourceId = true
            }) {
        StatusBarsSpacer(Modifier.background(MaterialTheme.colors.primaryVariant))
        Box(Modifier.focusRequester(topBarRef)) {
            when (uiState) {
                is ChatUiState.OneToOne -> {
                    OneToOneAppBar(
                        connectionState = uiState.connectionState,
                        recipientDetails = uiState.recipientDetails,
                        isInCall = uiState.isInCall,
                        actions = uiState.actions,
                        onBackPressed = onBackPressed
                    )
                }

                is ChatUiState.Group -> {
                    GroupAppBar(
                        image = uiState.image,
                        name = uiState.name.ifBlank { stringResource(R.string.kaleyra_chat_group_title) },
                        connectionState = uiState.connectionState,
                        participantsDetails = uiState.participantsDetails,
                        participantsState = uiState.participantsState,
                        isInCall = uiState.isInCall,
                        actions = uiState.actions,
                        onBackPressed = onBackPressed
                    )
                }
            }
        }

        if (uiState.isInCall) OngoingCallLabel(onClick = onShowCall)

        Box {
            Column {
                ConversationComponent(
                    conversationState = uiState.conversationState,
                    participantsDetails = if (uiState is ChatUiState.Group) uiState.participantsDetails else null,
                    onDirectionLeft = topBarRef::requestFocus,
                    onMessageScrolled = onMessageScrolled,
                    onApproachingTop = onFetchMessages,
                    onResetScroll = onResetMessagesScroll,
                    scrollState = scrollState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag(ConversationComponentTag)
                )

                Divider(
                    color = colorResource(id = R.color.kaleyra_color_grey_light),
                    modifier = Modifier.fillMaxWidth()
                )
                ChatUserInput(
                    onTextChanged = onTyping,
                    onMessageSent = onMessageSent,
                    onDirectionLeft = topBarRef::requestFocus
                )
            }

            UserMessageSnackbarHandler(userMessage = userMessage)
        }
    }
}

@Composable
internal fun OngoingCallLabel(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            )
            .fillMaxWidth()
            .background(
                shape = RectangleShape,
                color = colorResource(id = R.color.kaleyra_color_answer_button)
            )
            .highlightOnFocus(interactionSource)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_ongoing_call_label),
            color = Color.White,
            style = MaterialTheme.typography.body2,
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun ChatScreenPreview() = KaleyraTheme {
    ChatScreen(
        uiState = mockChatUiState,
        onBackPressed = { },
        onMessageScrolled = { },
        onResetMessagesScroll = { },
        onFetchMessages = { },
        onShowCall = { },
        onSendMessage = { },
        onTyping = { }
    )
}