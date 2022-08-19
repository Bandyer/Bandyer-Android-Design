package com.kaleyra.collaboration_suite_core_ui

import android.net.Uri
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.chatbox.OtherMessage
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.Message.Companion.toUiMessage
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.mapToStateFlow
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
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
open class ChatViewModel : CollaborationViewModel(), ComposeChatViewModel {

    companion object {
        private const val FETCH_COUNT = 50
    }

    private val _phoneBox = MutableSharedFlow<PhoneBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chatBox = MutableSharedFlow<ChatBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    private val _usersDescription =
        MutableSharedFlow<UsersDescription>(replay = 1, extraBufferCapacity = 1)

    var usersDescription = UsersDescription()
        private set

    init {
        isCollaborationConfigured
            .filter { it }
            .onEach {
                _phoneBox.emit(CollaborationUI.phoneBox)
                _chatBox.emit(CollaborationUI.chatBox)
                _usersDescription.emit(CollaborationUI.usersDescription)
                usersDescription = CollaborationUI.usersDescription
            }
            .launchIn(viewModelScope)
    }

    // Call
    val phoneBox = _phoneBox.asSharedFlow()

    val call = phoneBox.flatMapLatest { it.call }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // Chat
    val chatBox = _chatBox.asSharedFlow()

    val chat = _chat.asSharedFlow()

    val chatBoxState = chatBox.flatMapLatest { it.state }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val messages = chat.flatMapLatest { it.messages }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val actions = chat.flatMapLatest { it.actions }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    val participants = chat.flatMapLatest { it.participants }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    private val _firstUnreadMessageId = messages.map { it.other }.take(1).map { messages ->
        messages.forEachIndexed { index, message ->
            val previousMessage = messages.getOrNull(index - 1) ?: return@forEachIndexed
            if (previousMessage.state.value is Message.State.Received && message.state.value is Message.State.Read)
                return@map previousMessage.id
        }
        return@map null
    }

    private val _showUnreadHeader = MutableStateFlow(true)

    private val _unseenMessages = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        chat
            .flatMapLatest { it.messages }
            .map { it.other }
            .drop(1)
            .onEach { messages ->
                val receivedMessages =
                    messages.filter { it.state.value is Message.State.Received }.map { it.id }
                flow.value = flow.value + receivedMessages.toSet()
            }.launchIn(viewModelScope)
    }

    private val _otherParticipant = participants.map { it.others.first() }

    private val _typingEvents =
        _otherParticipant.flatMapLatest { it.events.filterIsInstance<ChatParticipant.Event.Typing>() }

    private val _otherParticipantState = _otherParticipant.flatMapLatest { it.state }

    private var previousChatBoxState: ChatBox.State? = null

    private val state = combine(
        _typingEvents,
        chatBoxState,
        _otherParticipantState
    ) { event, chatBoxState, participantState ->
        when {
            chatBoxState is ChatBox.State.Connecting && previousChatBoxState is ChatBox.State.Connected -> State.NetworkState.Offline
            chatBoxState is ChatBox.State.Connecting -> State.NetworkState.Connecting
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Online -> State.UserState.Online
            event is ChatParticipant.Event.Typing.Idle && participantState is ChatParticipant.State.Joined.Offline -> {
                val lastLogin = participantState.lastLogin
                State.UserState.Offline(
                    if (lastLogin is ChatParticipant.State.Joined.Offline.LastLogin.At)
                        Iso8601.parseTimestamp(ContextRetainer.context, lastLogin.date.time)
                    else null
                )
            }
            event is ChatParticipant.Event.Typing.Started -> State.UserState.Typing
            else -> State.None
        }.also {
            previousChatBoxState = chatBoxState
        }
    }

    private val info = combine(participants, _usersDescription) { participants, usersDescription ->
        val otherUserId = participants.others.first().userId
        Info(
            title = usersDescription.name(listOf(otherUserId)),
            image = usersDescription.image(listOf(otherUserId))
        )
    }

    override val stateInfo = combine(state, info) { state, info ->
        StateInfo(
            state,
            info
        )
    }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val chatActions = actions.map { actions ->
        mutableSetOf<Action>().apply {
            if (actions.any { it is ChatUI.Action.CreateCall && !it.preferredType.hasVideo() })
                add(Action.AudioCall)
            if (actions.any { it is ChatUI.Action.CreateCall && !it.preferredType.isVideoEnabled() })
                add(Action.AudioUpgradableCall)
            if (actions.any { it is ChatUI.Action.CreateCall && it.preferredType.isVideoEnabled() })
                add(Action.VideoCall)
        }
    }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val conversationItems =
        combine(messages.map { it.list }, _firstUnreadMessageId) { messages, firstUnreadMessageId ->
            val items = mutableListOf<ConversationItem>()
            messages.forEachIndexed { index, message ->
                val previousMessageItem = messages.getOrNull(index - 1) ?: kotlin.run {
                    items.add(ConversationItem.MessageItem(toUiMessage(viewModelScope, message), message !is OtherMessage))
                    return@forEachIndexed
                }

                if (_showUnreadHeader.value && previousMessageItem.id == firstUnreadMessageId)
                    items.add(ConversationItem.NewMessagesItem(index))

                if (!Iso8601.isSameDay(
                        message.creationDate.time,
                        previousMessageItem.creationDate.time
                    )
                )
                    items.add(
                        ConversationItem.DayItem(
                            Iso8601.parseDay(
                                ContextRetainer.context,
                                timestamp = previousMessageItem.creationDate.time
                            )
                        )
                    )

                items.add(ConversationItem.MessageItem(toUiMessage(viewModelScope, message), message !is OtherMessage))
            }
            items
        }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val unseenMessagesCount = _unseenMessages.map { it.count() }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val isCallActive = phoneBox
        .flatMapLatest { it.call }
        .flatMapLatest { it.state }
        .map { it !is Call.State.Disconnected.Ended }
        .shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    override val areMessagesFetched = messages.take(1).map { true }
        .stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, false)

    override fun readAllMessages() {
        val messages = messages.replayCache.firstOrNull() ?: return
        messages.other.forEach { it.markAsRead() }
    }

    override fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.replayCache.firstOrNull() ?: return null
        val chat = chatBox.create(object : User {
            override val userId = userId
        })
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

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        _unseenMessages.value = _unseenMessages.value - messageItem.id
    }

    override fun onAllMessagesScrolled() {
        _unseenMessages.value = setOf()
    }

    override fun call(callType: CallType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User {
            override val userId = userId
        })) {
            preferredType = callType.preferredType
        }
    }

    override fun showCall() = CollaborationUI.phoneBox.showCall()
}

