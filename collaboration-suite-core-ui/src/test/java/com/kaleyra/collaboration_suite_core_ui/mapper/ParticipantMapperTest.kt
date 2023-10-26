package com.kaleyra.collaboration_suite_core_ui.mapper

import android.net.Uri
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.conference.CallParticipants
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.mapper.ParticipantMapper.toInCallParticipants
import com.kaleyra.collaboration_suite_core_ui.mapper.ParticipantMapper.toMe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
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
    fun testToMe() = runTest {
        every { callParticipantsMock.me } returns participantMeMock
        val actual = flowOf(callMock).toMe().first()
        Assert.assertEquals(participantMeMock, actual)
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
        Assert.assertEquals(expected, actual)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.NotInCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            Assert.assertEquals(expected, actual)
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
            every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.InCall)
            every { streams } returns MutableStateFlow(listOf())
        }

        val result = flowOf(callMock).toInCallParticipants()
        val actual = result.first()
        val expected = listOf(meMock, participantMock1)
        Assert.assertEquals(expected, actual)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.NotInCall)
                every { streams } returns MutableStateFlow(listOf(mockk(relaxed = true)))
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            Assert.assertEquals(expected, actual)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.InCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val participantsFlow = MutableStateFlow(callParticipantsMock)
            every { callMock.participants } returns participantsFlow

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            Assert.assertEquals(expected, actual)

            val newCallParticipantsMock = mockk<CallParticipants> {
                every { others } returns listOf(participantMock1)
                every { me } returns meMock
            }
            participantsFlow.value = newCallParticipantsMock
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            Assert.assertEquals(newExpected, new)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.InCall)
                every { streams } returns MutableStateFlow(listOf())
            }

            val participantsFlow = MutableStateFlow(callParticipantsMock)
            every { callMock.participants } returns participantsFlow

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            Assert.assertEquals(expected, actual)

            val newCallParticipantsMock = mockk<CallParticipants> {
                every { others } returns listOf()
                every { me } returns meMock
            }
            participantsFlow.value = newCallParticipantsMock
            val new = result.first()
            val newExpected = listOf(meMock)
            Assert.assertEquals(newExpected, new)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.NotInCall)
                every { streams } returns participantStreams
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock)
            Assert.assertEquals(expected, actual)

            participantStreams.value = listOf(mockk())
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            Assert.assertEquals(newExpected, new)
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
                every { state } returns MutableStateFlow(com.kaleyra.collaboration_suite.conference.CallParticipant.State.NotInCall)
                every { streams } returns participantStreams
            }

            val result = flowOf(callMock).toInCallParticipants()
            val actual = result.first()
            val expected = listOf(meMock, participantMock1)
            Assert.assertEquals(expected, actual)

            participantStreams.value = listOf()
            val new = result.first()
            val newExpected = listOf(meMock)
            Assert.assertEquals(newExpected, new)
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
            Assert.assertEquals(expected, actual)

            participantState.value = CallParticipant.State.InCall
            val new = result.first()
            val newExpected = listOf(meMock, participantMock1)
            Assert.assertEquals(newExpected, new)
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
            Assert.assertEquals(expected, actual)

            participantState.value = CallParticipant.State.NotInCall
            val new = result.first()
            val newExpected = listOf(meMock)
            Assert.assertEquals(newExpected, new)
        }
}