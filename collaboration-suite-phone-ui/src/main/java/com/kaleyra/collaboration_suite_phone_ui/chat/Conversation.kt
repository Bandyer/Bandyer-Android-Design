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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite_core_ui.LazyColumnItem
import com.kaleyra.collaboration_suite_phone_ui.R
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val FETCH_THRESHOLD = 15

private val OtherBubbleShape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 12.dp)
private val MyBubbleShape = RoundedCornerShape(24.dp, 12.dp, 0.dp, 24.dp)

const val MessageTestTag = "MessageTestTag"


//@Preview("Action bubble")
//@Composable
//fun ChatBubble() {
//    MaterialTheme {
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .background(Color.White)
//        ) {
//            Bubble(
//                true,
//                content = "Hello there!",
//                time = "11:40",
//                state = Message.State.Read(),
//                200
//            )
//
//            Divider(color = Color.White, thickness = 16.dp)
//
//            Bubble(
//                false,
//                content = "How is going? I like trains, cars and dogs. But I hate mosquitos.",
//                time = "13:45",
//                state = Message.State.Received(),
//                200
//            )
//        }
//    }
//}

@Composable
fun Messages(
    items: List<LazyColumnItem>,
    onFetch: () -> Unit,
    scrollState: LazyListState,
    onMessageItemScrolled: (LazyColumnItem.Message) -> Unit,
    onNewMessageItems: (List<LazyColumnItem.Message>) -> Unit,
    modifier: Modifier = Modifier
) {
    val firstVisibleItemIndex by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex
        }
    }
    // Do extensions functions on scrollState
    val shouldFetch by remember {
        derivedStateOf {
            val layoutInfo = scrollState.layoutInfo
            val totalItemsCount = layoutInfo.totalItemsCount
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            totalItemsCount.let {
                it != 0 && it <= (visibleItemsInfo.lastOrNull()?.index ?: 0) + FETCH_THRESHOLD
            }
        }
    }

    LaunchedEffect(firstVisibleItemIndex) {
        snapshotFlow { firstVisibleItemIndex }
            .onEach {
                val item = items.getOrNull(it) as? LazyColumnItem.Message ?: return@onEach
                onMessageItemScrolled(item)
            }.launchIn(this)
    }


    LaunchedEffect(shouldFetch) {
        snapshotFlow { shouldFetch }
            .filter { it }
            .onEach { onFetch.invoke() }
            .launchIn(this)
    }

    LaunchedEffect(items) {
        snapshotFlow { items }
            .onEach { items ->
                val messageItems = items.filterIsInstance<LazyColumnItem.Message>()
                onNewMessageItems(messageItems)
                when {
                    firstVisibleItemIndex < 3 -> scrollState.animateScrollToItem(0)
                    messageItems.firstOrNull()?.message !is OtherMessage -> scrollState.scrollToItem(
                        0
                    )
                }
            }.launchIn(this)
    }

    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.testTag("lazyColumnMessages")
    ) {
        items(items, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is LazyColumnItem.Message -> Message(
                    item,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag(MessageTestTag)
                )
                is LazyColumnItem.DayHeader -> DayHeader(
                    item.timestamp,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                is LazyColumnItem.UnreadHeader -> UnreadHeader(
                    item.unreadCount,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@Composable
fun UnreadHeader(count: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Text(
            text = pluralStringResource(id = R.plurals.kaleyra_chat_unread_messages, count, count),
            fontSize = 12.sp,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
fun DayHeader(timestamp: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
        Text(text = timestamp, fontSize = 12.sp, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun Message(messageItem: LazyColumnItem.Message, modifier: Modifier = Modifier) {
    val horizontalArrangement = if (messageItem.message !is OtherMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        content = { Bubble(messageItem) }
    )
}

@Composable
fun Bubble(messageItem: LazyColumnItem.Message) {
    val configuration = LocalConfiguration.current

    val isMyMessage = messageItem.message !is OtherMessage

    val messageState = messageItem.message.state.collectAsState().value

    Card(
        shape = if (isMyMessage) MyBubbleShape else OtherBubbleShape,
        backgroundColor = if (isMyMessage) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant,
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
                    text = messageItem.time,
                    fontSize = 12.sp,
                    style = MaterialTheme.typography.body2
                )

                if (isMyMessage) {
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
fun ClickableMessage(item: LazyColumnItem.Message) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = (item.message.content as? Message.Content.Text)?.message ?: "",
        primary = item.message !is OtherMessage
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