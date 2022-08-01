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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatActivity
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.KotlinConstraintSet.Companion.changeConstraints
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getScreenSize
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.imageviews.KaleyraChatMessageStatusImageView
import com.kaleyra.collaboration_suite_phone_ui.chat.layout.KaleyraChatTextMessageLayout
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutEventListener
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatUnreadMessagesWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

private const val FETCH_THRESHOLD = 20

class PhoneChatActivity : ChatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                ChatScreen(onBackPressed = { onBackPressed() }, viewModel = viewModel)
            }
        }
    }
}

@Composable
fun ChatScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    viewModel: ChatViewModel
) {
    Scaffold(
        topBar = {
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
        },
        modifier = modifier
    ) {
        val scope = rememberCoroutineScope()
        val scrollState = rememberLazyListState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Messages(
                    messages = viewModel.messages.collectAsState(initial = listOf()).value,
                    onFetch = { viewModel.fetchMessages() },
                    scrollState = scrollState,
                    modifier = Modifier.weight(1f),
                )

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
                                scope.launch { scrollState.animateScrollToItem(0) }
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalFoundationApi::class)
@Composable
fun Messages(
    messages: List<Message>,
    onFetch: () -> Unit,
    scrollState: LazyListState,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        val scope = rememberCoroutineScope()
        val showFab by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex > 0
            }
        }

        val shouldFetch by remember {
            derivedStateOf {
                scrollState.layoutInfo.totalItemsCount.let { it != 0 && it <= (scrollState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + FETCH_THRESHOLD }
            }
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
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            for(index in messages.indices) {
                val message = messages[index]
                val previousMessage = messages.getOrNull(index - 1)

                if (previousMessage != null) {
                    val day = Iso8601.parseDay(timestamp = message.creationDate.time)
                    val previousMessageDay = Iso8601.parseDay(timestamp = previousMessage.creationDate.time)

                    if (previousMessageDay != day) {
                        item(key = previousMessageDay.hashCode()) {
                            Header(previousMessage.creationDate.time, Modifier.fillMaxWidth())
                        }
                    }
                }

                item(key = message.id) { Message(message) }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {

            AnimatedVisibility(
                visible = showFab,
                enter = scaleIn(),
                exit = scaleOut()
            ) {
                AndroidView(
                    factory = { KaleyraChatUnreadMessagesWidget(it) },
                    update = { view ->
                        view.setOnClickListener {
                            scope.launch {
                                scrollState.animateScrollToItem(0)
                            }
                        }
                    }
                )
            }
        }

    }
}

@Composable
fun Header(timestamp: Long, modifier: Modifier = Modifier) {
    Row(modifier = modifier) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { MaterialTextView(ContextThemeWrapper(it, it.theme.getAttributeResourceId(R.attr.kaleyra_chatTimestampStyle))) },
            update = { it.text = Iso8601.parseDay(it.context, timestamp) }
        )
    }
}

@Composable
fun Message(message: Message) {
    val scope = rememberCoroutineScope()

    Row {
        AndroidView(
            factory = {
                val style =
                    if (message is OtherMessage) R.style.KaleyraCollaborationSuiteUI_ChatMessage_OtherUser else R.style.KaleyraCollaborationSuiteUI_ChatMessage_LoggedUser
                KaleyraChatTextMessageLayout(ContextThemeWrapper(it, style))
            },
            update = { binding ->
                binding.messageTextView!!.text = (message.content as? Message.Content.Text)?.message
                binding.messageTextView!!.maxWidth = binding.context.getScreenSize().x / 2
                binding.timestampTextView!!.text = Iso8601.parseTime(message.creationDate.time)

                binding.changeConstraints {
                    if (message is OtherMessage) binding.dataViewContainer!!.id startToStartOf binding.id
                    else binding.dataViewContainer!!.id endToEndOf binding.id
                }

                if (message is OtherMessage) return@AndroidView

                message.state.onEach {
                    binding.statusView!!.state = when (it) {
                        is Message.State.Sending -> KaleyraChatMessageStatusImageView.State.PENDING
                        is Message.State.Sent -> KaleyraChatMessageStatusImageView.State.SENT
                        is Message.State.Read -> KaleyraChatMessageStatusImageView.State.SEEN
                        else -> return@onEach
                    }
                }.launchIn(scope)
            }
        )
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
