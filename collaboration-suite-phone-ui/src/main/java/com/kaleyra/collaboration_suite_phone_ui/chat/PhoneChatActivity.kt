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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Divider
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.IChatViewModel
import com.kaleyra.collaboration_suite_core_ui.MessageCompose
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatUnreadMessagesWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val FETCH_THRESHOLD = 5

class PhoneChatActivity : ChatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { onBackPressed() }, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
@Composable
fun ChatScreen(
    onBackPressed: () -> Unit,
    viewModel: IChatViewModel
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberLazyListState()
    val showFab by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex > 0
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .semantics {
                testTagsAsResourceId = true
            }) {
        ChatTopAppBar(
            viewModel.participants.replayCache.first().others.first().userId,
            viewModel.usersDescription,
            navigationIcon = { NavigationIcon(onBackPressed = onBackPressed) },
            actions = {
                Actions(
                    onAudioClick = {
                        viewModel.call(
                            Call.PreferredType(
                                audio = Call.Audio.Enabled,
                                video = null
                            )
                        )
                    },
                    onAudioUpgradableClick = {
                        viewModel.call(
                            Call.PreferredType(
                                audio = Call.Audio.Enabled,
                                video = Call.Video.Disabled
                            )
                        )
                    },
                    onVideoClick = {
                        viewModel.call(
                            Call.PreferredType(
                                audio = Call.Audio.Enabled,
                                video = Call.Video.Enabled
                            )
                        )
                    })
            })

        Box(Modifier.weight(1f)) {
            Messages(
                messages = viewModel.messages.collectAsState(initial = listOf()).value,
                onFetch = { viewModel.fetchMessages() },
                scrollState = scrollState,
                modifier = Modifier.fillMaxSize(),
            )

//            this@Column.AnimatedVisibility(
//                visible = showFab,
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(16.dp),
//                enter = scaleIn(),
//                exit = scaleOut()
//            ) {
//                FloatingActionButton(
//                    modifier = Modifier.size(40.dp),
//                    onClick = {
//                        scope.launch {
//                            scrollState.scrollToItem(0)
//                        }
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Filled.ArrowDropDown,
//                        contentDescription = null
//                    )
//                }
//            }
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
                        scope.launch { scrollState.scrollToItem(0) }
                    }
                }
            }
        )
    }
}

@Composable
fun Messages(
    messages: List<MessageCompose>,
    onFetch: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
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

    LaunchedEffect(shouldFetch) {
        snapshotFlow { shouldFetch }
            .filter { it }
            .onEach { onFetch.invoke() }
            .launchIn(this)
    }

    LazyColumn(
        reverseLayout = true,
        state = scrollState,
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.testTag("lazyColumnMessages")
    ) {
        for (index in messages.indices) {
            val message = messages[index]
            val previousMessage = messages.getOrNull(index - 1)

            if (previousMessage != null) {
                val previousTimeHash = previousMessage.parsedDay.hashCode()
                if (previousTimeHash != message.parsedDay.hashCode()) {
                    item(key = previousTimeHash) {
                        Header(
                            previousMessage.parsedDay,
                            Modifier
                                .fillMaxWidth()
                                .animateItemPlacement()
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            item(key = message.message.id) {
                Message(
                    message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItemPlacement()
                        .testTag("message"),
                    halfScreenDp = halfScreenDp
                )
            }
        }
    }
}

@Composable
fun Header(timestamp: String, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.Center) {
//        kaleyra_chatTimestampStyle
        Text(text = timestamp, fontSize = 12.sp, style = MaterialTheme.typography.body2)
    }
}

@Composable
fun Message(message: MessageCompose, halfScreenDp: Int, modifier: Modifier = Modifier) {
    val msg = message.message
    Row(
        modifier = modifier,
        horizontalArrangement = if (msg !is OtherMessage) Arrangement.End else Arrangement.Start
    ) {
        Bubble(
            isMyMessage = msg !is OtherMessage,
            content = (msg.content as? Message.Content.Text)?.message ?: "",
            time = message.parsedDay,
            state = msg.state.collectAsState().value,
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
            Text(text = content, style = MaterialTheme.typography.body2)

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
fun ChatTopAppBar(
    userId: String,
    usersDescription: UsersDescription,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit),
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.primary,
    ) {
        val scope = rememberCoroutineScope()

        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                content = navigationIcon
            )
        }

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
                    scope.launch {
                        it.contactNameView!!.text = usersDescription.name(listOf(userId))
                        it.contactNameView!!.visibility = View.VISIBLE
                        val uri = usersDescription.image(listOf(userId))
                        if (uri != Uri.EMPTY) it.contactImageView!!.setImageUri(uri)
                    }
                }
            )
        }

        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
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
    onAudioClick: () -> Unit,
    onAudioUpgradableClick: () -> Unit,
    onVideoClick: () -> Unit
) {
    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_audio_call),
        onClick = onAudioClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_audio_call)
    )

    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_audio_upgradable_call),
        onClick = onAudioUpgradableClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_audio_upgradable_call)
    )

    MenuIcon(
        painter = painterResource(R.drawable.ic_kaleyra_video_call),
        onClick = onVideoClick,
        contentDescription = stringResource(id = R.string.kaleyra_start_video_call)
    )
}

@Composable
fun MenuIcon(painter: Painter, onClick: () -> Unit, contentDescription: String) {
    Icon(
        painter = painter,
        tint = MaterialTheme.colors.onPrimary,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 16.dp)
            .height(24.dp),
        contentDescription = contentDescription
    )
}
