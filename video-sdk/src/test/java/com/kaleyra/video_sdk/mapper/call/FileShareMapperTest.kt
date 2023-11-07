package com.kaleyra.video_sdk.mapper.call

import android.net.Uri
import com.kaleyra.video.Participant
import com.kaleyra.video.conference.CallParticipant
import com.kaleyra.video.conference.CallParticipants
import com.kaleyra.video.sharedfolder.SharedFile
import com.kaleyra.video.sharedfolder.SharedFolder
import com.kaleyra.video_common_ui.CallUI
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri
import com.kaleyra.video_sdk.call.fileshare.model.SharedFileUi
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.mapToSharedFileUi
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.mapToSharedFileUiState
import com.kaleyra.video_sdk.call.mapper.FileShareMapper.toSharedFilesUi
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileShareMapperTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private val callMock = mockk<CallUI>()

    private val uriMock = mockk<Uri>()

    private val senderMock = mockk<Participant>()

    private val senderMock2 = mockk<Participant>()

    private val meMock = mockk<CallParticipant.Me>()

    private val sharedFolderMock = mockk<SharedFolder>()

    private val participantsMock = mockk<CallParticipants>()

    private val sharedFileMock1 = mockk<SharedFile>()

    private val sharedFileMock2 = mockk<SharedFile>()

    private val sharedFileUi1 = SharedFileUi(
        id = "sharedFileId",
        name = "sharedFileName",
        uri = ImmutableUri(uriMock),
        size = 1024L,
        sender = "displayName",
        time = 1234L,
        state = SharedFileUi.State.Available,
        isMine = false
    )

    private val sharedFileUi2 = SharedFileUi(
        id = "sharedFileId2",
        name = "sharedFileName2",
        uri = ImmutableUri(uriMock),
        size = 2048L,
        sender = "displayName2",
        time = 2345L,
        state = SharedFileUi.State.Pending,
        isMine = false
    )

    @Before
    fun setUp() {
        mockkObject(ContactDetailsManager)
        every { participantsMock.me } returns meMock
        with(meMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
        }
        with(callMock) {
            every { sharedFolder } returns sharedFolderMock
            every { participants } returns MutableStateFlow(participantsMock)
        }
        with(senderMock) {
            every { userId } returns "userId"
            every { combinedDisplayName } returns MutableStateFlow("displayName")
        }
        with(senderMock2) {
            every { userId } returns "userId2"
            every { combinedDisplayName } returns MutableStateFlow("displayName2")
        }
        with(sharedFileMock1) {
            every { id } returns "sharedFileId"
            every { name } returns "sharedFileName"
            every { size } returns 1024L
            every { creationTime } returns 1234L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
        with(sharedFileMock2) {
            every { id } returns "sharedFileId2"
            every { name } returns "sharedFileName2"
            every { size } returns 2048L
            every { creationTime } returns 2345L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Pending)
            every { sender } returns senderMock2
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun emptySet_toSharedFilesUi_emptyMappedSet() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf())
        val result = flowOf(callMock).toSharedFilesUi()
        val actual = result.first()
        assertEquals(setOf<SharedFileUi>(), actual)
    }

    @Test
    fun notEmptySet_toSharedFilesUi_mappedSet() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1, sharedFileMock2))
        val result = flowOf(callMock).toSharedFilesUi()
        val actual = result.first()
        val expected = setOf(sharedFileUi1, sharedFileUi2)
        assertEquals(expected, actual)
    }

    @Test
    fun uploadCancelled_toSharedFilesUi_uploadRemovedFromFilesSet() = runTest {
        val state = MutableStateFlow<SharedFile.State>(SharedFile.State.Pending)
        every { sharedFileMock2.state } returns state
        every { sharedFileMock2.sender } returns meMock
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1, sharedFileMock2))

        val flow = flowOf(callMock).toSharedFilesUi()
        val actual = flow.first()
        val expected = setOf(sharedFileUi1, sharedFileUi2.copy(sender = "myDisplayName", isMine = true))
        assertEquals(expected, actual)

        state.value = SharedFile.State.Cancelled
        val newActual = flow.drop(1).first()
        val newExpected = setOf(sharedFileUi1)
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateSenderDisplayName_toSharedFilesUi_setContainsSharedFileUiWithUpdatedDisplayName() = runTest {
        val displayName = MutableStateFlow("oldDisplayName")
        every { senderMock2.combinedDisplayName } returns displayName
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1, sharedFileMock2))

        val flow = flowOf(callMock).toSharedFilesUi()
        val actual = flow.first()
        val expected = setOf(sharedFileUi1, sharedFileUi2.copy(sender = "oldDisplayName"))
        assertEquals(expected, actual)

        displayName.value = "newDisplayName"
        val newActual = flow.drop(1).first()
        val newExpected = setOf(sharedFileUi1, sharedFileUi2.copy(sender = "newDisplayName"))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun updateSharedFileState_toSharedFilesUi_setContainsSharedFileUiWithUpdatedState() = runTest {
        val state = MutableStateFlow<SharedFile.State>(SharedFile.State.Pending)
        every { sharedFileMock2.state } returns state
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1, sharedFileMock2))

        val flow = flowOf(callMock).toSharedFilesUi()
        val actual = flow.first()
        val expected = setOf(sharedFileUi1, sharedFileUi2.copy(state = SharedFileUi.State.Pending))
        assertEquals(expected, actual)

        state.value = SharedFile.State.Error(Throwable())
        val newActual = flow.drop(1).first()
        val newExpected = setOf(sharedFileUi1, sharedFileUi2.copy(state = SharedFileUi.State.Error))
        assertEquals(newExpected, newActual)
    }

    @Test
    fun sharedFile_mapToShareFileUi_sharedFileUi() = runTest {
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1
        assertEquals(expected, actual)
    }

    @Test
    fun imTheSender_mapToShareFileUi_isMineTrue() = runTest {
        every { senderMock.userId } returns "myUserId"
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(isMine = true)
        assertEquals(expected, actual)
    }

    @Test
    fun senderWithNoDisplayName_mapToShareFileUi_mappedSenderIsUserId() = runTest {
        with(senderMock) {
            every { userId } returns "userId"
            every { combinedDisplayName } returns MutableStateFlow(null)
        }
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(sender = "userId")
        assertEquals(expected, actual)
    }

    @Test
    fun displayNameUpdated_mapToShareFileUi_updatedDisplayNameIsMapped() = runTest {
        val flow = MutableStateFlow("displayName")
        every { senderMock.combinedDisplayName } returns flow

        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(sender = "displayName")

        assertEquals(expected, actual)

        flow.value = "newDisplayName"
        val newActual = result.first()
        val newExpected = sharedFileUi1.copy(sender = "newDisplayName")

        assertEquals(newExpected, newActual)
    }

    @Test
    fun fileStateUpdated_mapToShareFileUi_mappedFileStateIsUpdated() = runTest {
        val flow = MutableStateFlow<SharedFile.State>(SharedFile.State.Available)
        every { sharedFileMock1.state } returns flow

        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(state = SharedFileUi.State.Available)

        assertEquals(expected, actual)

        flow.value = SharedFile.State.Pending
        val newActual = result.first()
        val newExpected = sharedFileUi1.copy(state = SharedFileUi.State.Pending)

        assertEquals(newExpected, newActual)
    }

    @Test
    fun fileStateAvailable_mapToShareFileUi_mappedFileStateIsAvailable() = runTest {
        every { sharedFileMock1.state } returns MutableStateFlow(SharedFile.State.Available)
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(state = SharedFileUi.State.Available)
        assertEquals(expected, actual)
    }

    @Test
    fun fileStatePending_mapToShareFileUi_mappedFileStateIsPending() = runTest {
        every { sharedFileMock1.state } returns MutableStateFlow(SharedFile.State.Pending)
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(state = SharedFileUi.State.Pending)
        assertEquals(expected, actual)
    }

    @Test
    fun fileStateCancelled_mapToShareFileUi_mappedFileStateIsCancelled() = runTest {
        every { sharedFileMock1.state } returns MutableStateFlow(SharedFile.State.Cancelled)
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(state = SharedFileUi.State.Cancelled)
        assertEquals(expected, actual)
    }

    @Test
    fun fileStateSuccess_mapToShareFileUi_mappedFileStateIsSuccess() = runTest {
        val successUri = mockk<Uri>()
        every { sharedFileMock1.state } returns MutableStateFlow(SharedFile.State.Success("", successUri))
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(state = SharedFileUi.State.Success(ImmutableUri(successUri)))
        assertEquals(expected, actual)
    }

    @Test
    fun fileStateInProgress_mapToShareFileUi_mappedFileStateIsInProgress() = runTest {
        with(sharedFileMock1) {
            every { state } returns MutableStateFlow(SharedFile.State.InProgress(700L))
            every { size } returns 1000L
        }
        val result = sharedFileMock1.mapToSharedFileUi("myUserId")
        val actual = result.first()
        val expected = sharedFileUi1.copy(size = 1000L, state = SharedFileUi.State.InProgress(progress = .7f))
        assertEquals(expected, actual)
    }

    @Test
    fun sharedFileStateAvailable_mapToSharedFileUiState_stateAvailable() {
        val state = SharedFile.State.Available
        val result = state.mapToSharedFileUiState(0L)
        assertEquals(SharedFileUi.State.Available, result)
    }

    @Test
    fun sharedFileStateCancelled_mapToSharedFileUiState_stateCancelled() {
        val state = SharedFile.State.Cancelled
        val result = state.mapToSharedFileUiState(0L)
        assertEquals(SharedFileUi.State.Cancelled, result)
    }

    @Test
    fun sharedFileStatePending_mapToSharedFileUiState_statePending() {
        val state = SharedFile.State.Pending
        val result = state.mapToSharedFileUiState(0L)
        assertEquals(SharedFileUi.State.Pending, result)
    }

    @Test
    fun sharedFileStateError_mapToSharedFileUiState_stateError() {
        val state = SharedFile.State.Error(Throwable())
        val result = state.mapToSharedFileUiState(0L)
        assertEquals(SharedFileUi.State.Error, result)
    }

    @Test
    fun sharedFileStateSuccess_mapToSharedFileUiState_stateSuccess() {
        val uriMock = mockk<Uri>()
        val state = SharedFile.State.Success(id = "", uriMock)
        val result = state.mapToSharedFileUiState(0L)
        assertEquals(SharedFileUi.State.Success(ImmutableUri(uriMock)), result)
    }

    @Test
    fun sharedFileStateInProgress_mapToSharedFileUiState_stateInProgress() {
        val state = SharedFile.State.InProgress(400L)
        val result = state.mapToSharedFileUiState(800L)
        assertEquals(SharedFileUi.State.InProgress(.5f), result)
    }
}