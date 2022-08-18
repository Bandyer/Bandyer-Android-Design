package com.kaleyra.collaboration_suite_phone_ui.chat

import android.net.Uri
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFromBaseline
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite_core_ui.CallType
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.ChatInfo
import com.kaleyra.collaboration_suite_core_ui.ChatSubtitle
import com.kaleyra.collaboration_suite_core_ui.ComposeChatViewModel
import com.kaleyra.collaboration_suite_core_ui.LazyColumnItem
import com.kaleyra.collaboration_suite_core_ui.SymbolAnnotationType
import com.kaleyra.collaboration_suite_core_ui.TopBarAction
import com.kaleyra.collaboration_suite_core_ui.messageFormatter
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val FETCH_THRESHOLD = 15
private const val NO_MESSAGES_TIMEOUT = 5000L

class PhoneChatActivity : ChatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { finishAndRemoveTask() }, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: ComposeChatViewModel
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val showFab by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0
        }
    }

    val lazyColumnItems = viewModel.lazyColumnItems.collectAsState(initial = emptyList()).value
    val areMessagesFetched = viewModel.areMessagesFetched.collectAsState(initial = false).value
    val chatSubtitle = viewModel.chatSubtitle.collectAsState(initial = ChatSubtitle.ChatState.Offline).value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                testTagsAsResourceId = true
            }) {
        ChatTopAppBar(
            info = viewModel.chatInfo.collectAsState(initial = ChatInfo.Empty).value,
            subtitle = chatSubtitle,
            navigationIcon = { NavigationIcon(onBackPressed = onBackPressed) },
            actions = {
                Actions(
                    actions = viewModel.topBarActions.collectAsState(initial = setOf()).value,
                    onAudioClick = { viewModel.call(CallType.Audio) },
                    onAudioUpgradableClick = { viewModel.call(CallType.AudioUpgradable) },
                    onVideoClick = { viewModel.call(CallType.Video) })
            })

        Box(Modifier.weight(1f)) {
            if (!areMessagesFetched) LoadingMessagesLabel()
            else if (lazyColumnItems.isEmpty()) NoMessagesLabel()
            else Messages(
                    items = lazyColumnItems,
                    onFetch = { viewModel.fetchMessages() },
                    scrollState = scrollState,
                    onMessageItemScrolled = { viewModel.onMessageScrolled(it) },
                    onNewMessageItems = { viewModel.markAsRead(it) },
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
                    unseenMessagesCounter = viewModel.unseenMessagesCount.collectAsState(0).value,
                    onClick = {
                        scope.launch { scrollState.scrollToItem(0) }
                        viewModel.onAllMessagesScrolled()
                    }
                )
            }

            if (viewModel.isCallActive.collectAsState(initial = false).value)
                OngoingCallLabel(onClick = { viewModel.showCall() })
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

                    override fun onSendClicked(text: String) {
                        viewModel.sendMessage(text)
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun NoMessagesLabel() {
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

@Preview
@Composable
fun LoadingMessagesLabel() {
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
fun OngoingCallLabel(onClick: () -> Unit) {
    Text(
        text = stringResource(id = R.string.kaleyra_ongoing_call_label),
        color = Color.White,
        style = MaterialTheme.typography.body2,
        modifier = Modifier
            .clickable(onClick = onClick, role = Role.Button)
            .fillMaxWidth()
            .background(
                shape = RectangleShape,
                color = colorResource(id = R.color.kaleyra_color_answer_button)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Preview
@Composable
fun FabPreview() {
    ScrollToBottomFab(unseenMessagesCounter = 5, onClick = { })
}

@Composable
fun ScrollToBottomFab(unseenMessagesCounter: Int, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        backgroundColor = MaterialTheme.colors.primary,
        modifier = Modifier.defaultMinSize(32.dp, 32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (unseenMessagesCounter > 0)
                Text(
                    text = "$unseenMessagesCounter",
                    modifier = Modifier
                        .paddingFromBaseline(bottom = 6.dp)
                        .padding(end = 4.dp)
                )
            Icon(
                painter = painterResource(id = R.drawable.ic_kaleyra_double_arrow_down),
                contentDescription = stringResource(id = R.string.kaleyra_chat_scroll_to_last_message),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

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
    val configuration = LocalConfiguration.current
    val halfScreenDp = remember {
        configuration.screenWidthDp / 2
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
                    messageItems.firstOrNull()?.message !is OtherMessage -> scrollState.scrollToItem(0)
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
//                        .animateItemPlacement()
                        .testTag("message"),
                    halfScreenDp = halfScreenDp
                )
                is LazyColumnItem.DayHeader -> DayHeader(
                    item.timestamp,
                    Modifier
                        .fillMaxWidth()
//                                .animateItemPlacement()
                        .padding(bottom = 8.dp)
                )
                is LazyColumnItem.UnreadHeader -> UnreadHeader(
                    item.unreadCount,
                    Modifier
                        .fillMaxWidth()
//                                .animateItemPlacement()
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
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
fun Message(messageItem: LazyColumnItem.Message, halfScreenDp: Int, modifier: Modifier = Modifier) {
    val message = messageItem.message
    Row(
        modifier = modifier,
        horizontalArrangement = if (message !is OtherMessage) Arrangement.End else Arrangement.Start
    ) {
        Bubble(
            isMyMessage = message !is OtherMessage,
            content = (message.content as? Message.Content.Text)?.message ?: "",
            time = messageItem.time,
            state = message.state.collectAsState().value,
            halfScreenDp = halfScreenDp
        )
    }
}

@Preview("Chat bubble")
@Composable
fun ChatBubble() {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Bubble(
                true,
                content = "Hello there!",
                time = "11:40",
                state = Message.State.Read(),
                200
            )

            Divider(color = Color.White, thickness = 16.dp)

            Bubble(
                false,
                content = "How is going? I like trains, cars and dogs. But I hate mosquitos.",
                time = "13:45",
                state = Message.State.Received(),
                200
            )
        }
    }
}

@Composable
fun Bubble(
    isMyMessage: Boolean,
    content: String,
    time: String,
    state: Message.State,
    halfScreenDp: Int
) {
    Card(
        // Do function to get the right style base on the message type
        shape = RoundedCornerShape(
            topStart = if (isMyMessage) 24.dp else 0.dp,
            topEnd = if (isMyMessage) 12.dp else 24.dp,
            bottomEnd = if (isMyMessage) 0.dp else 24.dp,
            bottomStart = if (isMyMessage) 24.dp else 12.dp,
        ),
        backgroundColor = if (isMyMessage) MaterialTheme.colors.secondary else MaterialTheme.colors.primaryVariant,
        elevation = 0.dp,
        modifier = Modifier.widthIn(min = 0.dp, max = halfScreenDp.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {

            ClickableMessage(message = content, isMyMessage = isMyMessage)

            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(text = time, fontSize = 12.sp, style = MaterialTheme.typography.body2)

                if (isMyMessage) {
                    Icon(
                        painter = when (state) {
                            is Message.State.Sending -> painterResource(id = R.drawable.ic_kaleyra_clock)
                            is Message.State.Sent -> painterResource(id = R.drawable.ic_kaleyra_single_tick)
                            else -> painterResource(id = R.drawable.ic_kaleyra_double_tick)
                        },
                        contentDescription = when (state) {
                            is Message.State.Sending -> stringResource(id = R.string.kaleyra_chat_msg_status_pending)
                            is Message.State.Sent -> stringResource(id = R.string.kaleyra_chat_msg_status_sent)
                            else -> stringResource(id = R.string.kaleyra_chat_msg_status_seen)
                        },
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
fun ClickableMessage(
    message: String,
    isMyMessage: Boolean
) {
    val uriHandler = LocalUriHandler.current

    val styledMessage = messageFormatter(
        text = message,
        primary = !isMyMessage
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

@Composable
fun ChatTopAppBar(
    info: ChatInfo,
    subtitle: ChatSubtitle?,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.high) {
            Row(
                Modifier.padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = navigationIcon
            )

            Row(
                Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        val themeResId =
                            it.theme.getAttributeResourceId(R.attr.kaleyra_chatInfoWidgetStyle)
                        KaleyraChatInfoWidget(ContextThemeWrapper(it, themeResId))
                    },
                    update = {
                        it.contactNameView!!.text = info.title
                        it.contactNameView!!.visibility = View.VISIBLE
                        it.state = when {
                            subtitle is ChatSubtitle.ChatState.Offline -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK()
                            subtitle is ChatSubtitle.ChatState.Connecting -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.CONNECTING()
                            subtitle is ChatSubtitle.ParticipantState.Online -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.ONLINE()
                            subtitle is ChatSubtitle.ParticipantState.Offline && subtitle.timestamp != null -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.OFFLINE(subtitle.timestamp)
                            subtitle is ChatSubtitle.ParticipantState.Typing -> KaleyraChatInfoWidget.KaleyraChatInfoWidgetState.TYPING()
                            else -> null
                        }
                        it.contactStatusView!!.visibility = View.VISIBLE
                        if (info.image != Uri.EMPTY) it.contactImageView!!.setImageUri(info.image)
                    }
                )
            }

            Row(
                Modifier.fillMaxHeight(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                content = actions
            )
        }
    }
}

@Composable
fun NavigationIcon(modifier: Modifier = Modifier, onBackPressed: () -> Unit) {
    IconButton(modifier = modifier, onClick = onBackPressed) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = stringResource(id = R.string.kaleyra_back)
        )
    }
}

@Composable
fun Actions(
    actions: Set<TopBarAction>,
    onAudioClick: () -> Unit,
    onAudioUpgradableClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    if (actions.any { it is TopBarAction.AudioCall })
        AudioCallAction(onClick = onAudioClick)
    if (actions.any { it is TopBarAction.AudioUpgradableCall })
        AudioUpgradableCallAction(onClick = onAudioUpgradableClick)
    if (actions.any { it is TopBarAction.VideoCall })
        VideoCallAction(onClick = onVideoClick)
}

@Composable
fun AudioCallAction(onClick: () -> Unit) {
    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_audio_call),
        onClick = onClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
    )
}

@Composable
fun AudioUpgradableCallAction(onClick: () -> Unit) {
    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
        onClick = onClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
    )
}

@Composable
fun VideoCallAction(onClick: () -> Unit) {
    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_video_call),
        onClick = onClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_video_call)
    )
}

@Composable
fun MenuIcon(painter: Painter, onClick: () -> Unit, contentDescription: String) {
    Icon(
        painter = painter,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = contentDescription
    )
}
