package com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
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

class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure) {

    private val showUnreadHeader = MutableStateFlow(true)

    private val isFetching = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    private val _uiState = MutableStateFlow(ChatUiState())

    val uiState = _uiState.asStateFlow()

    val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    val theme: Flow<CompanyUI.Theme>
        get() = company.flatMapLatest { it.combinedTheme }

    init {
        getChatState(participants, conversation).onEach { state ->
            _uiState.update { it.copy(state = state) }
        }.launchIn(viewModelScope)

        getChatInfo(participants).onEach { info ->
            _uiState.update { it.copy(info = info) }
        }.launchIn(viewModelScope)

        actions.map { it.mapToChatActions(call = { pt -> call(pt) }) }.onEach { actions ->
            _uiState.update { it.copy(actions = ImmutableSet(actions)) }
        }.launchIn(viewModelScope)

        conference.hasActiveCall().onEach { hasActiveCall ->
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

    fun sendMessage(text: String) {
        val chat = chat.getValue() ?: return
        chat.add(Message.Content.Text(text))
        showUnreadHeader.value = false
    }

    fun typing() {
        val chat = chat.getValue() ?: return
        chat.participants.value.me?.typing()
    }

    fun fetchMessages() {
        viewModelScope.launch {
            isFetching.emit(true)

            chat.first().fetch(FETCH_COUNT)
            isFetching.emit(false)
        }
    }

    fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        val messages = messages.getValue()?.other ?: return
        messages.firstOrNull { it.id == messageItem.id }?.markAsRead()
    }

    fun onAllMessagesScrolled() {
        val messages = messages.getValue()?.other ?: return
        messages.first().markAsRead()
    }

    fun showCall() {
        val conference = conference.getValue() ?: return
        conference.showCall()
    }

    private fun call(preferredType: Call.PreferredType) {
        val conference = conference.getValue() ?: return
        val chat = chat.getValue() ?: return
        val userId = chat.participants.value.others.first().userId
        conference.call(listOf(userId)) {
            this.preferredType = preferredType
        }
    }

    companion object {
        private const val FETCH_COUNT = 50

        fun provideFactory(configure: suspend () -> Configuration) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PhoneChatViewModel(configure) as T
            }
        }
    }
}