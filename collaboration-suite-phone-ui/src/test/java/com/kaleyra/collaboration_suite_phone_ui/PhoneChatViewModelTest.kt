package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatBoxMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatParticipantsMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.messagesUIMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherReadMessageMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock1
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherUnreadMessageMock2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.usersDescriptionMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.phoneBoxMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.RecordingMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.PhoneChatViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
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
        viewModel = spyk(PhoneChatViewModel { Configuration.Success(phoneBoxMock, chatBoxMock,  mockk(relaxed = true), mockk(relaxed = true), usersDescriptionMock) })
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
        viewModel.onMessageScrolled(ConversationItem.MessageItem(message))
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
        verify { phoneBoxMock.showCall() }
    }

}
