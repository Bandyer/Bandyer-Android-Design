package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.isGroupCall
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayImages
import com.kaleyra.collaboration_suite_phone_ui.call.compose.mapper.ParticipantMapper.toOtherDisplayNames
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.*

@ExperimentalCoroutinesApi
class ParticipantMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val uriMock1 = mockk<Uri>()

    private val uriMock2 = mockk<Uri>()

    @Before
    fun setUp() {
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(participantMock1) {
            every { userId } returns "userId1"
            every { displayName } returns MutableStateFlow("displayName1")
            every { displayImage } returns MutableStateFlow(uriMock1)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { displayName } returns MutableStateFlow("displayName2")
            every { displayImage } returns MutableStateFlow(uriMock2)
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
            every { displayName } returns displayNameFlow
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
        val result = call.isGroupCall()
        Assert.assertEquals(false, result.first())
    }

    @Test
    fun multipleOtherParticipants_isGroupCall_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall()
        Assert.assertEquals(true, result.first())
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
            every { displayImage } returns displayImageFlow
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

}