package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.appbar.model.ChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.isGroupChat
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.mapToChatParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatParticipantState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toOtherParticipantsState
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toParticipantsDetails
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.time.Instant
import java.util.Date

class ParticipantsMapperTest {

    private val chatParticipants = mockk<ChatParticipants>()

    private val otherParticipant = mockk<ChatParticipant>(relaxed = true)

    private val otherParticipant2 = mockk<ChatParticipant>(relaxed = true)

    private val meParticipant = mockk<ChatParticipant.Me>(relaxed = true)

    private val myUri = mockk<Uri>()

    private val otherUri = mockk<Uri>()

    private val otherUri2 = mockk<Uri>()

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        with(chatParticipants) {
            every { me } returns meParticipant
            every { others } returns listOf(otherParticipant, otherParticipant2)
        }
        with(meParticipant) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns flowOf("myUsername")
            every { combinedDisplayImage } returns flowOf(myUri)
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never))
            every { events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        }
        with(otherParticipant) {
            every { userId } returns "otherUserId"
            every { combinedDisplayName } returns flowOf("otherUsername")
            every { combinedDisplayImage } returns flowOf(otherUri)
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        }
        with(otherParticipant2) {
            every { userId } returns "otherUserId2"
            every { combinedDisplayName } returns flowOf("otherUsername2")
            every { combinedDisplayImage } returns flowOf(otherUri2)
            every { state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
            every { events } returns MutableStateFlow(ChatParticipant.Event.Typing.Started)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun twoParticipants_isGroupChat_false() {
        every { chatParticipants.others } returns listOf(otherParticipant)
        val result = chatParticipants.isGroupChat()
        assertEquals(false, result)
    }

    @Test
    fun moreThanTwoParticipants_isGroupChat_true() {
        every { chatParticipants.others } returns listOf(otherParticipant, otherParticipant2)
        val result = chatParticipants.isGroupChat()
        assertEquals(true, result)
    }

    @Test
    fun emptyParticipants_toParticipantDetails_emptyMap() = runTest {
        every { chatParticipants.list } returns listOf()
        val result = chatParticipants.toParticipantsDetails()
        val expected = ImmutableMap<String, ChatParticipantDetails>()
        assertEquals(expected, result)
    }

    @Test
    fun filledParticipants_toChatParticipantDetails_filledMap() = runTest {
        every { chatParticipants.list } returns listOf(meParticipant, otherParticipant)
        val result = chatParticipants.toParticipantsDetails()
        val expected = ImmutableMap(
            hashMapOf(
                "myUserId" to ChatParticipantDetails("myUsername", ImmutableUri(myUri), flowOf(ChatParticipantState.Offline())),
                "otherUserId" to ChatParticipantDetails("otherUsername", ImmutableUri(otherUri), flowOf(ChatParticipantState.Online)),
            )
        )
        assertChatParticipantDetailsAreEquals(expected["myUserId"]!!, result["myUserId"]!!)
        assertChatParticipantDetailsAreEquals(expected["otherUserId"]!!, result["otherUserId"]!!)
    }

    private suspend fun assertChatParticipantDetailsAreEquals(expected: ChatParticipantDetails, actual: ChatParticipantDetails) {
        assertEquals(expected.username, actual.username)
        assertEquals(expected.image, actual.image)
        assertEquals(expected.state.first(), actual.state.first())
    }

    @Test
    fun userIsTyping_toChatParticipantState_chatParticipantStateTyping() = runTest {
        every { otherParticipant.state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
        every { otherParticipant.events } returns MutableStateFlow(ChatParticipant.Event.Typing.Started)
        val result = otherParticipant.toChatParticipantState()
        val expected = ChatParticipantState.Typing
        assertEquals(expected, result.first())
    }

    @Test
    fun userIsOnline_toChatParticipantState_chatParticipantStateOnline() = runTest {
        every { otherParticipant.state } returns MutableStateFlow(ChatParticipant.State.Joined.Online)
        every { otherParticipant.events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        val result = otherParticipant.toChatParticipantState()
        val expected = ChatParticipantState.Online
        assertEquals(expected, result.first())
    }

    @Test
    fun userIsOfflineWithNoLastLogin_toChatParticipantState_chatParticipantStateOffline() = runTest {
        every { otherParticipant.state } returns MutableStateFlow(ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.Never))
        every { otherParticipant.events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        val result = otherParticipant.toChatParticipantState()
        val expected = ChatParticipantState.Offline(null)
        assertEquals(expected, result.first())
    }

    @Test
    fun userIsOfflineWithLastLogin_toChatParticipantState_chatParticipantStateOffline() = runTest {
        val nowMillis = Instant.now().toEpochMilli()
        every { otherParticipant.state } returns MutableStateFlow(ChatParticipant.State.Joined.Offline(ChatParticipant.State.Joined.Offline.LastLogin.At(
            Date(nowMillis)
        )))
        every { otherParticipant.events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        val result = otherParticipant.toChatParticipantState()
        val expected = ChatParticipantState.Offline(nowMillis)
        assertEquals(expected, result.first())
    }

    @Test
    fun userIsInvited_toChatParticipantState_chatParticipantStateUnknown() = runTest {
        every { otherParticipant.state } returns MutableStateFlow(ChatParticipant.State.Invited)
        every { otherParticipant.events } returns MutableStateFlow(ChatParticipant.Event.Typing.Idle)
        val result = otherParticipant.toChatParticipantState()
        val expected = ChatParticipantState.Unknown
        assertEquals(expected, result.first())
    }

    @Test
    fun emptyOtherParticipants_toOthersParticipantsState_chatParticipantsState() = runTest {
        every { chatParticipants.others } returns listOf()
        val result = chatParticipants.toOtherParticipantsState().first()
        val expected = ChatParticipantsState()
        assertEquals(expected, result)
    }

    @Test
    fun otherParticipants_toOthersParticipantsState_chatParticipantsState() = runTest {
        every { chatParticipants.others } returns listOf(otherParticipant, otherParticipant2)
        val result = chatParticipants.toOtherParticipantsState().first()
        val expected = ChatParticipantsState(
            online = ImmutableList(listOf("otherUsername")),
            typing = ImmutableList(listOf("otherUsername2")),
        )
        assertEquals(expected, result)
    }

    @Test
    fun emptyList_mapToChatParticipantsState_chatParticipantsState() {
        val list = listOf<Pair<String, ChatParticipantState>>()
        val result = list.mapToChatParticipantsState()
        assertEquals(ChatParticipantsState(), result)
    }

    @Test
    fun filledList_mapToChatParticipantsState_chatParticipantsState() {
        val list = listOf(
            "user1" to ChatParticipantState.Online,
            "user2" to ChatParticipantState.Online,
            "user3" to ChatParticipantState.Typing,
            "user4" to ChatParticipantState.Offline(),
            "user5" to ChatParticipantState.Offline(),
            "user6" to ChatParticipantState.Offline(),
        )
        val result = list.mapToChatParticipantsState()
        val expected = ChatParticipantsState(
            online = ImmutableList(listOf("user1", "user2")),
            typing = ImmutableList(listOf("user3")),
            offline = ImmutableList(listOf("user4", "user5", "user6")),
        )
        assertEquals(expected, result)
    }

}