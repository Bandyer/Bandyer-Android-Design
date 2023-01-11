package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.chat.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.viewmodel.PhoneChatViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
        viewModel = spyk(PhoneChatViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, usersDescriptionMock) })
        every { viewModel.chat } returns MutableStateFlow(chatMock)
        every { viewModel.messages } returns MutableStateFlow(messagesUIMock)
        every { messagesUIMock.other } returns listOf(otherUnreadMessageMock2, otherUnreadMessageMock1, otherReadMessageMock)
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
