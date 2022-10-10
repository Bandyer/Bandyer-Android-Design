@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalLayoutApi::class)

package com.kaleyra.collaboration_suite_phone_ui.chat.compose.conversation

import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601.isToday
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601.isYesterday
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.*
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.theme.KaleyraTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.highlightOnFocus
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private val OtherBubbleShape = RoundedCornerShape(0.dp, 24.dp, 24.dp, 12.dp)
private val MyBubbleShape = RoundedCornerShape(24.dp, 12.dp, 0.dp, 24.dp)

const val MessageStateTag = "MessageStateTag"
const val ConversationTag = "ConversationTag"

private const val TOP_THRESHOLD = 15

private val UnreadItemOffset = 56.dp
private val ScrollToBottomThreshold = 128.dp

private val LazyListState.isApproachingTop: Boolean
    get() = derivedStateOf {
        val totalItemsCount = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        totalItemsCount != 0 && totalItemsCount <= lastVisibleItemIndex + TOP_THRESHOLD
    }.value

@Composable
internal fun Messages(
    uiState: ConversationUiState,
    onDirectionLeft: (() -> Unit) = { },
    onMessageScrolled: (ConversationItem.MessageItem) -> Unit,
    onApproachingTop: () -> Unit,
    onResetScroll: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val isKeyboardOpen by isKeyboardOpen()
    val scope = rememberCoroutineScope()
    val fabRef = remember { FocusRequester() }
    val unreadItemOffset = with(LocalDensity.current) { UnreadItemOffset.toPx() }
    var isScrolledToUnreadItem by remember { mutableStateOf(false) }
    val scrollToBottomFabEnabled by scrollToBottomFabEnabled(scrollState)
    val onFabClick = remember(scope, scrollState) {
        {
            scope.launch { scrollState.scrollToItem(0) }
            onResetScroll()
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { isKeyboardOpen }.first { !it }
        val index =
            uiState.conversationItems?.value?.indexOfFirst { it is ConversationItem.UnreadMessagesItem }
                ?: -1
        if (index != -1) scrollState.scrollToBottomViewportItem(index, unreadItemOffset)
        isScrolledToUnreadItem = true
    }

    LaunchedEffect(scrollState, uiState.conversationItems, isScrolledToUnreadItem) {
        if (isScrolledToUnreadItem) {
            snapshotFlow { scrollState.firstVisibleItemIndex }
                .onEach {
                    val item =
                        uiState.conversationItems?.value?.getOrNull(it) as? ConversationItem.MessageItem
                            ?: return@onEach
                    onMessageScrolled(item)
                }.launchIn(this)
        }
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isApproachingTop }
            .filter { it }
            .onEach { onApproachingTop() }
            .launchIn(this)
    }

    LaunchedEffect(uiState.conversationItems) {
        if (scrollState.firstVisibleItemIndex < 3) scrollState.animateScrollToItem(0)
    }

    Box(
        modifier = Modifier
            .onPreviewKeyEvent {
                return@onPreviewKeyEvent when {
                    it.type != KeyEventType.KeyDown && it.key == Key.DirectionLeft -> {
                        onDirectionLeft(); true
                    }
                    scrollToBottomFabEnabled && it.type != KeyEventType.KeyDown && it.key == Key.DirectionRight -> {
                        fabRef.requestFocus(); true
                    }
                    else -> false
                }
            }
            .then(modifier)
    ) {
        if (uiState.conversationItems == null) LoadingMessagesLabel(Modifier.align(Alignment.Center))
        else if (uiState.conversationItems.value.isEmpty()) NoMessagesLabel(Modifier.align(Alignment.Center))
        else {
            Box(Modifier.alpha(if (isScrolledToUnreadItem) 1f else 0f)) {
                Conversation(
                    items = uiState.conversationItems,
                    areAllMessagesFetched = uiState.areAllMessagesFetched,
                    scrollState = scrollState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .align(Alignment.BottomCenter)
                )
            }
        }

        Box(
            Modifier
                .focusRequester(fabRef)
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            ResetScrollFab(
                counter = uiState.unreadMessagesCount,
                onClick = onFabClick,
                enabled = scrollToBottomFabEnabled
            )
        }
    }
}

