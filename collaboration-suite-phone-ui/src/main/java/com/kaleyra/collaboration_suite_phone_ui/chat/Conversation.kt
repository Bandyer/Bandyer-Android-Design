@file:OptIn(ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite_phone_ui.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

private val OtherBubbleShape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 12.dp)
private val MyBubbleShape = RoundedCornerShape(24.dp, 12.dp, 0.dp, 24.dp)

const val MessageTag = "MessageTag"
const val MessagesLazyColumnTag = "MessagesLazyColumnTag"

private const val FETCH_THRESHOLD = 15

private val LazyListState.isReachingTop: Boolean
    get() = derivedStateOf {
        val totalItemsCount = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        totalItemsCount != 0 && totalItemsCount <= lastVisibleItemIndex + FETCH_THRESHOLD
    }.value

@Composable
internal fun Messages(
    uiState: ConversationUiState,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onFetchMessages: () -> Unit,
    onAllMessagesScrolled: () -> Unit,
    onReadAllMessages: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.conversationItems) {
        if (scrollState.firstVisibleItemIndex < 3) scrollState.animateScrollToItem(0)
        onReadAllMessages()
    }

    LaunchedEffect(scrollState.firstVisibleItemIndex) {
        val item =
            uiState.conversationItems.getOrNull(scrollState.firstVisibleItemIndex) as? ConversationItem.MessageItem
                ?: return@LaunchedEffect
        onMessageScrolled(item)
    }

    LaunchedEffect(scrollState.isReachingTop) {
        if (scrollState.isReachingTop) onFetchMessages()
    }

    Box(modifier) {
        if (!uiState.areMessagesInitialized) LoadingMessagesLabel(Modifier.align(Alignment.Center))
        else if (uiState.conversationItems.isEmpty()) NoMessagesLabel(Modifier.align(Alignment.Center))
        else Conversation(
            items = uiState.conversationItems,
            scrollState = scrollState,
            modifier = Modifier.fillMaxSize()
        )

        ScrollToBottomFab(
            counter = uiState.unseenMessagesCount,
            onClick = {
                scope.launch { scrollState.scrollToItem(0) }
                onAllMessagesScrolled()
            },
            enabled = scrollState.scrollTopBottomFabEnabled,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

@Composable
internal fun NoMessagesLabel(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_no_messages),
            style = MaterialTheme.typography.body2,
        )
    }
}

@Composable
internal fun LoadingMessagesLabel(modifier: Modifier = Modifier) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_channel_loading),
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
internal fun Conversation(
    items: List<ConversationItem>,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.testTag(MessagesLazyColumnTag)
    ) {
        items(items, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationItem.MessageItem -> Message(
                    item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MessageTag)
                )
                is ConversationItem.DayItem -> DayHeader(
                    item.timestamp,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                is ConversationItem.NewMessagesItem -> NewMessagesHeader(
                    item.count,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
internal fun NewMessagesHeader(count: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Text(
            text = pluralStringResource(id = R.plurals.kaleyra_chat_unread_messages, count, count),
            fontSize = 12.sp,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
internal fun DayHeader(timestamp: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Text(text = timestamp, fontSize = 12.sp, style = MaterialTheme.typography.body2)
    }
}

@Composable
internal fun Message(messageItem: ConversationItem.MessageItem, modifier: Modifier = Modifier) {
    val horizontalArrangement =
        if (messageItem.message is Message.MyMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { Bubble(messageItem) }
    )
}

@Composable
internal fun Bubble(messageItem: ConversationItem.MessageItem) {
    val configuration = LocalConfiguration.current

    Card(
        shape = if (messageItem.message is Message.MyMessage) MyBubbleShape else OtherBubbleShape,
        backgroundColor = if (messageItem.message is Message.MyMessage) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant,
        elevation = 0.dp,
        modifier = Modifier.widthIn(min = 0.dp, max = configuration.screenWidthDp.div(2).dp)
    ) {
        Column(modifier = Modifier.padding(16.dp, 8.dp)) {
            ClickableMessage(item = messageItem)

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = messageItem.message.time,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body2
                )

                if (messageItem.message is Message.MyMessage) {
                    val messageState = messageItem.message.state.collectAsState().value

                    Icon(
                        painter = painterFor(messageState),
                        contentDescription = contentDescriptionFor(messageState),
                        modifier = Modifier
                            .padding(2.dp)
                            .size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun painterFor(state: Message.State): Painter =
    painterResource(
        id = when (state) {
            is Message.State.Sending -> R.drawable.ic_kaleyra_clock
            is Message.State.Sent -> R.drawable.ic_kaleyra_single_tick
            else -> R.drawable.ic_kaleyra_double_tick
        }
    )

@Composable
private fun contentDescriptionFor(state: Message.State): String =
    stringResource(
        id = when (state) {
            is Message.State.Sending -> R.string.kaleyra_chat_msg_status_pending
            is Message.State.Sent -> R.string.kaleyra_chat_msg_status_sent
            else -> R.string.kaleyra_chat_msg_status_seen
        }
    )

@Composable
internal fun ClickableMessage(item: ConversationItem.MessageItem) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = item.message.text,
        primary = item.message is Message.MyMessage
    )

    ClickableText(
        text = styledMessage,
        style = MaterialTheme.typography.body2.copy(color = LocalContentColor.current),
        onClick = {
            styledMessage
                .getStringAnnotations(start = it, end = it)
                .firstOrNull()
                ?.let { annotation ->
                    when (annotation.tag) {
                        SymbolAnnotationType.LINK.name -> uriHandler.openUri(annotation.item)
                        else -> Unit
                    }
                }
        }
    )
}

@Preview
@Composable
internal fun LoadingMessagesPreview() {
    Surface {
        Messages(
            uiState = ConversationUiState(),
            onMessageScrolled = { },
            onFetchMessages = { },
            onAllMessagesScrolled = { },
            onReadAllMessages = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun EmptyMessagesPreview() {
    Surface {
        Messages(
            uiState = ConversationUiState(areMessagesInitialized = true),
            onMessageScrolled = { },
            onFetchMessages = { },
            onAllMessagesScrolled = { },
            onReadAllMessages = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun MessagesPreview() {
    val messageItem1 = ConversationItem.MessageItem(Message.MyMessage("id1", "How is going?", "11:55", MutableStateFlow(Message.State.Read)))
    val messageItem2 = ConversationItem.MessageItem(Message.OtherMessage("id2", "Hello there!", "11:45"))
    val dayItem = ConversationItem.DayItem("23 august 2022")
    val newMessagesItem = ConversationItem.NewMessagesItem(1)

    Surface {
        Messages(
            uiState = ConversationUiState(
                areMessagesInitialized = true,
                conversationItems = listOf(
                    messageItem1,
                    messageItem2,
                    newMessagesItem,
                    dayItem
                )
            ),
            onMessageScrolled = { },
            onFetchMessages = { },
            onAllMessagesScrolled = { },
            onReadAllMessages = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}