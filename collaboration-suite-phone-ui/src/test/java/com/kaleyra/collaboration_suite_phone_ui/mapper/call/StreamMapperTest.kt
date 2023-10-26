package com.kaleyra.collaboration_suite_phone_ui.mapper.call

import android.net.Uri
import com.kaleyra.collaboration_suite.conference.*
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayImage
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.ParticipantMapper
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.doIHaveStreams
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.hasAtLeastAVideoEnabled
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.mapToStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.toMyStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.mapper.StreamMapper.toStreamsUi
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.ImmutableView
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.StreamUi
import com.kaleyra.collaboration_suite_phone_ui.call.stream.model.VideoUi
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.*

@OptIn(ExperimentalCoroutinesApi::class)
class StreamMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<Call>()
    
    private val viewMock = mockk<VideoStreamView>()

    private val uriMock = mockk<Uri>()

    private val videoMock = mockk<Input.Video.Camera>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)
    
    private val streamMock1 = mockk<Stream>()

    private val streamMock2 = mockk<Stream>()

    private val streamMock3 = mockk<Stream>()

    private val myStreamMock1 = mockk<Stream.Mutable>()

    private val myStreamMock2 = mockk<Stream.Mutable>()
    
    private val participantMeMock = mockk<CallParticipant.Me>()

    private val participantMock1 = mockk<CallParticipant>()

    private val participantMock2 = mockk<CallParticipant>()

    private val callParticipantsMock = mockk<CallParticipants>()

    private val displayNameFlow = MutableStateFlow("displayName")

    private val displayImageFlow = MutableStateFlow(uriMock)

    private val streamUi1 = StreamUi(
        id = "streamId1",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    private val streamUi2 = StreamUi(
        id = "streamId2",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    private val streamUi3 = StreamUi(
        id = "streamId3",
        video = VideoUi(id = "videoId", view = ImmutableView(viewMock), isEnabled = true),
        username = "displayName",
        avatar = ImmutableUri(uriMock)
    )

    private val myStreamUi1 = StreamUi(
        id = "myStreamId",
        video = VideoUi(id = "myVideoId", view = ImmutableView(viewMock), isEnabled = true),
        username = "myDisplayName",
        avatar = ImmutableUri(uriMock)
    )

    private val myStreamUi2 = StreamUi(
        id = "myStreamId2",
        video = VideoUi(id = "myVideoId", view = ImmutableView(viewMock), isEnabled = true),
        username = "myDisplayName",
        avatar = ImmutableUri(uriMock)
    )

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        // only needed for toCallStateUi function
        every { callMock.participants } returns MutableStateFlow(callParticipantsMock)
        with(callParticipantsMock) {
            every { me } returns participantMeMock
            every { list } returns listOf(participantMock1, participantMock2)
        }
        with(participantMock1) {
            every { userId } returns "userId1"
            every { streams } returns MutableStateFlow(listOf(streamMock1, streamMock2))
            every { combinedDisplayName } returns MutableStateFlow("displayName1")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMock2) {
            every { userId } returns "userId2"
            every { streams } returns MutableStateFlow(listOf(streamMock3))
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(participantMeMock) {
            every { userId } returns "myUserId"
            every { streams } returns MutableStateFlow(listOf(myStreamMock1, myStreamMock2))
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
            every { combinedDisplayImage } returns MutableStateFlow(uriMock)
        }
        with(streamMock1) {
            every { id } returns "streamId1"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock2) {
            every { id } returns "streamId2"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(streamMock3) {
            every { id } returns "streamId3"
            every { video } returns MutableStateFlow(videoMock)
        }
        with(myStreamMock1) {
            every { id } returns "myStreamId"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myStreamMock2) {
            every { id } returns "myStreamId2"
            every { video } returns MutableStateFlow(myVideoMock)
        }
        with(myVideoMock) {
            every { id } returns "myVideoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
        with(videoMock) {
            every { id } returns "videoId"
            every { view } returns MutableStateFlow(viewMock)
            every { enabled } returns MutableStateFlow(true)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptyParticipantsList_toStreamsUi_emptyStreamUiList() = runTest {
        every { callParticipantsMock.list } returns listOf()

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledParticipantsList_toStreamsUi_mappedStreamUiList() = runTest {
        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun allParticipantsHaveNoStreams_toStreamsUi_emptyStreamUiList() = runTest {
        every { participantMock1.streams } returns MutableStateFlow(listOf())
        every { participantMock2.streams } returns MutableStateFlow(listOf())

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun addNewParticipant_toStreamsUi_participantStreamsUiAdded() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants
        every { callParticipantsMock.list } returns listOf(participantMock1)

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
        )
        Assert.assertEquals(expected, actual)

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
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun removeParticipant_toStreamsUi_participantStreamsUiRemoved() = runTest {
        val participants = MutableStateFlow(callParticipantsMock)
        every { callMock.participants } returns participants

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(expected, actual)

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
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateParticipantDisplayName_toStreamsUi_participantStreamUiUpdated() = runTest {
        val displayNameParticipant1 = MutableStateFlow("displayName1")
        every { participantMock1.combinedDisplayName } returns displayNameParticipant1

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        displayNameParticipant1.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayNameModified"),
            streamUi2.copy(username = "displayNameModified"),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateParticipantDisplayImage_toStreamsUi_participantStreamUiUpdated() = runTest {
        val displayImageParticipant1 = MutableStateFlow(uriMock)
        every { participantMock1.combinedDisplayImage } returns displayImageParticipant1

        val call = MutableStateFlow(callMock)
        val result = call.toStreamsUi()
        val actual = result.first()
        val expected = listOf(
            streamUi1.copy(username = "displayName1"),
            streamUi2.copy(username = "displayName1"),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newUriMock = mockk<Uri>()
        displayImageParticipant1.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "displayName1", avatar = ImmutableUri(newUriMock)),
            streamUi2.copy(username = "displayName1", avatar = ImmutableUri(newUriMock)),
            streamUi3.copy(username = "displayName2")
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyStreamList_toMyStreamsUi_emptyList() = runTest {
        every { participantMeMock.streams } returns MutableStateFlow(listOf())

        val call = MutableStateFlow(callMock)
        val result = call.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun filledStreamList_toMyStreamsUi() = runTest {
        val call = MutableStateFlow(callMock)
        val result = call.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(username = "myDisplayName"),
            myStreamUi2.copy(username = "myDisplayName")
        )
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun updateMyDisplayName_toMyStreamsUi() = runTest {
        val myDisplayName = MutableStateFlow("displayName1")
        every { participantMeMock.combinedDisplayName } returns myDisplayName

        val call = MutableStateFlow(callMock)
        val result = call.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(username = "displayName1"),
            myStreamUi2.copy(username = "displayName1")
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        myDisplayName.value = "displayNameModified"
        val newActual = result.first()
        val newExpected = listOf(
            myStreamUi1.copy(username = "displayNameModified"),
            myStreamUi2.copy(username = "displayNameModified")
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateMyDisplayImage_toMyStreamsUi_mappedStreamUiUpdated() = runTest {
        val myDisplayImage = MutableStateFlow(uriMock)
        every { participantMeMock.combinedDisplayImage } returns myDisplayImage

        val call = MutableStateFlow(callMock)
        val result = call.toMyStreamsUi()
        val actual = result.first()
        val expected = listOf(
            myStreamUi1.copy(username = "myDisplayName"),
            myStreamUi2.copy(username = "myDisplayName")
        )
        Assert.assertEquals(expected, actual)

        // Update participants list
        val newUriMock = mockk<Uri>()
        myDisplayImage.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            myStreamUi1.copy(username = "myDisplayName", avatar = ImmutableUri(newUriMock)),
            myStreamUi2.copy(username = "myDisplayName", avatar = ImmutableUri(newUriMock))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyList_mapToStreamsUi_emptyMappedList() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun emptyListToFilledList_mapToStreamsUi_filledMapperList() = runTest {
        val streams = MutableStateFlow(listOf<Stream>())
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf<StreamUi>()
        Assert.assertEquals(expected, actual)

        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun filledListToEmptyList_mapToStreamsUi_emptyMappedList() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        Assert.assertEquals(expected, actual)

        streams.value = listOf()
        val newActual = result.first()
        val newExpected = listOf<StreamUi>()
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun filledList_mapToStreamsUi_filledMappedList() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)
    }

    @Test
    fun removeElementsFromList_mapToStreamsUi_mappedListWithElementsRemoved() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1)
        val newActual = result.first()
        val newExpected = listOf(streamUi1)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun addElementsToList_mapToStreamsUi_mappedListWithElementsAdded() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1)
        Assert.assertEquals(expected, actual)

        // Update streams list
        streams.value = listOf(streamMock1, streamMock2, streamMock3)
        val newActual = result.first()
        val newExpected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateStreamVideo_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val modifiedStreamVideoFlow = MutableStateFlow(videoMock)
        val modifiedStreamMock = mockk<Stream> {
            every { id } returns "modifiedStreamId"
            every { this@mockk.video } returns modifiedStreamVideoFlow
        }
        val modifiedStreamUi = StreamUi(
            id = "modifiedStreamId",
            video = VideoUi(id = "videoId", view = ImmutableView(viewMock), isEnabled = true),
            username = "displayName",
            avatar = ImmutableUri(uriMock)
        )

        val streams = MutableStateFlow(listOf(streamMock1, modifiedStreamMock, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, modifiedStreamUi, streamUi3)
        Assert.assertEquals(expected, actual)

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
                    view =  ImmutableView(viewMock),
                    isEnabled = false
                )
            ),
            streamUi3
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayName_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update display name
        displayNameFlow.value = "newDisplayName"
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(username = "newDisplayName"),
            streamUi2.copy(username = "newDisplayName"),
            streamUi3.copy(username = "newDisplayName")
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun updateUserDisplayImage_mapToStreamsUi_mappedStreamUiUpdated() = runTest {
        val streams = MutableStateFlow(listOf(streamMock1, streamMock2, streamMock3))
        val result = streams.mapToStreamsUi(displayNameFlow, displayImageFlow)
        val actual = result.first()
        val expected = listOf(streamUi1, streamUi2, streamUi3)
        Assert.assertEquals(expected, actual)

        // Update display name
        val newUriMock = mockk<Uri>()
        displayImageFlow.value = newUriMock
        val newActual = result.first()
        val newExpected = listOf(
            streamUi1.copy(avatar = ImmutableUri(newUriMock)),
            streamUi2.copy(avatar = ImmutableUri(newUriMock)),
            streamUi3.copy(avatar = ImmutableUri(newUriMock))
        )
        Assert.assertEquals(newExpected, newActual)
    }

    @Test
    fun emptyList_hasAtLeastAVideoEnabled_false() = runTest {
        val flow = flowOf(listOf<StreamUi>())
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun participantHasVideoEnabled_hasAtLeastAVideoEnabled_true() = runTest {
        val flow = flowOf(listOf(StreamUi(id = "streamId", username = "username", video = VideoUi(id = "videoId", isEnabled = true))))
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun participantHasVideoDisabled_hasAtLeastAVideoEnabled_false() = runTest {
        val flow = flowOf(listOf(StreamUi(id = "streamId", username = "username", video = VideoUi(id = "videoId", isEnabled = false))))
        val result = flow.hasAtLeastAVideoEnabled()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

    @Test
    fun meHasOneStream_doIHaveStreams_true() = runTest {
        mockkObject(ParticipantMapper)
        val flow = flowOf(callMock)
        with(ParticipantMapper) {
            every { flow.toMe() } returns flowOf(participantMeMock)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf(mockk()))
        val result = flow.doIHaveStreams()
        val actual = result.first()
        Assert.assertEquals(true, actual)
    }

    @Test
    fun meHasNoStreamS_doIHaveStreams_false() = runTest {
        mockkObject(ParticipantMapper)
        val flow = flowOf(callMock)
        with(ParticipantMapper) {
            every { flow.toMe() } returns flowOf(participantMeMock)
        }
        every { participantMeMock.streams } returns MutableStateFlow(listOf())
        val result = flow.doIHaveStreams()
        val actual = result.first()
        Assert.assertEquals(false, actual)
    }

}