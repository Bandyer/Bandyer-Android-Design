@file:OptIn(ExperimentalComposeUiApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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

@Preview
@Composable
internal fun MessagesPreview() {
    val messageItem1 = ConversationItem.MessageItem(Message.MyMessage("id1", "How is going?", "11:55", MutableStateFlow(Message.State.Read)))
    val messageItem2 = ConversationItem.MessageItem(Message.OtherMessage("id2", "Hello there!", "11:45"))
    val dayItem = ConversationItem.DayItem("23 august 2022")
    val newMessagesItem = ConversationItem.NewMessagesItem(3)

    Surface {
        Column {
            Messages(
                items = listOf(newMessagesItem, messageItem1, messageItem2, dayItem),
                onMessageScrolled = { },
                onReachingTop = { },
                scrollState = rememberLazyListState()
            )
        }
    }
}


@Composable
internal fun Messages(
    items: List<ConversationItem>,
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onReachingTop: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(scrollState.firstVisibleItemIndex) {
        val item = items.getOrNull(scrollState.firstVisibleItemIndex) as? ConversationItem.MessageItem ?: return@LaunchedEffect
        onMessageScrolled(item)
    }

    LaunchedEffect(scrollState.isReachingTop) {
        if (scrollState.isReachingTop) onReachingTop()
    }

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
    val horizontalArrangement = if (messageItem.message is Message.MyMessage) Arrangement.End else Arrangement.Start

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