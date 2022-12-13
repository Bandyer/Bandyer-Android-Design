package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

open class ChatViewModel(configure: suspend () -> Configuration) : CollaborationViewModel(configure) {

    private val _chat = MutableSharedFlow<ChatUI>(replay = 1, extraBufferCapacity = 1)
    val chat = _chat.asSharedFlow()

    val call = phoneBox.flatMapLatest { it.call }.shareInEagerly(viewModelScope)

    val messages = chat.flatMapLatest { it.messages }.shareInEagerly(viewModelScope)

    val actions = chat.flatMapLatest { it.actions }.shareInEagerly(viewModelScope)

    val participants = chat.flatMapLatest { it.participants }.shareInEagerly(viewModelScope)

    fun setChat(userId: String): ChatUI? {
        val chatBox = chatBox.getValue() ?: return null
        val chat = chatBox.activeChats.replayCache.firstOrNull()?.firstOrNull { it.participants.value.others.all { it.userId == userId } } ?: chatBox.create(listOf(userId)).getOrNull() ?: return null
        viewModelScope.launch { _chat.emit(chat) }
        return chat
    }
}