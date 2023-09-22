package com.kaleyra.collaboration_suite_phone_ui.mapper.chat

import android.net.Uri
import com.kaleyra.collaboration_suite.conversation.ChatParticipant
import com.kaleyra.collaboration_suite.conversation.ChatParticipants
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.Mocks
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantDetails
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.isGroupChat
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatInfo
import com.kaleyra.collaboration_suite_phone_ui.chat.mapper.ParticipantsMapper.toChatParticipantsDetails
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class ParticipantsMapperTest {

    private val chatParticipants = mockk<ChatParticipants>()

    private val otherParticipant = mockk<ChatParticipant>(relaxed = true)

    private val otherParticipant2 = mockk<ChatParticipant>(relaxed = true)

    private val meParticipant = mockk<ChatParticipant.Me>(relaxed = true)

    private val myUri = mockk<Uri>()

    private val otherUri = mockk<Uri>()

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        with(meParticipant) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns flowOf("myUsername")
            every { combinedDisplayImage } returns flowOf(myUri)
        }
        with(otherParticipant) {
            every { userId } returns "otherUserId"
            every { combinedDisplayName } returns flowOf("otherUsername")
            every { combinedDisplayImage } returns flowOf(otherUri)
        }
    }

    @Test
    fun emptyParticipants_toChatParticipantDetails_emptyMap() = runTest {
        every { chatParticipants.list } returns listOf()
        val result = flowOf(chatParticipants).toChatParticipantsDetails()
        val expected = ImmutableMap<String, ParticipantDetails>()
        assertEquals(expected, result.first())
    }

    @Test
    fun filledParticipants_toChatParticipantDetails_filledMap() = runTest {
        every { chatParticipants.list } returns listOf(meParticipant, otherParticipant)
        val result = flowOf(chatParticipants).toChatParticipantsDetails()
        val expected = ImmutableMap(
            hashMapOf(
                "myUserId" to ParticipantDetails("myUsername", ImmutableUri(myUri)),
                "otherUserId" to ParticipantDetails("otherUsername", ImmutableUri(otherUri)),
            )
        )
        assertEquals(expected, result.first())
    }

    @Test
    fun userDisplayNameChanges_toChatParticipantDetails_participantDetailsIsUpdated() = runTest {
        val name = MutableStateFlow("otherUsername")
        every { otherParticipant.combinedDisplayName } returns name
        every { chatParticipants.list } returns listOf(otherParticipant)
        val result = flowOf(chatParticipants).toChatParticipantsDetails()
        val expected = ImmutableMap(
            hashMapOf("otherUserId" to ParticipantDetails("otherUsername", ImmutableUri(otherUri)))
        )
        assertEquals(expected, result.first())
        name.value = "newOtherUsername"
        val newExpected = ImmutableMap(
            hashMapOf("otherUserId" to ParticipantDetails("newOtherUsername", ImmutableUri(otherUri)))
        )
        assertEquals(newExpected, result.first())
    }

    @Test
    fun userDisplayImageChanges_toChatParticipantDetails_participantDetailsIsUpdated() = runTest {
        val image = MutableStateFlow(otherUri)
        every { otherParticipant.combinedDisplayImage } returns image
        every { chatParticipants.list } returns listOf(otherParticipant)
        val result = flowOf(chatParticipants).toChatParticipantsDetails()
        val expected = ImmutableMap(
            hashMapOf("otherUserId" to ParticipantDetails("otherUsername", ImmutableUri(otherUri)))
        )
        assertEquals(expected, result.first())
        val newUri = mockk<Uri>()
        image.value = newUri
        val newExpected = ImmutableMap(
            hashMapOf("otherUserId" to ParticipantDetails("otherUsername", ImmutableUri(newUri)))
        )
        assertEquals(newExpected, result.first())
    }

    @Test
    fun twoParticipants_isGroupChat_false() = runTest {
        every { chatParticipants.others } returns listOf(otherParticipant)
        val result = flowOf(chatParticipants).isGroupChat().first()
        assertEquals(false, result)
    }

    @Test
    fun moreThanTwoParticipants_isGroupChat_true() = runTest {
        every { chatParticipants.others } returns listOf(otherParticipant, otherParticipant2)
        val result = flowOf(chatParticipants).isGroupChat().first()
        assertEquals(true, result)
    }

    @Test
    fun usersDetails_getChatInfo_userIdAndImageUri() = runTest {
        mockkObject(ContactDetailsManager)
        val uriMock = mockk<Uri>()
        every { Mocks.otherParticipantMock.combinedDisplayName } returns MutableStateFlow("customDisplayName")
        every { Mocks.otherParticipantMock.combinedDisplayImage } returns MutableStateFlow(uriMock)
        Assert.assertEquals(flowOf(Mocks.chatParticipantsMock).toChatInfo().first(), ChatInfo("customDisplayName", ImmutableUri(uriMock)))
    }
}