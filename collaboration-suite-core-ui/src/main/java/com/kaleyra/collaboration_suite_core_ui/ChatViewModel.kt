package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ChatViewModel: CollaborationViewModel(), IChatViewModel {

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
    override val phoneBox = _phoneBox.asSharedFlow()

    override val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // Chat
    override val chatBox = _chatBox.asSharedFlow()

    override val chat = _chat.asSharedFlow()

    override val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val messages = chat.flatMapLatest { it.messages }.map { it.list }.map { messages ->
        messages.map {
            MessageCompose(it, Iso8601.parseDay(ContextRetainer.context, it.creationDate.time))
        }
    }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val actions = chat.flatMapLatest { it.actions }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    override val participants = chat.flatMapLatest { it.participants }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)


    override fun markAsRead(message: MessageCompose) {
        val msg = message.message
        if (msg as? OtherMessage == null || msg.state.value is Message.State.Read) return
        msg.markAsRead()
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

data class MessageCompose(
    val message: Message,
    val parsedDay: String
)

interface IChatViewModel {

    val usersDescription: UsersDescription

    val phoneBox: SharedFlow<PhoneBoxUI>

    val call: SharedFlow<CallUI>

    val chatBox: SharedFlow<ChatBoxUI>

    val chat: SharedFlow<ChatUI>

    val chatBoxState: SharedFlow<ChatBox.State>

    val messages: SharedFlow<List<MessageCompose>>

    val actions: SharedFlow<Set<ChatUI.Action>>

    val participants: SharedFlow<ChatParticipants>

    fun setChat(userId: String): ChatUI?

    fun markAsRead(message: MessageCompose)

    fun sendMessage(text: String)

    fun fetchMessages()

    fun call(preferredType: Call.PreferredType)
}