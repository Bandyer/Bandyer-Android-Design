@file:OptIn(ExperimentalCoroutinesApi::class)

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.chatbox.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    lateinit var viewModel: ChatViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>(relaxed = true)
    private val callMock = mockk<CallUI>()

    private val chatBoxMock = mockk<ChatBoxUI>(relaxed = true)
    private val chatBoxStateMock = mockk<ChatBox.State>()

    private val chatMock = mockk<ChatUI>()

    private val userDescriptionMock = mockk<UsersDescription>()

    @Before
    fun setUp() {
        mockkObject(CollaborationService)
        mockkObject(CollaborationUI)
        coEvery { CollaborationService.get() } returns null
        coEvery { CollaborationUI.isConfigured } returns true
        coEvery { CollaborationUI.phoneBox } returns phoneBoxMock
        coEvery { CollaborationUI.chatBox } returns chatBoxMock
        coEvery { CollaborationUI.usersDescription } returns userDescriptionMock
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { chatBoxMock.state } returns MutableStateFlow(chatBoxStateMock)
        every { chatBoxMock.create(any()) } returns chatMock
        viewModel = ChatViewModel()
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun getUsersDescription() = runTest {
        assert(viewModel.usersDescription.first() == userDescriptionMock)
    }

    @Test
    fun getPhoneBox() = runTest {
        assert(viewModel.phoneBox.first() == phoneBoxMock)
    }

    @Test
    fun getCall() = runTest {
        assert(viewModel.call.first() == callMock)
    }

    @Test
    fun getChatBox() = runTest {
        assert(viewModel.chatBox.first() == chatBoxMock)
    }

    @Test
    fun getChatBoxState() = runTest {
        assert(viewModel.chatBoxState.first() == chatBoxStateMock)
    }

    @Test
    fun setChat() = runTest {
        val chat = viewModel.setChat(userId = "")
        assert(viewModel.chat.first() == chat)
    }

    @Test
    fun getMessages() = runTest {
        val messagesMock = mockk<MessagesUI>()
        every { chatMock.messages } returns MutableStateFlow(messagesMock)
        viewModel.setChat(userId = "")
        assert(viewModel.messages.first() == messagesMock)
    }

    @Test
    fun getActions() = runTest {
        val actions = setOf(ChatUI.Action.ShowParticipants)
        every { chatMock.actions } returns MutableStateFlow(actions)
        viewModel.setChat(userId = "")
        assert(viewModel.actions.first { it.isNotEmpty() } == actions)
    }

    @Test
    fun getParticipants() = runTest {
        val participantsMock = mockk<ChatParticipants>()
        every { chatMock.participants } returns MutableStateFlow(participantsMock)
        viewModel.setChat(userId = "")
        assert(viewModel.participants.first() == participantsMock)
    }

}