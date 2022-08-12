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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalCoroutinesApi::class)
open class ChatViewModel : CollaborationViewModel(), IChatViewModel {

    companion object {
        private const val FETCH_COUNT = 50
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

    final override val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // Chat
    final override val chatBox = _chatBox.asSharedFlow()

    final override val chat = _chat.asSharedFlow()

    final override val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    final override val messages = chat.flatMapLatest { it.messages }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    final override val actions = chat.flatMapLatest { it.actions }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    final override val participants = chat.flatMapLatest { it.participants }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    private val _firstUnreadMessageId = messages.map { it.other }.take(1).map { messages ->
        messages.forEachIndexed { index, message ->
            val previousMessage = messages.getOrNull(index - 1) ?: return@forEachIndexed
            if (previousMessage.state.value is Message.State.Received && message.state.value is Message.State.Read)
                return@map previousMessage.id
        }
        return@map null
    }

    private val _showUnreadHeader = MutableStateFlow(true)

    override val lazyColumnItems = combine(messages.map { it.list }, _firstUnreadMessageId) { messages, firstUnreadMessageId ->
            val items = mutableListOf<LazyColumnItem>()
            messages.forEachIndexed { index, message ->
                val previousMessage = messages.getOrNull(index - 1) ?: kotlin.run {
                    items.add(LazyColumnItem.Message(message, Iso8601.parseTime(message.creationDate.time)))
                    return@forEachIndexed
                }

                if (_showUnreadHeader.value && previousMessage.id == firstUnreadMessageId)
                    items.add(LazyColumnItem.UnreadHeader(index))

                if (Iso8601.parseDay(timestamp = message.creationDate.time) != Iso8601.parseDay(timestamp = previousMessage.creationDate.time))
                    items.add(LazyColumnItem.DayHeader(Iso8601.parseDay(ContextRetainer.context, timestamp = previousMessage.creationDate.time)))

                items.add(LazyColumnItem.Message(message, Iso8601.parseTime(message.creationDate.time)))
            }
            items
        }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    private val _unseenMessages = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        chat
            .flatMapLatest { it.messages }
            .map { it.other }
            .drop(1)
            .onEach { messages ->
                val receivedMessages = messages.filter { it.state.value is Message.State.Received }.map { it.id }
                flow.value = flow.value + receivedMessages.toSet()
            }.launchIn(viewModelScope)
    }

    override val unseenMessagesCount = _unseenMessages.map { it.count() }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override fun markAsRead(items: List<LazyColumnItem.Message>) =
        items.forEach {
            val otherMsg = it.message as? OtherMessage ?: return@forEach
            otherMsg.markAsRead()
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
        _showUnreadHeader.value = false
    }

    override fun fetchMessages() {
        chat.replayCache.firstOrNull()?.fetch(FETCH_COUNT)
    }

    override fun onMessageScrolled(messageItem: LazyColumnItem.Message) {
        _unseenMessages.value = _unseenMessages.value - messageItem.id
    }

    override fun onAllMessagesScrolled() {
        _unseenMessages.value = setOf()
    }

    override fun call(callType: CallType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User { override val userId = userId })) {
            preferredType = callType.preferredType
        }
    }
}

sealed class LazyColumnItem(val id: String) {
    data class DayHeader(val timestamp: String): LazyColumnItem(timestamp.hashCode().toString())
    data class UnreadHeader(val unreadCount: Int): LazyColumnItem(UUID.randomUUID().toString())
    data class Message(val message: com.kaleyra.collaboration_suite.chatbox.Message, val time: String): LazyColumnItem(message.id)
}

sealed class CallType(val preferredType: Call.PreferredType) {
    object Audio: CallType(Call.PreferredType(video = null))
    object AudioUpgradable: CallType(Call.PreferredType(video = Call.Video.Disabled))
    object Video: CallType(Call.PreferredType())
}

interface IChatViewModel {

    val usersDescription: UsersDescription

    val phoneBox: SharedFlow<PhoneBoxUI>

    val call: SharedFlow<CallUI>

    val chatBox: SharedFlow<ChatBoxUI>

    val chat: SharedFlow<ChatUI>

    val chatBoxState: SharedFlow<ChatBox.State>

    val messages: SharedFlow<MessagesUI>

    val lazyColumnItems: SharedFlow<List<LazyColumnItem>>

    val actions: SharedFlow<Set<ChatUI.Action>>

    val participants: SharedFlow<ChatParticipants>

    val unseenMessagesCount: SharedFlow<Int>

    fun setChat(userId: String): ChatUI?

    fun markAsRead(items: List<LazyColumnItem.Message>)

    fun sendMessage(text: String)

    fun fetchMessages()

    fun onMessageScrolled(messageItem: LazyColumnItem.Message)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)
}