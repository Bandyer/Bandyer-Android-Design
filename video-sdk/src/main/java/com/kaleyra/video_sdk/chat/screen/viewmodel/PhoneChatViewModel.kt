package com.kaleyra.video_sdk.chat.screen.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conversation.Message
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.ChatViewModel
import com.kaleyra.video_common_ui.CompanyUI
import com.kaleyra.video_common_ui.theme.CompanyThemeManager.combinedTheme
import com.kaleyra.video_sdk.chat.appbar.model.ChatAction
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.video_sdk.chat.appbar.model.ChatParticipantsState
import com.kaleyra.video_sdk.chat.appbar.model.ConnectionState
import com.kaleyra.video_sdk.chat.conversation.model.ConversationItem
import com.kaleyra.video_sdk.chat.conversation.model.ConversationState
import com.kaleyra.video_sdk.chat.mapper.CallStateMapper.hasActiveCall
import com.kaleyra.video_sdk.chat.mapper.ChatActionsMapper.mapToChatActions
import com.kaleyra.video_sdk.chat.mapper.ConversationStateMapper.toConnectionState
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.video_sdk.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.video_sdk.chat.mapper.ParticipantsMapper.isGroupChat
import com.kaleyra.video_sdk.chat.mapper.ParticipantsMapper.toOtherParticipantsState
import com.kaleyra.video_sdk.chat.mapper.ParticipantsMapper.toParticipantsDetails
import com.kaleyra.video_sdk.chat.screen.model.ChatUiState
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableList
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableMap
import com.kaleyra.video_sdk.common.immutablecollections.ImmutableSet
import com.kaleyra.video_sdk.common.usermessages.model.UserMessage
import com.kaleyra.video_sdk.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.video_sdk.common.viewmodel.UserMessageViewModel
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
    val isInCall: Boolean = false,
    val isUserConnected: Boolean = true
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
                isInCall = isInCall,
                isUserConnected = isUserConnected
            )
        } else {
            ChatUiState.OneToOne(
                recipientDetails = recipientDetails,
                actions = actions,
                connectionState = connectionState,
                conversationState = conversationState,
                isInCall = isInCall,
                isUserConnected = isUserConnected
            )
        }
    }
}

internal class PhoneChatViewModel(configure: suspend () -> Configuration) : ChatViewModel(configure), UserMessageViewModel {

    private val firstUnreadMessageId = MutableStateFlow<String?>(null)

    private val viewModelState = MutableStateFlow(PhoneChatViewModelState())

    val uiState = viewModelState
        .map(PhoneChatViewModelState::toUiState)
        .stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())

    val theme = company
        .flatMapLatest { it.combinedTheme }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CompanyUI.Theme())

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
            .map { it.mapToChatActions(call = { pt, a -> call(pt, a) }) }
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

        connectedUser
            .onEach { user -> viewModelState.update { it.copy(isUserConnected = user != null) } }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            val chat = chat.first()
            findFirstUnreadMessageId(chat.messages.first(), chat::fetch).also {
                firstUnreadMessageId.value = it
            }

            messages
                .onEach { messagesUI ->
                    val newItems = messagesUI.list.mapToConversationItems(firstUnreadMessageId = firstUnreadMessageId.value)
                    updateConversationItems(newItems)
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
        chat.participants.value.me?.typing()
    }

    fun fetchMessages() {
        viewModelScope.launch {
            updateFetchingState(isFetching = true)
            chat.first().fetch(FETCH_COUNT)
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
        val call = call.getValue() ?: return
        call.show()
    }

    private fun updateUnreadMessagesCount(count: Int) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(unreadMessagesCount = count)
            it.copy(conversationState = conversationState)
        }
    }

    private fun updateConversationItems(newItems: List<ConversationItem>) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(conversationItems = ImmutableList(newItems))
            it.copy(conversationState = conversationState)
        }
    }

    private fun updateFetchingState(isFetching: Boolean) {
        viewModelState.update {
            val conversationState = it.conversationState.copy(isFetching = isFetching)
            it.copy(conversationState = conversationState)
        }
    }

    private fun call(preferredType: Call.PreferredType, callActions: Set<CallUI.Action>) {
        val conference = conference.getValue() ?: return
        val chat = chat.getValue() ?: return
        val userId = chat.participants.value.others.first().userId
        val call = conference.call(listOf(userId)) {
            this.preferredType = preferredType
        }.getOrNull()
        call?.actions?.value = callActions
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