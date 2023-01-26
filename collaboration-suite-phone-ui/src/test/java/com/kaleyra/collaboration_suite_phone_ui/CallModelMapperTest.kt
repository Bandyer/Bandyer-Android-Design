package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.callParticipantsMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMeMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMock1
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.participantMock2
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.recordingMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.streamMock1
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.streamMock2
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.streamMock3
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.uriMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.videoMock
import com.kaleyra.collaboration_suite_phone_ui.PhoneBoxMocks.viewMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class CallModelMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val displayNameFlow = MutableStateFlow("displayName")

    private val displayImageFlow = MutableStateFlow(uriMock)

    private val streamUi1 = StreamUi(
        id = "streamId1",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    private val streamUi2 = StreamUi(
        id = "streamId2",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    private val streamUi3 = StreamUi(
        id = "streamId3",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    @Before
    fun setUp() {
        // only needed for toCallStateUi function
        every { callParticipantsMock.me } returns participantMeMock
        every { callParticipantsMock.creator() } returns participantMeMock
        every { callParticipantsMock.list } returns listOf(participantMock1, participantMock2)
        every { participantMock1.streams } returns MutableStateFlow(
            listOf(
                streamMock1,
                streamMock2
            )
        )
        every { participantMock2.streams } returns MutableStateFlow(listOf(streamMock3))
        every { participantMock1.displayName } returns MutableStateFlow("displayName1")
        every { participantMock2.displayName } returns MutableStateFlow("displayName2")
        every { participantMock1.displayImage } returns MutableStateFlow(uriMock)
        every { participantMock2.displayImage } returns MutableStateFlow(uriMock)
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptyOtherParticipants_toOtherDisplayNames_emptyList() = runTest {
        every { callParticipantsMock.others } returns listOf()
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf<String>()
        assertEquals(expected, actual)
    }

    @Test
    fun filledOtherParticipants_toOtherDisplayNames_displayNamesList() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        assertEquals(expected, actual)
    }

    @Test
    fun addOtherParticipant_toOtherDisplayNames() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1")
        assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayName2")
        assertEquals(newExpected, newActual)
    }

    @Test
    fun removeOtherParticipant_toOtherDisplayNames() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        assertEquals(expected, actual)

        val newCallParticipantsMock = mockk<CallParticipants> {
            every { others } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf("displayName1")
        assertEquals(newExpected, newActual)
    }

    @Test
    fun displayNameUpdate_toOtherDisplayNames() = runTest {
        val displayNameFlow = MutableStateFlow("displayName2")
        every { participantMock2.displayName } returns displayNameFlow
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.toOtherDisplayNames()
        val actual = result.first()
        val expected = listOf("displayName1", "displayName2")
        assertEquals(expected, actual)

        displayNameFlow.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf("displayName1", "displayNameModified")
        assertEquals(newExpected, newActual)
    }

    @Test
    fun mapToRecordingUi_null() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.Never
        val result = MutableStateFlow(recordingMock).mapToRecordingUi()
        assertEquals(null, result.first())
    }

    @Test
    fun mapToRecordingUi_onConnect() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        val result = MutableStateFlow(recordingMock).mapToRecordingUi()
        assertEquals(Recording.OnConnect, result.first())
    }

    @Test
    fun mapToRecordingUi_onDemand() = runTest {
        every { recordingMock.type } returns Call.Recording.Type.OnDemand
        val result = MutableStateFlow(recordingMock).mapToRecordingUi()
        assertEquals(Recording.OnDemand, result.first())
    }

    @Test
    fun isRecording_true() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Started)
        val result = MutableStateFlow(callMock).isRecording()
        assertEquals(true, result.first())
    }

    @Test
    fun isRecording_false() = runTest {
        every { callMock.extras.recording.state } returns MutableStateFlow(Call.Recording.State.Stopped)
        val result = MutableStateFlow(callMock).isRecording()
        assertEquals(false, result.first())
    }

    @Test
    fun connected_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connected)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Connected, result.first())
    }

    @Test
    fun reconnecting_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Reconnecting)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Reconnecting, result.first())
    }

    @Test
    fun connecting_meCreator_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Dialing, result.first())
    }

    @Test
    fun connecting() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Connecting)
        every { callParticipantsMock.creator() } returns participantMock1
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Connecting, result.first())
    }

    @Test
    fun answeredOnAnotherDevice_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.AnsweredOnAnotherDevice)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.AnsweredOnAnotherDevice, result.first())
    }

    @Test
    fun declined_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Declined)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Declined, result.first())
    }

    @Test
    fun lineBusy_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.LineBusy)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.LineBusy, result.first())
    }

    @Test
    fun timeout_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Timeout)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Timeout, result.first())
    }

    @Test
    fun serverError_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Server())
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Error.Server, result.first())
    }

    @Test
    fun unknownError_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error.Unknown())
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Error.Unknown, result.first())
    }

    @Test
    fun hungUp_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.HungUp())
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.HungUp, result.first())
    }

    @Test
    fun kicked_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Kicked("userId"))
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Kicked("userId"), result.first())
    }

    @Test
    fun error_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended.Error)
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected.Ended.Error, result.first())
    }

    @Test
    fun disconnected_creatorNotMe_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns participantMock1
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Ringing, result.first())
    }

    @Test
    fun disconnected_toCallStateUi() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected)
        every { callParticipantsMock.creator() } returns participantMeMock
        val result = MutableStateFlow(callMock).toCallStateUi()
        assertEquals(CallState.Disconnected, result.first())
    }

    @Test
    fun singleOtherParticipants_isGroupCall_false() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall()
        assertEquals(false, result.first())
    }

    @Test
    fun multipleOtherParticipants_isGroupCall_true() = runTest {
        every { callParticipantsMock.others } returns listOf(participantMock1, participantMock2)
        val call = MutableStateFlow(callMock)
        val result = call.isGroupCall()
        assertEquals(true, result.first())
    }

    @Test
    fun emptyParticipantsList_reduceToStreamsUi() = runTest {
        every { callParticipantsMock.list } returns listOf()

        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        assertEquals(expected, actual)
    }

    @Test
    fun filledParticipantsList_reduceToStreamsUi() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(expected, actual)
    }

    @Test
    fun allParticipantsHaveNoStreams_reduceToStreamsUi() = runTest {
        every { participantMock1.streams } returns MutableStateFlow(listOf())
        every { participantMock2.streams } returns MutableStateFlow(listOf())

        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        assertEquals(expected, actual)
    }

    @Test
    fun addNewParticipant_reduceToStreamsUi() = runTest {
        every { callParticipantsMock.list } returns listOf(participantMock1)

        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
        )
        assertEquals(expected, actual)

        // Update participants list
        val newCallParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(participantMock1, participantMock2)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun removeParticipant_reduceToStreamsUi() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(expected, actual)

        // Update participants list
        val newCallParticipantsMock = mockk<CallParticipants> {
            every { list } returns listOf(participantMock1)
        }
        participants.value = newCallParticipantsMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1")
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateParticipantDisplayName_reduceToStreamsUi() = runTest {
        val displayNameParticipant1 = MutableStateFlow("displayName1")
        every { participantMock1.displayName } returns displayNameParticipant1

        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(expected, actual)

        // Update participants list
        displayNameParticipant1.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayNameModified"),
            streamUi2.copy(username = "displayNameModified"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateParticipantDisplayImage_reduceToStreamsUi() = runTest {
        val displayImageParticipant1 = MutableStateFlow(uriMock)
        every { participantMock1.displayImage } returns displayImageParticipant1

        val participants = MutableStateFlow(callParticipantsMock)
        val result = participants.reduceToStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(expected, actual)

        // Update participants list
        val newUriMock = mockk<Uri>()
        displayImageParticipant1.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayName1", avatar = ImmutableUri(newUriMock)),
            streamUi2.copy(username = "displayName1", avatar = ImmutableUri(newUriMock)),
            streamUi3.copy(username = "displayName2")
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        assertEquals(expected, actual)
    }

    @Test
    fun emptyListToFilledList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        assertEquals(expected, actual)

        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        assertEquals(newExpected, newActual)
    }

    @Test
    fun filledListToEmptyList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        assertEquals(expected, actual)

        streams.value = listOf()
        val newActual = result.first()
        val newExpected = listOf<StreamUi>()
        assertEquals(newExpected, newActual)
    }

    @Test
    fun filledList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        assertEquals(expected, actual)
    }

    @Test
    fun removeElementsFromList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        assertEquals(newExpected, newActual)
    }

    @Test
    fun addElementsToList_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1, streamMock2, streamMock3)
        val newActual = result.first()
        val newExpected = listOf(streamUi1, streamUi2, streamUi3)
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateStreamVideo_mapToStreamsUi() = runTest {
        val modifiedStreamVideoFlow = MutableStateFlow(videoMock)
        val modifiedStreamMock = mockk<Stream> {
            every { id } returns "modifiedStreamId"
            every { this@mockk.video } returns modifiedStreamVideoFlow
        }
        val modifiedStreamUi = StreamUi(
            id = "modifiedStreamId",
            video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )

        val streams = MutableStateFlow(listOf(streamMock1, modifiedStreamMock, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, modifiedStreamUi, streamUi3)
        assertEquals(expected, actual)

        // Update stream video
        val newStreamVideoMock = mockk<Input.Video.Camera>(relaxed = true) {
            every { id } returns "videoId2"
            every { this@mockk.view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(false)
        }
        modifiedStreamVideoFlow.value = newStreamVideoMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1,
            modifiedStreamUi.copy(
                video = VideoUi(
                    id = "videoId2",
                    view = viewMock,
                    isEnabled = false
                )
            ),
            streamUi3
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayName_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        assertEquals(expected, actual)

        // Update display name
        displayNameFlow.value = "newDisplayName"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "newDisplayName"),
            streamUi2.copy(username = "newDisplayName"),
            streamUi3.copy(username = "newDisplayName")
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayImage_mapToStreamsUi() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        assertEquals(expected, actual)

        // Update display name
        val newUriMock = mockk<Uri>()
        displayImageFlow.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(avatar = ImmutableUri(newUriMock)),
            streamUi2.copy(avatar = ImmutableUri(newUriMock)),
            streamUi3.copy(avatar = ImmutableUri(newUriMock))
        )
        assertEquals(newExpected, newActual)
    }

    @Test
    fun videoInputNull_mapToVideoUi_null() = runTest {
        val actual = MutableStateFlow(null).mapToVideoUi().first()
        assertEquals(null, actual)
    }

    @Test
    fun videoInput_mapToVideoUi_videoUi() = runTest {
        val flow = MutableStateFlow(videoMock)
        val actual = flow.mapToVideoUi().first()
        val expected = VideoUi("videoId", viewMock, true)
        assertEquals(expected, actual)
    }
}