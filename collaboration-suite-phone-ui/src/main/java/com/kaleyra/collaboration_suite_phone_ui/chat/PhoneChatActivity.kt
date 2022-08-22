@file:OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val FETCH_THRESHOLD = 15

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
    val scrollState = rememberLazyListState()

    ChatScreen(
        uiState = uiState,
        scrollState = scrollState,
        onBackPressed = onBackPressed,
        onMessageScrolled = { viewModel.onMessageScrolled(it) },
        onFetchMessages = { viewModel.fetchMessages() },
        onReadAllMessages = { viewModel.readAllMessages() },
        onAllMessagesScrolled = { viewModel.onAllMessagesScrolled() },
        onCall = { viewModel.call(it) },
        onShowCall = { viewModel.showCall() },
        onSendMessage = { viewModel.sendMessage(it) },
    )
}

@Composable
internal fun ChatScreen(
    uiState: ChatUiState,
    scrollState: LazyListState,
    onBackPressed: () -> Unit,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onFetchMessages: () -> Unit,
    onReadAllMessages: () -> Unit,
    onAllMessagesScrolled: () -> Unit,
    onCall: (CallType) -> Unit,
    onShowCall: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    val shouldFetch by scrollState.shouldFetch()
    val showFab by scrollState.shouldShowFab()

    LaunchedEffect(scrollState.firstVisibleItemIndex) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .onEach {
                val item = uiState.conversationItems.getOrNull(it) as? ConversationItem.MessageItem ?: return@onEach
                onMessageScrolled(item)
            }.launchIn(this)
    }

    LaunchedEffect(shouldFetch) {
        snapshotFlow { shouldFetch }
            .filter { it }
            .onEach { onFetchMessages() }
            .launchIn(this)
    }

    LaunchedEffect(uiState.conversationItems) {
        snapshotFlow { uiState.conversationItems }
            .onEach { items ->
                val messageItems = items.filterIsInstance<ConversationItem.MessageItem>()
                onReadAllMessages()
                when {
                    scrollState.firstVisibleItemIndex < 3 -> scrollState.animateScrollToItem(0)
                    messageItems.firstOrNull()?.isMine == true -> scrollState.scrollToItem(0)
                }
            }.launchIn(this)
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

        Box(Modifier.weight(1f)) {
            if (!uiState.areMessagesFetched) LoadingMessagesLabel()
            else if (uiState.conversationItems.isEmpty()) NoMessagesLabel()
            else Messages(
                items = uiState.conversationItems,
                scrollState = scrollState,
                modifier = Modifier.fillMaxSize()
            )

            this@Column.AnimatedVisibility(
                visible = showFab,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                ScrollToBottomFab(
                    counter = uiState.unseenMessagesCount,
                    onClick = {
                        scope.launch { scrollState.scrollToItem(0) }
                        onAllMessagesScrolled()
                    }
                )
            }

            if (uiState.isInCall) OngoingCallLabel(onClick = { onShowCall() })
        }

        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = {
                val themeResId =
                    it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
                KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
            },
            update = {
                it.callback = object : KaleyraChatInputLayoutEventListener {
                    override fun onTextChanged(text: String) = Unit
                    override fun onSendClicked(text: String) = onSendMessage(text)
                }
            }
        )
    }
}

@Composable
private fun LazyListState.shouldFetch(): androidx.compose.runtime.State<Boolean> {
    return remember(this) {
        derivedStateOf {
            val totalItemsCount = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItemsCount != 0 && totalItemsCount <= lastVisibleItemIndex + FETCH_THRESHOLD
        }
    }
}

@Composable
private fun LazyListState.shouldShowFab(): androidx.compose.runtime.State<Boolean> =
    remember(this) { derivedStateOf { firstVisibleItemIndex > 0 && firstVisibleItemScrollOffset > 0 } }

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
private fun NoMessagesLabel() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_no_messages),
            style = MaterialTheme.typography.body2,
        )
    }
}

@Composable
private fun LoadingMessagesLabel() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_channel_loading),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
private fun OngoingCallLabel(onClick: () -> Unit) {
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

