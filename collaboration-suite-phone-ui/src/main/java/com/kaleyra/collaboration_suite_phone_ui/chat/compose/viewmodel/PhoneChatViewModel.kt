package com.kaleyra.collaboration_suite_phone_ui.chat.compose.viewmodel

import androidx.lifecycle.viewModelScope
import com.kaleyra.collaboration_suite.User
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.ChatViewModel
import com.kaleyra.collaboration_suite_core_ui.CollaborationUI
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.CallType
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.getChatState
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.hasActiveCall
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.mapToUiActions
import com.kaleyra.collaboration_suite_phone_ui.chat.compose.utility.UiModelMapper.unreadMessagesIds
import kotlinx.coroutines.flow.*

internal class PhoneChatViewModel : ChatViewModel(), ChatUiViewModel {

    private val _uiState = MutableStateFlow(ChatUiState())

    override val uiState: StateFlow<ChatUiState>
        get() = _uiState

    private val showUnreadHeader = MutableStateFlow(true)

    private val unseenMessagesIds = MutableStateFlow<Set<String>>(setOf()).also { flow ->
        chat.unreadMessagesIds()
            .onEach { messages -> flow.value += messages.toSet() }
            .launchIn(viewModelScope)
    }

    init {
        getChatState(participants, chatBox.replayCache.first()).onEach { state ->
            _uiState.update { it.copy(state = state) }
        }.launchIn(viewModelScope)

        getChatInfo(participants, usersDescription).onEach { info ->
            _uiState.update { it.copy(info = info) }
        }.launchIn(viewModelScope)

        actions.map { it.mapToUiActions() }.onEach { actions ->
            _uiState.update { it.copy(actions = actions) }
        }.launchIn(viewModelScope)

        phoneBox.hasActiveCall().onEach { hasActiveCall ->
            _uiState.update { it.copy(isInCall = hasActiveCall) }
        }.launchIn(viewModelScope)

        messages.mapToConversationItems(viewModelScope, showUnreadHeader).onEach { items ->
            _uiState.update {
                val conversationState = it.conversationState.copy(conversationItems = items)
                it.copy(conversationState = conversationState)
            }
        }.launchIn(viewModelScope)

        messages.take(1).map { true }.onEach { areMessagesInitialized ->
            _uiState.update {
                val conversationState = it.conversationState.copy(areMessagesInitialized = areMessagesInitialized)
                it.copy(conversationState = conversationState) }
        }.launchIn(viewModelScope)

        unseenMessagesIds.onEach { messages ->
            _uiState.update {
                val conversationState = it.conversationState.copy(unseenMessagesCount = messages.count())
                it.copy(conversationState = conversationState)
            }
        }.launchIn(viewModelScope)
    }

    override fun readAllMessages() {
        val messages = messages.replayCache.firstOrNull() ?: return
        messages.other.forEach { it.markAsRead() }
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
        chat.replayCache.firstOrNull()?.fetch(FETCH_COUNT) { result ->
            _uiState.update {
                val areAllMessageFetched = result.getOrNull()?.list?.isEmpty()
                val conversationState = it.conversationState.copy(areAllMessagesFetched = areAllMessageFetched ?: it.conversationState.areAllMessagesFetched)
                it.copy(conversationState = conversationState)
            }
        }
    }

    override fun onMessageScrolled(messageItem: ConversationItem.MessageItem) {
        unseenMessagesIds.value = unseenMessagesIds.value - messageItem.id
    }

    override fun onAllMessagesScrolled() {
        unseenMessagesIds.value = setOf()
    }

    override fun call(callType: CallType) {
        val phoneBox = phoneBox.replayCache.firstOrNull() ?: return
        val chat = chat.replayCache.firstOrNull() ?: return
        val userId = chat.participants.value.others.first().userId
        phoneBox.call(listOf(object : User {
            override val userId = userId
        })) {
            preferredType = callType.preferredType
        }
    }

    override fun showCall() = CollaborationUI.phoneBox.showCall()

    companion object {
        private const val FETCH_COUNT = 50
    }
}






