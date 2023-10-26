@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ChatViewModel

    private val conference = mockk<ConferenceUI>()

    private val conversation = mockk<ConversationUI>()

    private val call = mockk<CallUI>()

    private val chat = mockk<ChatUI>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = ChatViewModel { CollaborationViewModel.Configuration.Success(conference, conversation, mockk()) }
        every { conversation.create(any()) } returns Result.success(chat)
        every { conversation.create(any(), any()) } returns Result.success(chat)
        every { conference.call } returns MutableStateFlow(call)
    }

    @Test
    fun getCall_getCallInstance() = runTest {
        assertEquals(viewModel.call.first(), call)
    }

    @Test
    fun setChatUser_getChatInstance() = runTest {
        advanceUntilIdle()
        assertEquals(viewModel.setChat("user"), viewModel.chat.first())
    }

    @Test
    fun setGroupChatUser_getChatInstance() = runTest {
        advanceUntilIdle()
        assertEquals(viewModel.setChat(listOf("user1", "user2"), "chatId"), viewModel.chat.first())
    }

    @Test
    fun getMessages_getMessagesInstance() = runTest {
        advanceUntilIdle()
        val messages = mockk<MessagesUI>()
        every { chat.messages } returns MutableStateFlow(messages)
        viewModel.setChat("")
        assertEquals(viewModel.messages.first(), messages)
    }

    @Test
    fun getActions_getActionsInstance() = runTest {
        advanceUntilIdle()
        val actions = setOf(ChatUI.Action.ShowParticipants)
        every { chat.actions } returns MutableStateFlow(actions)
        viewModel.setChat("")
        assertEquals(viewModel.actions.first(), actions)
    }

    @Test
    fun getParticipants_getParticipantsInstance() = runTest {
        advanceUntilIdle()
        val participants = mockk<ChatParticipants>()
        every { chat.participants } returns MutableStateFlow(participants)
        viewModel.setChat("")
        assertEquals(viewModel.participants.first(), participants)
    }

}