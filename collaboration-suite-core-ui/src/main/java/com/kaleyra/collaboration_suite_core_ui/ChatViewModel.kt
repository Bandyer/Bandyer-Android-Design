package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.*

open class ChatViewModel : CollaborationViewModel(), IChatViewModel {

    companion object {
        private val FETCH_COUNT = 50
    }

    private val _phoneBox = MutableSharedFlow<PhoneBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chatBox = MutableSharedFlow<ChatBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    final override var usersDescription = UsersDescription()
        private set

    init {
        isCollaborationConfigured
            .filter { it }
            .onEach {
                _phoneBox.emit(CollaborationUI.phoneBox)
                _chatBox.emit(CollaborationUI.chatBox)
                usersDescription = CollaborationUI.usersDescription
            }
            .launchIn(viewModelScope)
    }

    // Call
    final override val phoneBox = _phoneBox.asSharedFlow()

    override val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // Chat
    final override val chatBox = _chatBox.asSharedFlow()

    final override val chat = _chat.asSharedFlow()

    override val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val unreadMessageId =
        chat.flatMapLatest { it.messages }.map { it.other }.take(1).map { messages ->
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index - 1) ?: return@forEachIndexed
                if (previousMessage.state.value is Message.State.Received && message.state.value is Message.State.Read)
                    return@map previousMessage.id
            }
            return@map null
        }

    val messageList = chat.flatMapLatest { it.messages }.map { it.list }

    val messageSent = MutableStateFlow(false)

    // TODO Fare stato della ui in cui metto questo in uno stato
    override val lazyColumnItems =
        combine(messageList, unreadMessageId) { messages, unreadMessageId ->
            val items = mutableListOf<LazyColumnItem>()
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index - 1) ?: kotlin.run {
                    items.add(
                        LazyColumnItem.Message(
                            message,
                            Iso8601.parseTime(message.creationDate.time)
                        )
                    )
                    return@forEachIndexed
                }

                if (!messageSent.value && previousMessage.id == unreadMessageId)
                    items.add(LazyColumnItem.UnreadHeader(index))

                if (Iso8601.parseDay(timestamp = message.creationDate.time) != Iso8601.parseDay(
                        timestamp = previousMessage.creationDate.time
                    )
                )
                    items.add(
                        LazyColumnItem.DayHeader(
                            Iso8601.parseDay(
                                ContextRetainer.context,
                                timestamp = previousMessage.creationDate.time
                            )
                        )
                    )

                items.add(
                    LazyColumnItem.Message(
                        message,
                        Iso8601.parseTime(message.creationDate.time)
                    )
                )
            }
            items
        }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    private val unreadMessages = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        val unreadSet = mutableSetOf<String>()
        chat
            .flatMapLatest { it.messages }
            .map { it.other }
            .drop(1)
            .onEach {
                unreadSet.addAll(it.filter { it.state.value is Message.State.Received }
                    .map { it.id }.toSet())
                flow.value = unreadSet.toSet()
            }.launchIn(viewModelScope)
    }

    private val _readMessages = MutableStateFlow<Set<String>>(setOf())

    private val _unreadMessagesCounter =
        MutableSharedFlow<Int>(replay = 1, extraBufferCapacity = 1).also { flow ->
            combine(unreadMessages, _readMessages) { unreadMsgs, readMsgs ->
                val diff = unreadMsgs - readMsgs
                diff.size
            }
                .onEach { flow.emit(it) }
                .launchIn(viewModelScope)
        }

    override val unreadMessagesCounter = _unreadMessagesCounter.asSharedFlow()

    override val actions = chat.flatMapLatest { it.actions }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    override val participants = chat.flatMapLatest { it.participants }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override fun markAsRead(message: OtherMessage) {
        if (message.state.value is Message.State.Read) return
        message.markAsRead()
    }

    override fun removeUnreadMessage(messageId: String) {
        viewModelScope.launch {
            _readMessages.value = _readMessages.value + messageId
        }
    }

    override fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.replayCache.firstOrNull() ?: return null
        val chat = chatBox.create(object : User { override val userId = userId })
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }

    override fun sendMessage(text: String) {
        val chat = chat.replayCache.firstOrNull() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
        messageSent.value = true
    }

    override fun fetchMessages() {
        val chat = chat.replayCache.firstOrNull() ?: return
        chat.fetch(FETCH_COUNT)
    }

    override fun call(preferredType: Call.PreferredType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User { override val userId = userId })) {
           this.preferredType = preferredType
        }
    }
}

sealed class LazyColumnItem(val id: String) {
    data class DayHeader(val timestamp: String): LazyColumnItem(timestamp.hashCode().toString())
    data class UnreadHeader(val unreadCount: Int): LazyColumnItem(UUID.randomUUID().toString())
    data class Message(val message: com.kaleyra.collaboration_suite.chatbox.Message, val time: String): LazyColumnItem(message.id)
}

interface IChatViewModel {

    val usersDescription: UsersDescription

    val phoneBox: SharedFlow<PhoneBoxUI>

    val call: SharedFlow<CallUI>

    val chatBox: SharedFlow<ChatBoxUI>

    val chat: SharedFlow<ChatUI>

    val chatBoxState: SharedFlow<ChatBox.State>

    val lazyColumnItems: SharedFlow<List<LazyColumnItem>>

    val actions: SharedFlow<Set<ChatUI.Action>>

    val participants: SharedFlow<ChatParticipants>

    val unreadMessagesCounter: SharedFlow<Int>

    fun setChat(userId: String): ChatUI?

    fun markAsRead(message: OtherMessage)

    fun sendMessage(text: String)

    fun fetchMessages()

    fun call(preferredType: Call.PreferredType)
    fun removeUnreadMessage(messageId: String)
}