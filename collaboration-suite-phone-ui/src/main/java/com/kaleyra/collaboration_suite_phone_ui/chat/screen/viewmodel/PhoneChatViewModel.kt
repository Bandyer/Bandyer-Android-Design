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
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.CallStateMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ChatActionsMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ConversationStateMapper.toConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.isGroupChat
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatParticipantsDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toRecipientDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.UserMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.common.viewmodel.UserMessageViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private data class PhoneChatViewModelState(
    val isGroupChat: Boolean = false,
    val recipientDetails: ParticipantDetails = ParticipantDetails("", ImmutableUri()),
    val chatName: String = "",
    val chatImage: ImmutableUri = ImmutableUri(),
    val participantsDetails: ImmutableMap<String, ParticipantDetails> = ImmutableMap(),
    val actions: ImmutableSet<ChatAction> = ImmutableSet(),
    val connectionState: ConnectionState = ConnectionState.Undefined,
    val participantsState: ImmutableMap<String, ParticipantState> = ImmutableList(),
    val conversationState: ConversationState = ConversationState(),
    val isInCall: Boolean = false
) {

    fun toUiState(): ChatUiState {
        return if (isGroupChat) {
            ChatUiState.Group(
                name = chatName,
                image = chatImage,
                participantsDetails = participantsDetails,
                actions = actions,
                connectionState = connectionState,
                conversationState = conversationState,
                isInCall = isInCall
            )
        } else {
            ChatUiState.OneToOne(
                recipientDetails = recipientDetails,
                actions = actions,
                connectionState = connectionState,
                conversationState = conversationState,
                isInCall = isInCall
            )
        }
    }
}

class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure), UserMessageViewModel {

    private val firstUnreadMessageId = MutableStateFlow<String?>(null)

    private val viewModelState = MutableStateFlow(PhoneChatViewModelState())

    val theme: Flow<CompanyUI.Theme>
        get() = company.flatMapLatest { it.combinedTheme }

    val uiState = viewModelState
        .map(PhoneChatViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        viewModelScope.launch {
            val isGroupChat = participants.isGroupChat().first()
            viewModelState.update { it.copy(isGroupChat = isGroupChat) }

            actions
                .map { it.mapToChatActions(call = { pt -> call(pt) }) }
                .onEach { actions -> viewModelState.update { it.copy(actions = ImmutableSet(actions)) } }
                .launchIn(this)

            call
                .hasActiveCall()
                .onEach { hasActiveCall -> viewModelState.update { it.copy(isInCall = hasActiveCall) } }
                .launchIn(this)

            launch {
                val chat = chat.first()
                findFirstUnreadMessageId(chat.messages.first(), chat::fetch).also {
                    firstUnreadMessageId.value = it
                }

                var latestMessage: Message? = null
                messages
                    .onEach { messagesUI ->
                        val messages = messagesUI.list
                        // Take only new messages after the latest one to avoid mapping all the previous messages again
                        val newMessages = latestMessage?.let { message -> messages.takeWhile { it.id != message.id } } ?: messages
                        val newItems = newMessages.mapToConversationItems(firstUnreadMessageId.value, latestMessage)
                        updateConversationItems(newItems, prepend = true)
                        latestMessage = messages.firstOrNull()
                    }
                    .launchIn(this)
            }

            conversation
                .toConnectionState(participants)
                .onEach { connectionState -> viewModelState.update { it.copy(connectionState = connectionState) } }
                .launchIn(this)

            chat
                .flatMapLatest { it.unreadMessagesCount }
                .onEach { count -> updateUnreadMessagesCount(count) }
                .launchIn(this)

            if (isGroupChat) {
                // TODO Bind the chat name and chat image when it will be developed for mtm chats

                participants
                    .toChatParticipantsDetails()
                    .onEach { participantsDetails -> viewModelState.update { it.copy(participantsDetails = participantsDetails) } }
                    .launchIn(this)
            } else {
                participants
                    .toRecipientDetails()
                    .onEach { recipientDetails -> viewModelState.update { it.copy(recipientDetails = recipientDetails) } }
                    .launchIn(this)
            }
        }
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
            updateFetchingState(isFetching = true)
            val messages = chat.first().fetch(FETCH_COUNT).getOrNull()
            val fetchedItems = messages?.list?.mapToConversationItems(firstUnreadMessageId.value) ?: emptyList()
            updateConversationItems(fetchedItems, prepend = true)
            updateFetchingState(isFetching = false)
        }
    }

    fun onMessageScrolled(message: ConversationItem.Message) {
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

    private fun updateUnreadMessagesCount(count: Int) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(unreadMessagesCount = count)
            it.copy(conversationState = conversationState)
        }
    }

    private fun updateConversationItems(newItems: List<ConversationItem>, prepend: Boolean = false) {
        viewModelState.update {
            val conversationState = it.conversationState
            val currentItems = conversationState.conversationItems?.value ?: emptyList()
            val updatedItems = if (prepend) newItems + currentItems  else currentItems + newItems
            val updatedConversationState = it.conversationState.copy(conversationItems = ImmutableList(updatedItems))
            it.copy(conversationState = updatedConversationState)
        }
    }

    private fun updateFetchingState(isFetching: Boolean) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(isFetching = isFetching)
            it.copy(conversationState = conversationState)
        }
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