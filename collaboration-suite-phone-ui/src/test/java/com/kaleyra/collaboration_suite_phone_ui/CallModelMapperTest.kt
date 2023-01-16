package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_phone_ui.call.compose.*
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CallModelMapperTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    val viewMock = mockk<VideoStreamView>()

    val videoMock = mockk<Input.Video.Camera>(relaxed = true) {
        every { id } returns "videoId"
        every { this@mockk.view } returns MutableStateFlow(viewMock)
        every { enabled } returns MutableStateFlow(true)
    }

    val streamMock1 = mockk<Stream> {
        every { id } returns "streamId1"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val streamMock2 = mockk<Stream> {
        every { id } returns "streamId2"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val streamMock3 = mockk<Stream> {
        every { id } returns "streamId3"
        every { this@mockk.video } returns MutableStateFlow(videoMock)
    }

    val uriMock = mockk<Uri>()

    val participantMock1 = mockk<CallParticipant> {
        every { userId } returns "userId1"
        every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
        every { displayName } returns MutableStateFlow("displayName1")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    val participantMock2 = mockk<CallParticipant> {
        every { userId } returns "userId2"
        every { streams } returns MutableStateFlow(listOf(streamMock3))
        every { displayName } returns MutableStateFlow("displayName2")
        every { displayImage } returns MutableStateFlow(uriMock)
    }

    val callParticipantsMock = mockk<CallParticipants> {
        every { list } returns listOf(participantMock1, participantMock2)
    }

    val displayNameFlow = MutableStateFlow("displayName")

    val displayImageFlow = MutableStateFlow(uriMock)

    val streamUi1 = StreamUi(
        id = "streamId1",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    val streamUi2 = StreamUi(
        id = "streamId2",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    val streamUi3 = StreamUi(
        id = "streamId3",
        video = VideoUi(id = "videoId", view = viewMock, isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

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