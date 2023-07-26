package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toMe
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayNames
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.Assert.assertEquals

@ExperimentalCoroutinesApi
class ParticipantMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>(relaxed = true)

    private val callParticipantsMock = mockk<CallParticipants>(relaxed = true)

    private val participantMock1 = mockk<CallParticipant>(relaxed = true)

    private val participantMock2 = mockk<CallParticipant>(relaxed = true)

    private val participantMeMock = mockk<CallParticipant.Me>(relaxed = true)

    private val uriMock1 = mockk<Uri>(relaxed = true)

    private val uriMock2 = mockk<Uri>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(participantMock1) {
            every { userId } returns "userId1"
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock1)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock2)
        }
    }

    @Test
    fun emptyOtherParticipants_toOtherDisplayNames_emptyList() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf<String>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledOtherParticipants_toOtherDisplayNames_displayNamesList() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addOtherParticipant_toOtherDisplayNames_displayNameAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1")
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayName2")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeOtherParticipant_toOtherDisplayNames_displayNameRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun displayNameUpdate_toOtherDisplayNames_displayNameUpdated() = runTest {
        val displayNameFlow = MutableStateFlow("displayName2")
        val participantMock3 = mockk<CallParticipant> {
            every { userId } returns "userId3"
            every { combinedDisplayName } returns displayNameFlow
        }
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock3)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        Assert.assertEquals(expected, actual)

        displayNameFlow.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayNameModified")
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun singleOtherParticipants_isGroupCall_false() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun multipleOtherParticipants_isGroupCall_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(true, result.first())
    }

    @Test
    fun `when a participant userId is the companyId, they are not counted as group member`() = runTest {
        every { participantMock1.userId } returns "companyId"
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall(MutableStateFlow("companyId"))
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun emptyOtherParticipants_toOtherDisplayImages_emptyList() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf<Uri>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledOtherParticipants_toOtherDisplayImages_displayImagesList() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addOtherParticipant_toOtherDisplayImages_displayImageAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1)
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeOtherParticipant_toOtherDisplayImages_displayImageRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(uriMock1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun displayNameUpdate_toOtherDisplayImages_displayNameUpdated() = runTest {
        val uriMock3 = mockk<Uri>()
        val displayImageFlow = MutableStateFlow(uriMock2)
        val participantMock3 = mockk<CallParticipant> {
            every { userId } returns "userId3"
            every { combinedDisplayImage } returns displayImageFlow
        }
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock3)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        val call = MutableStateFlow(callMock)
        val result = call.toOtherDisplayImages()
        val actual = result.first()
        val expected = listOf(uriMock1, uriMock2)
        Assert.assertEquals(expected, actual)

        displayImageFlow.value = uriMock3
        val newActual = result.first()
        val newExpected = listOf(uriMock1, uriMock3)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun `there are no other participant, the only participant in call it's me`() = runTest {
        val meMock = mockk<CallParticipant.Me> {
            every { userId } returns "myUserId"
        }
        with(callParticipantsMock) {
            every { others } returns listOf()
            every { me } returns meMock
        }
        every { meMock.state } returns MutableStateFlow(CallParticipant.State.NotInCall)

        val result = flowOf(callMock).toInCallParticipants()
        val actual = result.first()
        val expected = listOf(meMock)
        assertEquals(expected, actual)
    }

    @Test
    fun `other participants do not in call state and have no streams, the only participant in call it's me`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            assertEquals(expected, actual)
        }

    @Test
    fun `other participant have in call state, they are in the in call participants`() = runTest {
        val meMock = mockk<CallParticipant.Me>(relaxed = true) {
            every { userId } returns "myUserId"
        }
        with(callParticipantsMock) {
            every { others } returns listOf(participantMock1)
            every { me } returns meMock
        }
        with(participantMock1) {
            every { state } returns MutableStateFlow(CallParticipant.State.InCall)
            every { streams } returns MutableStateFlow(listOf())
        }

        val result = flowOf(callMock).toInCallParticipants()
        val actual = result.first()
        val expected = listOf(meMock, participantMock1)
        assertEquals(expected, actual)
    }

    @Test
    fun `other participant does have streams, they are in the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
                every { streams } returns MutableStateFlow(listOf(mockk(relaxed = true)))
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            assertEquals(expected, actual)
        }

    @Test
    fun `new participant with in call state added, they are added in the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            with(callParticipantsMock) {
                every { others } returns listOf()
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.InCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val participantsFlow = MutableStateFlow(callParticipantsMock)
            every { callMock.participants } returns participantsFlow

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            assertEquals(expected, actual)

            val newCallParticipantsMock = mockk<CallParticipants> {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            participantsFlow.value = newCallParticipantsMock
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            assertEquals(newExpected, new)
        }

    @Test
    fun `participant with in call state is removed, they are removed from the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.InCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val participantsFlow = MutableStateFlow(callParticipantsMock)
            every { callMock.participants } returns participantsFlow

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            assertEquals(expected, actual)

            val newCallParticipantsMock = mockk<CallParticipants> {
                every { others } returns listOf()
                every { me } returns meMock
            }
            participantsFlow.value = newCallParticipantsMock
            val new = result.first()
            val newExpected = listOf(meMock)
            assertEquals(newExpected, new)
        }

    @Test
    fun `participant state not in call and get a new stream, they are added in the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            val participantStreams = MutableStateFlow(listOf<Stream>())
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
                every { streams } returns participantStreams
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            assertEquals(expected, actual)

            participantStreams.value = listOf(mockk())
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            assertEquals(newExpected, new)
        }

    @Test
    fun `participant state not in call and remains with no streams, they are added in the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            val participantStreams = MutableStateFlow(listOf<Stream>(mockk()))
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns MutableStateFlow(CallParticipant.State.NotInCall)
                every { streams } returns participantStreams
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            assertEquals(expected, actual)

            participantStreams.value = listOf()
            val new = result.first()
            val newExpected = listOf(meMock)
            assertEquals(newExpected, new)
        }

    @Test
    fun `participant have no streams and goes to in call state, they are added in the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            val participantState = MutableStateFlow<CallParticipant.State>(CallParticipant.State.NotInCall)
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns participantState
                every { streams } returns MutableStateFlow(listOf())
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            assertEquals(expected, actual)

            participantState.value = CallParticipant.State.InCall
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            assertEquals(newExpected, new)
        }

    @Test
    fun `participant have no streams and goes to not in call state, they are removed from the in call participants`() =
        runTest {
            val meMock = mockk<CallParticipant.Me>(relaxed = true) {
                every { userId } returns "myUserId"
            }
            val participantState = MutableStateFlow<CallParticipant.State>(CallParticipant.State.InCall)
            with(callParticipantsMock) {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            with(participantMock1) {
                every { state } returns participantState
                every { streams } returns MutableStateFlow(listOf())
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            assertEquals(expected, actual)

            participantState.value = CallParticipant.State.NotInCall
            val new = result.first()
            val newExpected = listOf(meMock)
            assertEquals(newExpected, new)
        }

    @Test
    fun testToMe() = runTest {
        every { callParticipantsMock.me } returns participantMeMock
        val actual = flowOf(callMock).toMe().first()
        assertEquals(participantMeMock, actual)
    }

}