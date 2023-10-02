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
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.CallStateMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ChatActionsMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ConversationStateMapper.toConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.isLastChainMessage
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.isGroupChat
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toOtherParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toParticipantsDetails
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
    val recipientDetails: ChatParticipantDetails = ChatParticipantDetails(),
    val chatName: String = "",
    val chatImage: ImmutableUri = ImmutableUri(),
    val actions: ImmutableSet<ChatAction> = ImmutableSet(),
    val connectionState: ConnectionState = ConnectionState.Unknown,
    val participantsDetails: ImmutableMap<String, ChatParticipantDetails> = ImmutableMap(),
    val participantsState: ChatParticipantsState = ChatParticipantsState(),
    val conversationState: ConversationState = ConversationState(),
    val isInCall: Boolean = false
) {

    fun toUiState(): ChatUiState {
        return if (isGroupChat) {
            ChatUiState.Group(
                name = chatName,
                image = chatImage,
                participantsDetails = participantsDetails,
                participantsState = participantsState,
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

    val uiState = viewModelState
        .map(PhoneChatViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())

    val theme: Flow<CompanyUI.Theme>
        get() = company.flatMapLatest { it.combinedTheme }

    override val userMessage: Flow<UserMessage>
        get() = CallUserMessagesProvider.userMessage

    init {
        viewModelScope.launch {
            val isGroupChat = participants.first().isGroupChat()
            viewModelState.update { it.copy(isGroupChat = isGroupChat) }

            if (isGroupChat) {
                // TODO bind the chat name and chat image when the mtm chats will be available
                participants.first()
                    .toOtherParticipantsState()
                    .onEach { participantsState -> viewModelState.update { it.copy(participantsState = participantsState) } }
                    .launchIn(this)

                participants
                    .map { it.toParticipantsDetails() }
                    .onEach { participantsDetails ->
                        viewModelState.update {
                            it.copy(
                                participantsDetails = participantsDetails
                            )
                        }
                    }
                    .launchIn(this)
            } else {
                participants
                    .map { it.toParticipantsDetails() }
                    .onEach { participantsDetails ->
                        val others = participants.getValue()?.others
                        val recipientUserId = others?.firstOrNull()?.userId
                        val recipientDetails = recipientUserId?.let { participantsDetails[it] }
                        if (recipientDetails != null) viewModelState.update {
                            it.copy(
                                recipientDetails = recipientDetails
                            )
                        }
                    }.launchIn(this)
            }
        }

        actions
            .map { it.mapToChatActions(call = { pt -> call(pt) }) }
            .onEach { actions -> viewModelState.update { it.copy(actions = ImmutableSet(actions)) } }
            .launchIn(viewModelScope)

        call
            .hasActiveCall()
            .onEach { hasActiveCall -> viewModelState.update { it.copy(isInCall = hasActiveCall) } }
            .launchIn(viewModelScope)

        conversation
            .toConnectionState()
            .onEach { connectionState -> viewModelState.update { it.copy(connectionState = connectionState) } }
            .launchIn(viewModelScope)

        chat
            .flatMapLatest { it.unreadMessagesCount }
            .onEach { count -> updateUnreadMessagesCount(count) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
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

                    latestMessage?.let { updateLatestMessageItem(newMessages, it) }
                    val newItems = newMessages.mapToConversationItems(firstUnreadMessageId = firstUnreadMessageId.value, lastMappedMessage = latestMessage)
                    updateConversationItems(newItems, prepend = true, removeUnreadMessage = firstUnreadMessageId.value == null)

                    latestMessage = messages.firstOrNull()
                }
                .launchIn(this)
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
            val fetchedItems = messages?.list?.mapToConversationItems() ?: emptyList()
            updateConversationItems(fetchedItems)
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

    // Update last message item's isLastChainMessage flag
    private fun updateLatestMessageItem(
        newMessages: List<Message>,
        latestMessage: Message
    ) {
        val firstNewMessage = newMessages.firstOrNull()
        val conversationState = viewModelState.value.conversationState
        val currentItems = conversationState.conversationItems?.value?.toMutableList()
        val lastConversationItem = currentItems?.firstOrNull()
        if (firstNewMessage == null || currentItems.isNullOrEmpty() || lastConversationItem !is ConversationItem.Message) return

        val updatedMessageItem = lastConversationItem.copy(
            isLastChainMessage = latestMessage.isLastChainMessage(firstNewMessage)
        )
        currentItems[0] = updatedMessageItem
        viewModelState.update {
            val updatedConversationState = it.conversationState.copy(conversationItems = ImmutableList(currentItems))
            it.copy(conversationState = updatedConversationState)
        }
    }

    private fun updateConversationItems(
        newItems: List<ConversationItem>,
        prepend: Boolean = false,
        removeUnreadMessage: Boolean = false
    ) {
        viewModelState.update {
            val conversationState = it.conversationState
            val currentItems =
                conversationState.conversationItems?.value?.toMutableList() ?: mutableListOf()
            if (removeUnreadMessage) {
                val unreadItemIndex =
                    currentItems.indexOfFirst { item -> item is ConversationItem.UnreadMessages }
                if (unreadItemIndex != -1) currentItems.removeAt(unreadItemIndex)
            }
            val updatedItems = if (prepend) newItems + currentItems else currentItems + newItems
            val updatedConversationState =
                it.conversationState.copy(conversationItems = ImmutableList(updatedItems))
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