package com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.mapToChatActions
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.mapToConversationItems
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class PhoneChatViewModel : ChatViewModel(), ChatUiViewModel {

    private val _uiState = MutableStateFlow(ChatUiState())

    override val uiState: StateFlow<ChatUiState>
        get() = _uiState

    private val showUnreadHeader = MutableStateFlow(true)

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
    }

    override fun sendMessage(text: String) {
        val chat = chat.replayCache.firstOrNull() ?: return
        val message = chat.create(Message.Content.Text(text))
        chat.add(message)
        showUnreadHeader.value = false
    }

    override fun typing() {
        val chat = chat.replayCache.firstOrNull() ?: return
        chat.participants.value.me.typing()
    }

    override fun fetchMessages() {
        viewModelScope.launch {
            chat.first().fetch(FETCH_COUNT) { result ->
                _uiState.update {
                    val areAllMessageFetched = result.getOrNull()?.list?.isEmpty()
                    val conversationState = it.conversationState.copy(areAllMessagesFetched = areAllMessageFetched ?: it.conversationState.areAllMessagesFetched)
                    it.copy(conversationState = conversationState)
                }
            }
        }
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        val messages = messages.replayCache.firstOrNull()?.other ?: return
        messages.firstOrNull { it.id == messageItem.id }?.markAsRead()
    }

    override fun onAllMessagesScrolled() {
        val messages = messages.replayCache.firstOrNull()?.other ?: return
        messages.first().markAsRead()
    }

    override fun showCall() = CollaborationUI.phoneBox.showCall()

    private fun call(preferredType: Call.PreferredType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User {
            override val userId = userId
        })) {
            this.preferredType = preferredType
        }
    }

    companion object {
        private const val FETCH_COUNT = 50
    }
}