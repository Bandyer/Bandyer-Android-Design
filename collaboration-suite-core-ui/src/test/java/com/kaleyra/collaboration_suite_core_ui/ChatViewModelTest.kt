@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ChatViewModel

    private val phoneBox = mockk<PhoneBoxUI>()

    private val chatBox = mockk<ChatBoxUI>()

    private val call = mockk<CallUI>()

    private val chat = mockk<ChatUI>()

    @Before
    fun setUp() {
        viewModel = spyk(ChatViewModel { Configuration.Success(phoneBox, chatBox, mockk()) })
        every { viewModel.chatBox } returns MutableStateFlow(chatBox)
        every { chatBox.create(any()) } returns chat
        every { phoneBox.call } returns MutableStateFlow(call)
    }

    @Test
    fun getCall_getCallInstance() = runTest {
        assert(viewModel.call.first() == call)
    }

    @Test
    fun setChatUser_getChatInstance() = runTest {
        assert(viewModel.setChat("") == viewModel.chat.first())
    }

    @Test
    fun getMessages_getMessagesInstance() = runTest {
        val messages = mockk<MessagesUI>()
        every { chat.messages } returns MutableStateFlow(messages)
        viewModel.setChat("")
        assert(viewModel.messages.first() == messages)
    }

    @Test
    fun getActions_getActionsInstance() = runTest {
        val actions = setOf(ChatUI.Action.ShowParticipants)
        every { chat.actions } returns MutableStateFlow(actions)
        viewModel.setChat("")
        assert(viewModel.actions.first() == actions)
    }

    @Test
    fun getParticipants_getParticipantsInstance() = runTest {
        val participants = mockk<ChatParticipants>()
        every { chat.participants } returns MutableStateFlow(participants)
        viewModel.setChat("")
        assert(viewModel.participants.first() == participants)
    }

}