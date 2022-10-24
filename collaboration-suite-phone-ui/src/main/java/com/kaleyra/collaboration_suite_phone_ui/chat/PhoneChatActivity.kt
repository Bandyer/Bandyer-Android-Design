@file:OptIn(ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.requestConfiguration
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.Messages
import com.kaleyra.collaboration_suite_phone_ui.chat.input.UserInput
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.model.mockUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.topappbar.TopAppBar
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.collectAsStateWithLifecycle
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.highlightOnFocus
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.ChatUiViewModel
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.PhoneChatViewModel
import kotlinx.coroutines.launch

internal class PhoneChatActivity : ChatActivity() {

    override val viewModel: PhoneChatViewModel by viewModels {
        PhoneChatViewModel.provideFactory(::requestConfiguration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = this::finishAndRemoveTask, viewModel = viewModel)

                val isSystemInDarkTheme = isSystemInDarkTheme()
                SideEffect {
                    window.navigationBarColor = if (isSystemInDarkTheme) Color.Black.toArgb() else Color.White.toArgb()
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !isSystemInDarkTheme
                }
            }
        }
    }
}

internal const val MessagesTag = "MessagesTag"

@Composable
fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: ChatUiViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ChatScreen(
        uiState = uiState,
        onBackPressed = onBackPressed,
        onMessageScrolled = viewModel::onMessageScrolled,
        onResetMessagesScroll = viewModel::onAllMessagesScrolled,
        onFetchMessages = viewModel::fetchMessages,
        onShowCall = viewModel::showCall,
        onSendMessage = viewModel::sendMessage,
        onTyping = viewModel::typing
    )
}

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
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
            .systemBarsPadding()
            .imePadding()
            .semantics {
                testTagsAsResourceId = true
            }) {
        Box(Modifier.focusRequester(topBarRef)){
            TopAppBar(
                state = uiState.state,
                info = uiState.info,
                onBackPressed = onBackPressed,
                actions = uiState.actions
            )
        }

        if (uiState.isInCall) OngoingCallLabel(onClick = onShowCall)

        Messages(
            uiState = uiState.conversationState,
            onDirectionLeft = topBarRef::requestFocus,
            onMessageScrolled = onMessageScrolled,
            onApproachingTop = onFetchMessages,
            onResetScroll = onResetMessagesScroll,
            scrollState = scrollState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag(MessagesTag)
        )

        Divider(
            color = colorResource(id = R.color.kaleyra_color_grey_light),
            modifier = Modifier.fillMaxWidth()
        )
        UserInput(
            onTextChanged = onTyping,
            onMessageSent = onMessageSent,
            onDirectionLeft = topBarRef::requestFocus
        )
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

@Preview
@Composable
internal fun ChatScreenPreview() = KaleyraTheme {
    ChatScreen(
        uiState = mockUiState,
        onBackPressed = { },
        onMessageScrolled = { },
        onResetMessagesScroll = { },
        onFetchMessages = { },
        onShowCall = { },
        onSendMessage = { },
        onTyping = { }
    )
}

@Preview
@Composable
internal fun ChatScreenDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    ChatScreen(
        uiState = mockUiState,
        onBackPressed = { },
        onMessageScrolled = { },
        onResetMessagesScroll = { },
        onFetchMessages = { },
        onShowCall = { },
        onSendMessage = { },
        onTyping = { }
    )
}