private suspend fun LazyListState.scrollToBottomViewportItem(index: Int, scrollOffset: Float = 0f) {
    scrollToItem(index)
    val firstItemSize = layoutInfo.visibleItemsInfo.first().size
    val viewportSize = layoutInfo.viewportSize.height
    scrollBy(-viewportSize + firstItemSize + scrollOffset)
}

@Composable
private fun scrollToBottomFabEnabled(listState: LazyListState): State<Boolean> {
    val resetScrollThreshold = with(LocalDensity.current) { ScrollToBottomThreshold.toPx() }
    return remember {
        derivedStateOf {
            val firstCompletelyVisibleItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            val firstCompletelyVisibleItemIndex = firstCompletelyVisibleItem?.index ?: 0
            val firstCompletelyVisibleItemOffset = -(firstCompletelyVisibleItem?.offset ?: 0)
            firstCompletelyVisibleItemIndex != 0 || firstCompletelyVisibleItemOffset > resetScrollThreshold
        }
    }
}

@Composable
internal fun isKeyboardOpen(): State<Boolean> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val isResumed = lifecycle.currentState == Lifecycle.State.RESUMED
    return rememberUpdatedState(WindowInsets.isImeVisible && isResumed)
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
    items: ImmutableList<ConversationItem>,
    areAllMessagesFetched: Boolean,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .testTag(ConversationTag)
            .then(modifier)
    ) {
        items(items.value, key = { it.id }, contentType = { it::class.java }) { item ->
            when (item) {
                is ConversationItem.MessageItem -> Message(
                    messageItem = item,
                    modifier = Modifier.fillMaxWidth()
                )
                is ConversationItem.DayItem -> DayHeader(
                    timestamp = item.timestamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                is ConversationItem.UnreadMessagesItem -> NewMessagesHeader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
        }
        if (!areAllMessagesFetched) {
            item {
                CircularProgressIndicator(
                    color = LocalContentColor.current,
                    strokeWidth = 3.dp,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp)
                )
            }
        }
    }
}

@Composable
internal fun NewMessagesHeader(modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .then(modifier),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = R.string.kaleyra_chat_unread_messages),
            fontSize = 12.sp,
            style = MaterialTheme.typography.body2
        )
    }
}

@Composable
internal fun DayHeader(timestamp: Long, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .then(modifier),
        horizontalArrangement = Arrangement.Center
    ) {
        val text = when {
            timestamp.isToday() -> stringResource(id = R.string.kaleyra_today)
            timestamp.isYesterday() -> stringResource(id = R.string.kaleyra_yesterday)
            else -> Iso8601.parseDay(timestamp)
        }
        Text(text = text, fontSize = 12.sp, style = MaterialTheme.typography.body2)
    }
}

@Composable
internal fun Message(messageItem: ConversationItem.MessageItem, modifier: Modifier = Modifier) {
    val interactionSource = remember { MutableInteractionSource() }
    val horizontalArrangement =
        if (messageItem.message is Message.MyMessage) Arrangement.End else Arrangement.Start

    Row(
        modifier = Modifier
            .focusable(true, interactionSource)
            .highlightOnFocus(interactionSource)
            .then(modifier),
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
                    val messageState = messageItem.message.state.collectAsState(Message.State.Sending).value

                    Icon(
                        painter = painterFor(messageState),
                        contentDescription = contentDescriptionFor(messageState),
                        modifier = Modifier
                            .padding(2.dp)
                            .size(16.dp)
                            .testTag(MessageStateTag)
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
        primary = item.message !is Message.MyMessage
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
internal fun LoadingMessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun LoadingMessagesDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun EmptyMessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(conversationItems = ImmutableList(listOf())),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun EmptyMessagesDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(conversationItems = ImmutableList(listOf())),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun MessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(conversationItems = mockConversationItems),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview
@Composable
internal fun MessagesDarkPreview() = KaleyraTheme(isDarkTheme = true) {
    Surface(color = MaterialTheme.colors.background) {
        Messages(
            uiState = ConversationUiState(conversationItems = mockConversationItems),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}