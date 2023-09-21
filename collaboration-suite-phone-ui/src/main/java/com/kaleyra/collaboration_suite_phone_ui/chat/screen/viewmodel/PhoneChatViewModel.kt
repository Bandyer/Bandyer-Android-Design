package com.kaleyra.collaboration_suite_phone_ui.chat.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CompanyUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.CallStateMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ChatActionsMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ConversationStateMapper.toChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure),
    UserMessageViewModel {

    private var latestMessage: Message? = null

    private val firstUnreadMessageId = MutableStateFlow<String?>(null)

    private val isFetching = MutableSharedFlow<Boolean>(replay = 1, extraBufferCapacity = 1)

    private val _uiState = MutableStateFlow(ChatUiState())

    val uiState = _uiState.asStateFlow()

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    val theme: Flow<CompanyUI.Theme>
        get() = company.flatMapLatest { it.combinedTheme }

    init {
        conversation
            .toChatState(participants)
            .onEach { state -> _uiState.update { it.copy(state = state) } }
            .launchIn(viewModelScope)

        participants
            .toChatInfo()
            .onEach { info -> _uiState.update { it.copy(info = info) } }
            .launchIn(viewModelScope)

        participants
            .toChatParticipantDetails()
            .onEach { participantsDetails ->
                _uiState.update { it.copy(conversationState = it.conversationState.copy(participantsDetails = participantsDetails)) }
            }
            .launchIn(viewModelScope)

        actions
            .map { it.mapToChatActions(call = { pt -> call(pt) }) }
            .onEach { actions -> _uiState.update { it.copy(actions = ImmutableSet(actions)) } }
            .launchIn(viewModelScope)

        call
            .hasActiveCall()
            .onEach { hasActiveCall -> _uiState.update { it.copy(isInCall = hasActiveCall) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val chat = chat.first()
            val unreadMessageId = findFirstUnreadMessageId(chat.messages.first(), chat::fetch).also {
                firstUnreadMessageId.value = it
            }

            this@PhoneChatViewModel.messages
                .onEach { messagesUI ->
                    val messages = messagesUI.list
                    val newMessages = latestMessage?.let { message -> messages.takeWhile { it.id != message.id } } ?: messages
                    val newItems = newMessages.mapToConversationItems(unreadMessageId, latestMessage)
                    latestMessage = messages.firstOrNull()
                    _uiState.update {
                        val conversationState = it.conversationState
                        val currentItems = conversationState.conversationElements?.value ?: emptyList()
                        val newConversationState = conversationState.copy(conversationElements = ImmutableList(newItems + currentItems))
                        it.copy(conversationState = newConversationState)
                    }
                }
                .launchIn(this)
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
        firstUnreadMessageId.value = null
    }

    fun typing() {
        val chat = chat.getValue() ?: return
        chat.participants.value.me.typing()
    }

    fun fetchMessages() {
        viewModelScope.launch {
            isFetching.emit(true)
            val messages = chat.first().fetch(FETCH_COUNT).getOrNull()
            val fetchedItems = messages?.list?.mapToConversationItems(firstUnreadMessageId.value) ?: emptyList()

            _uiState.update {
                val currentItems = it.conversationState.conversationElements?.value ?: emptyList()
                val newItems = currentItems + fetchedItems
                val conversationState = it.conversationState.copy(conversationElements = ImmutableList(newItems))
                it.copy(conversationState = conversationState)
            }
            isFetching.emit(false)
        }
    }

    fun onMessageScrolled(message: ConversationElement.Message) {
        val messages = messages.getValue()?.other ?: return
        messages.firstOrNull { it.id == message.id }?.markAsRead()
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

        fun provideFactory(configure: suspend () -> Configuration) =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return PhoneChatViewModel(configure) as T
                }
            }
    }
}