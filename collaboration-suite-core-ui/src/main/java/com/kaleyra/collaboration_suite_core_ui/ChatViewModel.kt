package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

open class ChatViewModel: CollaborationViewModel() {

    private val _phoneBox = MutableSharedFlow<PhoneBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chatBox = MutableSharedFlow<ChatBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    // UsersDescription
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

    // PhoneBox
    val phoneBox = _phoneBox.asSharedFlow()

    val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    // ChatBox
    val chatBox = _chatBox.asSharedFlow()

    val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    val chat = _chat.asSharedFlow()

    val actions = chat.flatMapLatest { it.actions }.stateIn(scope = viewModelScope, started = SharingStarted.Eagerly, initialValue = setOf())

    val participants = chat.flatMapLatest { it.participants }.shareIn(scope = viewModelScope, started = SharingStarted.Eagerly, replay = 1)

    fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.replayCache.firstOrNull() ?: return null
        val chat = chatBox.create(object : User { override val userId = userId })
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }
}