data class Message(
    val id: String,
    val text: String,
    val time: String,
    val state: StateFlow<State>
) {
    companion object {
        fun toUiMessage(coroutineScope: CoroutineScope, message: Message) =
            Message(
                id = message.id,
                text = (message.content as? Message.Content.Text)?.message ?: "",
                time = Iso8601.parseTime(message.creationDate.time),
                state = message.state.mapToStateFlow(coroutineScope = coroutineScope) { state ->
                    when (state) {
                        is Message.State.Sending -> State.Sending
                        is Message.State.Sent -> State.Sent
                        else -> State.Read
                    }
                }
            )
    }

    sealed class State {
        object Sending : State()
        object Sent : State()
        object Read : State()
    }
}

sealed class ConversationItem(val id: String) {
    data class DayItem(val timestamp: String) :
        ConversationItem(id = timestamp.hashCode().toString())

    data class NewMessagesItem(val count: Int) : ConversationItem(id = UUID.randomUUID().toString())
    data class MessageItem(
        val message: com.kaleyra.collaboration_suite_core_ui.Message,
        val isMine: Boolean
    ) : ConversationItem(id = message.id)
}

sealed class CallType(val preferredType: Call.PreferredType) {
    object Audio : CallType(Call.PreferredType(video = null))
    object AudioUpgradable : CallType(Call.PreferredType(video = Call.Video.Disabled))
    object Video : CallType(Call.PreferredType())
}

sealed class Action {
    object AudioCall : Action()
    object AudioUpgradableCall : Action()
    object VideoCall : Action()
}

sealed class State {
    sealed class NetworkState : State() {
        object Connecting : NetworkState()
        object Offline : NetworkState()
    }

    sealed class UserState : State() {
        object Online : UserState()
        data class Offline(val timestamp: String?) : UserState()
        object Typing : UserState()
    }

    object None : State()
}

data class Info(
    val title: String,
    val image: Uri
) {
    companion object {
        val Empty = Info("", Uri.EMPTY)
    }
}

typealias StateInfo = Pair<State, Info>

interface ComposeChatViewModel {

    val stateInfo: SharedFlow<StateInfo>

    val chatActions: SharedFlow<Set<Action>>

    val conversationItems: SharedFlow<List<ConversationItem>>

    val unseenMessagesCount: SharedFlow<Int>

    val isCallActive: SharedFlow<Boolean>

    val areMessagesFetched: StateFlow<Boolean>

    fun setChat(userId: String): ChatUI?

    fun readAllMessages()

    fun sendMessage(text: String)

    fun fetchMessages()

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem)

    fun onAllMessagesScrolled()

    fun call(callType: CallType)

    fun showCall()
}