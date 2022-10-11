@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

open class ChatViewModel : CollaborationViewModel() {

    private val _phoneBox = MutableSharedFlow<PhoneBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chatBox = MutableSharedFlow<ChatBoxUI>(replay = 1, extraBufferCapacity = 1)

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)

    private val _usersDescription = MutableSharedFlow<UsersDescription>(replay = 1, extraBufferCapacity = 1)

    init {
        isCollaborationConfigured
            .filter { it }
            .onEach {
                _phoneBox.emit(CollaborationUI.phoneBox)
                _chatBox.emit(CollaborationUI.chatBox)
                _usersDescription.emit(CollaborationUI.usersDescription)
            }
            .launchIn(viewModelScope)
    }

    val usersDescription = _usersDescription.asSharedFlow()

    val phoneBox = _phoneBox.asSharedFlow()

    val call = phoneBox.flatMapLatest { it.call }.shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

    val chatBox = _chatBox.asSharedFlow()

    val chat = _chat.asSharedFlow()

    val chatBoxState = chatBox.flatMapLatest { it.state }.shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

    val messages = chat.flatMapLatest { it.messages }.shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

    val actions = chat.flatMapLatest { it.actions }.stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), initialValue = setOf())

    val participants = chat.flatMapLatest { it.participants }.shareIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5_000), replay = 1)

    fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.replayCache.firstOrNull() ?: return null
        val chat = chatBox.create(object : User {
            override val userId = userId
        })
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }
}