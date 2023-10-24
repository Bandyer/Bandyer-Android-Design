package com.kaleyra.collaboration_suite_phone_ui.viewmodel.chat

import android.net.Uri
import com.kaleyra.collaboration_suite.State
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.Message
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel.Configuration
import com.kaleyra.collaboration_suite_core_ui.MessagesUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.chatMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conferenceMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.conversationMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.groupChatParticipantsMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.messagesUIMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.myParticipantMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.oneToOneChatParticipantsFlow
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherParticipantMock2
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayReadMessage
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayUnreadMessage
import com.kaleyra.collaboration_suite_phone_ui.Mocks.otherTodayUnreadMessage2
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatAction
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ConnectionState
import com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.ConversationItem
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.findFirstUnreadMessageId
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.MessagesMapper.mapToConversationItems
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.model.ChatUiState
import com.kaleyra.collaboration_suite_phone_ui.chat.screen.viewmodel.PhoneChatViewModel
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableSet
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import io.mockk.*
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class PhoneChatViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: PhoneChatViewModel

    private val myUri = mockk<Uri>()

    private val otherUri = mockk<Uri>()

    private val otherUri2 = mockk<Uri>()

    private val chatParticipantsFlow = MutableStateFlow(oneToOneChatParticipantsFlow)

    private val messagesFlow = MutableStateFlow(messagesUIMock)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        mockkObject(CallUserMessagesProvider)
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        with(conversationMock) {
            every { chats } returns MutableStateFlow(listOf(chatMock))
            every { create(any()) } returns Result.success(chatMock)
        }
        with(messagesUIMock) {
            every { list } returns listOf(otherTodayUnreadMessage, otherTodayReadMessage)
            every { other } returns listOf(otherTodayUnreadMessage, otherTodayReadMessage)
        }
        with(myParticipantMock) {
            every { userId } returns "myUserId"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("myUsername")
            every { combinedDisplayImage } returns flowOf(myUri)
        }
        with(otherParticipantMock) {
            every { userId } returns "otherUserId"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("otherDisplayName")
            every { combinedDisplayImage } returns flowOf(otherUri)
        }
        with(otherParticipantMock2) {
            every { userId } returns "otherUserId2"
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { combinedDisplayName } returns flowOf("otherDisplayName2")
            every { combinedDisplayImage } returns flowOf(otherUri2)
        }
        with(chatMock) {
            every { messages } returns messagesFlow
            every { unreadMessagesCount } returns MutableStateFlow(5)
            every { participants } returns chatParticipantsFlow
            every { actions } returns MutableStateFlow(
                setOf(
                    ChatUI.Action.CreateCall(preferredType = Call.PreferredType.audioUpgradable()),
                    ChatUI.Action.CreateCall(preferredType = Call.PreferredType.audioVideo())
                )
            )
        }
        chatParticipantsFlow.value = oneToOneChatParticipantsFlow
        viewModel = spyk(PhoneChatViewModel {
            Configuration.Success(
                conferenceMock,
                conversationMock,
                mockk(relaxed = true)
            )
        })
        TestScope().launch { viewModel.setChat("userId") }
    }

    @Test
    fun testChatUiState_groupChat_otherParticipantsStateUpdated() = runTest {
        chatParticipantsFlow.value = groupChatParticipantsMock
        val uiState = viewModel.uiState.first() as? ChatUiState.Group
        val actual = uiState?.participantsState
        assertEquals(null, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as ChatUiState.Group
        val newActual = newUiState.participantsState
        val newExpected = ChatParticipantsState(online = ImmutableList(listOf("otherDisplayName", "otherDisplayName2")))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun testChatUiState_groupChat_participantsDetailsUpdated() = runTest {
        chatParticipantsFlow.value = groupChatParticipantsMock
        val uiState = viewModel.uiState.first() as? ChatUiState.Group
        val actual = uiState?.participantsDetails
        assertEquals(null, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as ChatUiState.Group
        val newActual = newUiState.participantsDetails.value
        val newExpected = hashMapOf(
            "myUserId" to ChatParticipantDetails("myUsername", ImmutableUri(myUri), flowOf(ChatParticipantState.Online)),
            "otherUserId" to ChatParticipantDetails("otherDisplayName", ImmutableUri(otherUri), flowOf(ChatParticipantState.Online)),
            "otherUserId2" to ChatParticipantDetails("otherDisplayName2", ImmutableUri(otherUri2), flowOf(ChatParticipantState.Online))
        )
        newActual.forEach { (key, entry) ->
            areChatParticipantDetailsEquals(newExpected[key]!!, entry)
        }
    }

    @Test
    fun testChatUiState_oneToOneChat_recipientDetailsUpdates() = runTest {
        chatParticipantsFlow.value = oneToOneChatParticipantsFlow
        val uiState = viewModel.uiState.first() as ChatUiState.OneToOne
        val actual = uiState.recipientDetails
        val expected = ChatParticipantDetails()
        areChatParticipantDetailsEquals(expected, actual)
        advanceUntilIdle()
        val newUiState = viewModel.uiState.first() as ChatUiState.OneToOne
        val newActual = newUiState.recipientDetails
        val newExpected = ChatParticipantDetails(
            username = "otherDisplayName",
            image = ImmutableUri(otherUri),
            state = flowOf(ChatParticipantState.Online)
        )
        areChatParticipantDetailsEquals(newExpected, newActual)
    }

    @Test
    fun testChatUiState_isInCallUpdated() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val current = viewModel.uiState.first().isInCall
        Assert.assertEquals(false, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().isInCall
        Assert.assertEquals(true, new)
    }

    @Test
    fun testChatUiState_actionsUpdated() = runTest {
        val current = viewModel.uiState.first().actions
        Assert.assertEquals(ImmutableSet<ChatAction>(), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().actions.value
        assert(new.filterIsInstance<ChatAction.AudioUpgradableCall>().isNotEmpty())
        assert(new.filterIsInstance<ChatAction.VideoCall>().isNotEmpty())
    }

    @Test
    fun testChatUiState_conversationItemsUpdated() = runTest {
        val current = viewModel.uiState.first().conversationState.conversationItems
        Assert.assertEquals(null, current)
        advanceUntilIdle()
        val unreadMessage = findFirstUnreadMessageId(messagesUIMock) { mockk() }

        val actual = viewModel.uiState.first().conversationState.conversationItems
        val expected = listOf(otherTodayUnreadMessage, otherTodayReadMessage).mapToConversationItems(unreadMessage)
        Assert.assertEquals(ImmutableList(expected), actual)
    }

    @Test
    fun `unread messages items is removed if a message is sent`() = runTest {
        val current = viewModel.uiState.first().conversationState.conversationItems
        Assert.assertEquals(null, current)
        advanceUntilIdle()
        val unreadMessage = findFirstUnreadMessageId(messagesUIMock) { mockk() }

        val actual = viewModel.uiState.first().conversationState.conversationItems
        val expected = listOf(otherTodayUnreadMessage, otherTodayReadMessage).mapToConversationItems(unreadMessage)
        Assert.assertEquals(ImmutableList(expected), actual)

        viewModel.sendMessage("text")

        val newMessagesUIMock = mockk<MessagesUI>(relaxed = true)
        every { newMessagesUIMock.list } returns messagesUIMock.list
        messagesFlow.value =  newMessagesUIMock

        advanceUntilIdle()
        val newActual = viewModel.uiState.first().conversationState.conversationItems
        val newExpected = messagesUIMock.list.mapToConversationItems()
        Assert.assertEquals(ImmutableList(newExpected), newActual)
    }

    @Test
    fun testChatUiState_connectionStateUpdated() = runTest {
        every { conversationMock.state } returns MutableStateFlow(State.Connecting)
        val current = viewModel.uiState.first().connectionState
        Assert.assertEquals(ConnectionState.Unknown, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().connectionState
        Assert.assertEquals(ConnectionState.Connecting, new)
    }

    @Test
    fun testChatUiState_unreadMessagesUpdated() = runTest {
        val current = viewModel.uiState.first().conversationState.unreadMessagesCount
        Assert.assertEquals(0, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().conversationState.unreadMessagesCount
        Assert.assertEquals(5, new)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }

    @Test
    fun testSendMessage() = runTest {
        advanceUntilIdle()
        val text = "text"
        viewModel.sendMessage(text)
        verify { chatMock.add(match { it is Message.Content.Text && it.message == text }) }
    }

    @Test
    fun testTyping() = runTest {
        advanceUntilIdle()
        viewModel.typing()
        verify { oneToOneChatParticipantsFlow.me!!.typing() }
    }

    @Test
    fun testFetchMessages() = runTest {
        advanceUntilIdle()
        mockkObject(MessagesMapper)
        val fetchedMessagesUIMock = mockk<MessagesUI>()
        every { fetchedMessagesUIMock.list } returns listOf(otherTodayUnreadMessage2, otherTodayUnreadMessage, otherTodayReadMessage)
        coEvery { chatMock.fetch(any()) } coAnswers {
            delay(2000L)
            messagesFlow.value = fetchedMessagesUIMock
            Result.success(mockk(relaxed = true))
        }

        val isFetchingValues = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.map { it.conversationState.isFetching }.toList(isFetchingValues)
        }

        viewModel.fetchMessages()
        advanceUntilIdle()

        coVerify { chatMock.fetch(any()) }
        assertEquals(false, isFetchingValues[0])
        assertEquals(true, isFetchingValues[1])
        assertEquals(false, isFetchingValues[2])
        val actualItems = viewModel.uiState.first().conversationState.conversationItems?.value
        val expectedItems = fetchedMessagesUIMock.list.mapToConversationItems(otherTodayUnreadMessage.id)
        assertEquals(expectedItems, actualItems)
    }

    @Test
    fun testOnMessageScrolled() = runTest {
        advanceUntilIdle()
        val message = mockk<com.kaleyra.collaboration_suite_phone_ui.chat.conversation.model.Message.OtherMessage>()
        every { message.id } returns otherTodayUnreadMessage.id
        viewModel.onMessageScrolled(ConversationItem.Message(message))
        verify { otherTodayUnreadMessage.markAsRead() }
    }

    @Test
    fun testOnAllMessagesScrolled() = runTest {
        advanceUntilIdle()
        viewModel.onAllMessagesScrolled()
        verify { otherTodayUnreadMessage.markAsRead() }
    }

    @Test
    fun testShowCall() = runTest {
        advanceUntilIdle()
        viewModel.showCall()
        verify { callMock.show() }
    }

    private suspend fun areChatParticipantDetailsEquals(expected: ChatParticipantDetails,  actual: ChatParticipantDetails) {
        assertEquals(expected.username, actual.username)
        assertEquals(expected.image, actual.image)
        assertEquals(expected.state.firstOrNull(), actual.state.firstOrNull())
    }

}