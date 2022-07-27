package com.kaleyra.collaboration_suite_phone_ui.chat

import android.os.Bundle
import android.view.ContextThemeWrapper
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getScreenSize
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.chat.layout.KaleyraChatTextMessageLayout
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInfoWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatInputLayoutWidget
import com.kaleyra.collaboration_suite_phone_ui.chat.widgets.KaleyraChatUnreadMessagesWidget
import com.kaleyra.collaboration_suite_phone_ui.extensions.getAttributeResourceId
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

private fun mockMessage() = object : Message {
    override val content: Message.Content = Message.Content.Text("Ciao")
    override val creationDate: Date = Date()
    override val creator: ChatParticipant = object : ChatParticipant {
        override val events: StateFlow<ChatParticipant.Event> =
            MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        override val state: StateFlow<ChatParticipant.State> =
            MutableStateFlow(ChatParticipant.State.Invited)
        override val userId: String = UUID.randomUUID().toString()
    }
    override val id: String = UUID.randomUUID().toString()
    override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Received())
}

private var mockMessages =
    listOf(
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage(),
        mockMessage()
    )

class PhoneChatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme {
                ChatScreen(onBackPressed = { onBackPressed() })
            }
        }
    }
}

@Composable
fun ChatScreen(modifier: Modifier = Modifier, onBackPressed: () -> Unit) {
    Scaffold(
        topBar = { ChatTopAppBar(navigationIcon = { NavigationIcon(onBackPressed = onBackPressed) }) },
        modifier = modifier
    ) {
        val scrollState = rememberLazyListState()

        Box(modifier = Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                Messages(
                    messages = mockMessages,
                    scrollState = scrollState,
                    modifier = Modifier.weight(1f),
                )

                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = {
                        val themeResId =
                            it.theme.getAttributeResourceId(R.attr.kaleyra_chatInputWidgetStyle)
                        KaleyraChatInputLayoutWidget(ContextThemeWrapper(it, themeResId))
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Messages(
    messages: List<Message>,
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

        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(messages, key = { it.id }) { item ->
                Row {
                    AndroidView(
                        modifier = modifier,
                        factory = {
                            val style =
                                if (item is OtherMessage) R.style.KaleyraCollaborationSuiteUI_ChatMessage_OtherUser else R.style.KaleyraCollaborationSuiteUI_ChatMessage_LoggedUser
                            KaleyraChatTextMessageLayout(ContextThemeWrapper(it, style))
                        },
                        update = { binding ->
                            binding.messageTextView!!.text =
                                (item.content as? Message.Content.Text)?.message
                            binding.messageTextView!!.maxWidth =
                                binding.context.getScreenSize().x / 2
                            binding.timestampTextView!!.text =
                                Iso8601.parseTime(item.creationDate.time)
//                            item.state.onEach {
//                                binding.statusView!!.state =
//                                    KaleyraChatMessageStatusImageView.State.SEEN
//                            }.launchIn(coroutineScope)
//                            when (item.state.value) {
//                                is Message.State.Received(
//                                    )
//                                -> {
//
//                                }
//                            }
                        }
                    )
                }
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
fun ChatTopAppBar(
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)
) {
    TopAppBar(
        modifier = modifier
    ) {
        Row(Modifier.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
            CompositionLocalProvider(
                LocalContentAlpha provides ContentAlpha.high,
                content = navigationIcon
            )
        }

        AndroidView(
            factory = {
                val themeResId = it.theme.getAttributeResourceId(R.attr.kaleyra_chatInfoWidgetStyle)
                KaleyraChatInfoWidget(ContextThemeWrapper(it, themeResId))
            },
            update = {

            }
        )
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
