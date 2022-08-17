/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.demo_collaboration_suite_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.composethemeadapter.MdcTheme
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.Messages
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.*
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_phone_ui.chat.ChatScreen
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.*

class MockChatViewModel : ViewModel(), ComposeChatViewModel {

    val participant1 = object : ChatParticipant {
        override val state: StateFlow<ChatParticipant.State> =
            MutableStateFlow(ChatParticipant.State.Joined.Online)
        override val events: StateFlow<ChatParticipant.Event> =
            MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        override val userId: String = "user1"
    }

    val participant2 = object : ChatParticipant {
        override val state: StateFlow<ChatParticipant.State> =
            MutableStateFlow(ChatParticipant.State.Joined.Online)
        override val events: StateFlow<ChatParticipant.Event> =
            MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        override val userId: String = "user2"
    }

    private fun newMessage(): Message {
        return object : Message {
            override val id: String = UUID.randomUUID().toString()
            override val creator: ChatParticipant = participant1
            override val creationDate: Date = Date()
            override val content: Message.Content = Message.Content.Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            override val state: StateFlow<Message.State> = MutableStateFlow(Message.State.Read())
        }
    }

    private fun newOtherMessage(): OtherMessage {
        return object : OtherMessage {
            override val id: String = UUID.randomUUID().toString()
            override val creator: ChatParticipant = participant1
            override val creationDate: Date = Date()
            override val content: Message.Content = Message.Content.Text("Lorem ipsum dolor sit amet, consectetur adipiscing elit.")
            private val _state: MutableStateFlow<Message.State> = MutableStateFlow(Message.State.Received())
            override val state: StateFlow<Message.State> = _state
            override fun markAsRead() {
                _state.value = Message.State.Read()
            }
        }
    }

    val usersDescription: UsersDescription
        get() = UsersDescription()

    val phoneBox: SharedFlow<PhoneBoxUI> = MutableSharedFlow()

    val call: SharedFlow<CallUI> = MutableSharedFlow()

    val chatBox: SharedFlow<ChatBoxUI> = MutableSharedFlow()

    val chat: SharedFlow<ChatUI> = MutableSharedFlow()

    val chatBoxState: SharedFlow<ChatBox.State> = MutableSharedFlow()

    private val _messages: MutableSharedFlow<MessagesUI> = MutableSharedFlow(replay = 1, extraBufferCapacity = 1)
    val messages: SharedFlow<MessagesUI> = _messages
    override val chatInfo: SharedFlow<ChatInfo>
        get() = TODO("Not yet implemented")
    override val chatSubtitle: SharedFlow<ChatSubtitle>
        get() = TODO("Not yet implemented")
    override val topBarActions: SharedFlow<Set<TopBarAction>>
        get() = TODO("Not yet implemented")

    override val lazyColumnItems: SharedFlow<List<LazyColumnItem>> = messages.map { it.list }.map {
        it.map {
            LazyColumnItem.Message(it, Iso8601.parseTime(it.creationDate.time))
        }
    }.shareIn(viewModelScope, SharingStarted.Eagerly, replay = 1)

    val actions: SharedFlow<Set<ChatUI.Action>> = MutableSharedFlow()

    val participants: SharedFlow<ChatParticipants> =
        MutableSharedFlow<ChatParticipants>(replay = 1, extraBufferCapacity = 1).also {
            it.tryEmit(object : ChatParticipants {
                override val me: ChatParticipant = participant1
                override val others: List<ChatParticipant> = listOf(participant2)
                override val list: List<ChatParticipant> = others + me
                override fun creator(): ChatParticipant? = null
            })
        }
    override val unseenMessagesCount: SharedFlow<Int> = MutableSharedFlow()

    override fun setChat(userId: String): ChatUI? = null

    override fun markAsRead(items: List<LazyColumnItem.Message>) = Unit

    override fun sendMessage(text: String) = Unit

    override fun fetchMessages() {
        viewModelScope.launch {
            val currentMessages = _messages.replayCache.firstOrNull()?.list ?: listOf()
            val newMessages = currentMessages + (0..50).map { if (it % 2 == 0) newMessage() else newOtherMessage() }
            val messages = object : Messages {
                override val list: List<Message> = newMessages
                override val my: List<Message> = newMessages.filter { it !is OtherMessage }
                override val other: List<OtherMessage> = newMessages.filterIsInstance<OtherMessage>()
            }
            _messages.emit(MessagesUI(messages, ChatActivity::class.java))
        }
    }

    override fun onMessageScrolled(messageItem: LazyColumnItem.Message) = Unit
    override fun onAllMessagesScrolled() {
        TODO("Not yet implemented")
    }

    override fun call(callType: CallType) {
        TODO("Not yet implemented")
    }

    fun call(preferredType: Call.PreferredType) = Unit

    init {
        fetchMessages()
    }
}

class ChatActivity : ComponentActivity() {

    private val viewModel: MockChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MdcTheme(setDefaultFontFamily = true) {
                ChatScreen(onBackPressed = { onBackPressed() }, viewModel = viewModel)
            }
        }
    }
}