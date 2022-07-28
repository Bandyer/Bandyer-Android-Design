package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

open class ChatViewModel: CollaborationViewModel() {

    companion object {
        private val FETCH_COUNT = 50
    }

    private val _phoneBox = MutableSharedFlow<PhoneBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chatBox = MutableSharedFlow<ChatBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    private val _isFetching = MutableStateFlow(false)

    var usersDescription = UsersDescription()
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
    val phoneBox = _phoneBox.asSharedFlow()

    val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // Chat
    val chatBox = _chatBox.asSharedFlow()

    val chat = _chat.asSharedFlow()

    val isFetching = _isFetching.asStateFlow()

    val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val messages = chat.flatMapLatest { it.messages }.map { it.list }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val actions = chat.flatMapLatest { it.actions }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    val participants = chat.flatMapLatest { it.participants }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)


    fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.replayCache.firstOrNull() ?: return null
        val chat = chatBox.create(object : User { override val userId = userId })
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }

    fun sendMessage(text: String) {
        val chat = chat.replayCache.firstOrNull() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
    }

    fun fetchMessages() {
        val chat = chat.replayCache.firstOrNull() ?: return
        _isFetching.value = true
        chat.fetch(FETCH_COUNT) { _isFetching.value = false }
    }

    fun call(preferredType: Call.PreferredType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User { override val userId = userId })) {
           this.preferredType = preferredType
        }
    }
}