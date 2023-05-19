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
        mockkConstructor(CallUserMessagesProvider::class)
        every { anyConstructed<CallUserMessagesProvider>().recordingUserMessage() } returns MutableStateFlow(RecordingMessage.Started())
        every { anyConstructed<CallUserMessagesProvider>().mutedUserMessage() } returns MutableStateFlow(MutedMessage(null))
        viewModel = spyk(PhoneChatViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, mockk(), usersDescriptionMock) })
        every { viewModel.chat } returns MutableStateFlow(chatMock)
        every { viewModel.messages } returns MutableStateFlow(messagesUIMock)
        every { messagesUIMock.other } returns listOf(otherUnreadMessageMock2, otherUnreadMessageMock1, otherReadMessageMock)
    }

    @Test
    fun testRecordingUserMessageReceived_userMessagesUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().userMessages.recordingMessage
        assert(actual is RecordingMessage.Started)
    }

    @Test
    fun testMutedUserMessageReceived_userMessagesUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().userMessages.mutedMessage
        Assert.assertNotEquals(null, actual)
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
