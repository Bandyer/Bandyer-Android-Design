package com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.getChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.getChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.utility.UiModelMapper.mapToConversationItems
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure), ChatUiViewModel {

    private val showUnreadHeader = MutableStateFlow(true)

    private val isFetching = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    private val _uiState = MutableStateFlow(ChatUiState())

    override val uiState = _uiState.asStateFlow()

    init {
        getChatState(participants, chatBox).onEach { state ->
            _uiState.update { it.copy(state = state) }
        }.launchIn(viewModelScope)

        getChatInfo(participants, usersDescription).onEach { info ->
            _uiState.update { it.copy(info = info) }
        }.launchIn(viewModelScope)

        actions.map { it.mapToChatActions(call = { pt -> call(pt) }) }.onEach { actions ->
            _uiState.update { it.copy(actions = ImmutableSet(actions)) }
        }.launchIn(viewModelScope)

        phoneBox.hasActiveCall().onEach { hasActiveCall ->
            _uiState.update { it.copy(isInCall = hasActiveCall) }
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            val chat = chat.first()
            val firstUnreadMessageId = findFirstUnreadMessageId(chat.messages.first(), chat::fetch)
            messages.mapToConversationItems(firstUnreadMessageId, showUnreadHeader).collect { items ->
                _uiState.update {
                    val conversationState = it.conversationState.copy(conversationItems = ImmutableList(items))
                    it.copy(conversationState = conversationState)
                }
            }
        }

        chat.flatMapLatest { it.unreadMessagesCount }.onEach { count ->
            _uiState.update {
                val conversationState = it.conversationState.copy(unreadMessagesCount = count)
                it.copy(conversationState = conversationState)
            }
        }.launchIn(viewModelScope)

        isFetching.onEach { isFetching ->
            _uiState.update {
                val conversationState = it.conversationState.copy(isFetching = isFetching)
                it.copy(conversationState = conversationState)
            }
        }.launchIn(viewModelScope)
    }

    override fun sendMessage(text: String) {
        val chat = chat.getValue() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
        showUnreadHeader.value = false
    }

    override fun typing() {
        val chat = chat.getValue() ?: return
        chat.participants.value.me.typing()
    }

    override fun fetchMessages() {
        viewModelScope.launch {
            isFetching.emit(true)
            chat.first().fetch(FETCH_COUNT) {
                isFetching.tryEmit(false)
            }
        }
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        val messages = messages.getValue()?.other ?: return
        messages.firstOrNull { it.id == messageItem.id }?.markAsRead()
    }

    override fun onAllMessagesScrolled() {
        val messages = messages.getValue()?.other ?: return
        messages.first().markAsRead()
    }

    override fun showCall() {
        val phoneBox = phoneBox.getValue() ?: return
        phoneBox.showCall()
    }

    private fun call(preferredType: Call.PreferredType) {
        val phoneBox = phoneBox.getValue() ?: return
        val chat = chat.getValue() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User {
            override val userId = userId
        })) {
            this.preferredType = preferredType
        }
    }

    companion object {
        private const val FETCH_COUNT = 50

        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.NewInstanceFactory() {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PhoneChatViewModel(configure) as T
            }
        }
    }
}