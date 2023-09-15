package com.kaleyra.collaboration_suite_phone_ui.chat.conversation

import android.content.res.Configuration
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.mock.mockConversationElements
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.ConversationContent
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.view.ResetScrollFab
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.theme.KaleyraTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val TOP_THRESHOLD = 15

private val ScrollToBottomThreshold = 128.dp

private val LazyListState.isApproachingTop: Boolean
    get() = derivedStateOf {
        val totalItemsCount = layoutInfo.totalItemsCount
        val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        totalItemsCount != 0 && totalItemsCount <= lastVisibleItemIndex + TOP_THRESHOLD
    }.value

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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun ConversationComponent(
    uiState: ConversationUiState,
    onDirectionLeft: (() -> Unit) = { },
    onMessageScrolled: (ConversationElement.Message) -> Unit,
    onApproachingTop: () -> Unit,
    onResetScroll: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    val screenHeight = with(LocalDensity.current) {
        LocalConfiguration.current.screenHeightDp.dp.toPx()
    }
    val scope = rememberCoroutineScope()
    val fabRef = remember { FocusRequester() }
    val scrollToBottomFabEnabled by scrollToBottomFabEnabled(scrollState)
    val onFabClick = remember(scope, scrollState) {
        {
            scope.launch { scrollState.scrollToItem(0) }
            onResetScroll()
        }
    }

    LaunchedEffect(scrollState) {
        val index = uiState.conversationElements?.value?.indexOfFirst { it is ConversationElement.UnreadMessages } ?: -1
        if (index != -1) {
            scrollState.scrollToItem(index)
            scrollState.scrollBy(-screenHeight * 2 / 3f)
        }
    }

    LaunchedEffect(scrollState, uiState.conversationElements) {
        snapshotFlow { scrollState.firstVisibleItemIndex }
            .onEach {
                val item = uiState.conversationElements?.value?.getOrNull(it) as? ConversationElement.Message ?: return@onEach
                onMessageScrolled(item)
            }.launchIn(this)
    }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.isApproachingTop }
            .filter { it }
            .onEach { onApproachingTop() }
            .launchIn(this)
    }

    LaunchedEffect(uiState.conversationElements) {
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
        if (uiState.conversationElements == null) LoadingMessagesLabel(Modifier.align(Alignment.Center))
        else if (uiState.conversationElements.value.isEmpty()) NoMessagesLabel(Modifier.align(Alignment.Center))
        else {
            ConversationContent(
                items = uiState.conversationElements,
                participantsDetails = uiState.participantsDetails,
                myMessagesStates = uiState.myMessagesStates,
                showUserDetails = uiState.isGroupChat,
                isFetching = uiState.isFetching,
                scrollState = scrollState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
            )
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

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun LoadingMessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        ConversationComponent(
            uiState = ConversationUiState(),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun EmptyMessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        ConversationComponent(
            uiState = ConversationUiState(conversationElements = ImmutableList(listOf())),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Light Mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, name = "Dark Mode")
@Composable
internal fun MessagesPreview() = KaleyraTheme {
    Surface(color = MaterialTheme.colors.background) {
        ConversationComponent(
            uiState = ConversationUiState(conversationElements = mockConversationElements),
            onMessageScrolled = { },
            onApproachingTop = { },
            onResetScroll = { },
            scrollState = rememberLazyListState(),
            modifier = Modifier.fillMaxSize()
        )
    }
}