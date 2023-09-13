package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatParticipantsMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.messagesUIMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherReadMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock1
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conferenceMock
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationElement
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.viewmodel.PhoneChatViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PhoneChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PhoneChatViewModel

    @Before
    fun setUp() {
        mockkObject(CallUserMessagesProvider)
        viewModel = spyk(PhoneChatViewModel { Configuration.Success(conferenceMock, conversationMock,  mockk(relaxed = true)) })
        every { viewModel.chat } returns MutableStateFlow(chatMock)
        every { viewModel.messages } returns MutableStateFlow(messagesUIMock)
        every { messagesUIMock.other } returns listOf(otherUnreadMessageMock2, otherUnreadMessageMock1, otherReadMessageMock)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }

    @Test
    fun testSendMessage() {
        val text = "text"
        viewModel.sendMessage(text)
        verify { chatMock.add(match { it is Message.Content.Text && it.message == text }) }
    }

    @Test
    fun testTyping() = runTest {
        every { chatMock.participants } returns MutableStateFlow(chatParticipantsMock)
        viewModel.typing()
        verify { chatParticipantsMock.me.typing() }
    }

    @Test
    fun testFetchMessages() = runTest {
        viewModel.fetchMessages()
        advanceUntilIdle()
        coVerify { chatMock.fetch(any()) }
    }

    @Test
    fun testOnMessageScrolled() {
        val message = mockk<com.kaleyra.collaboration_suite_phone_ui.chat.model.Message.OtherMessage>()
        every { message.id } returns otherUnreadMessageMock1.id
        viewModel.onMessageScrolled(ConversationElement.Message(message))
        verify { otherUnreadMessageMock1.markAsRead() }
    }

    @Test
    fun testOnAllMessagesScrolled() = runTest {
        viewModel.onAllMessagesScrolled()
        verify { otherUnreadMessageMock2.markAsRead() }
    }

    @Test
    fun testShowCall() = runTest {
        advanceUntilIdle()
        viewModel.showCall()
        verify { conferenceMock.showCall() }
    }

}
