package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import android.net.Uri
import com.kaleyra.collaboration_suite.Participant
import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite.sharedfolder.SharedFolder
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel.Configuration
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.UriExtensions.getFileSize
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.common.avatar.model.ImmutableUri
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.filepick.FilePickProvider
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.model.SharedFileUi
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.viewmodel.FileShareViewModel
import com.kaleyra.collaboration_suite_phone_ui.call.fileshare.viewmodel.FileShareViewModel.Companion.MaxFileUploadBytes
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.common.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.common.immutablecollections.ImmutableList
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FileShareViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: FileShareViewModel

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val senderMock = mockk<Participant>()

    private val meMock = mockk<CallParticipant.Me>()

    private val sharedFolderMock = mockk<SharedFolder>(relaxed = true)

    private val sharedFileMock1 = mockk<SharedFile>(relaxed = true)

    private val sharedFileMock2 = mockk<SharedFile>(relaxed = true)

    private val sharedFileMock3 = mockk<SharedFile>(relaxed = true)

    private val sharedFileUi1 = SharedFileUi(id = "sharedFileId", name = "sharedFileName", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 1234L, state = SharedFileUi.State.Available, isMine = false)

    private val sharedFileUi2 = SharedFileUi(id = "sharedFileId2", name = "sharedFileName2", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 2345L, state = SharedFileUi.State.Available, isMine = false)

    private val sharedFileUi3 = SharedFileUi(id = "sharedFileId3", name = "sharedFileName3", uri = ImmutableUri(uriMock), size = 1024L, sender = "displayName", time = 3456L, state = SharedFileUi.State.Available, isMine = false)

    @Before
    fun setUp() {
        mockkObject(UriExtensions)
        mockkObject(ContactDetailsManager)
        every { any<Uri>().getFileSize() } returns 0
        mockkObject(CallUserMessagesProvider)
        viewModel = FileShareViewModel(
            configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true), MutableStateFlow(mockk())) },
            filePickProvider = object : FilePickProvider {
                override val fileUri: Flow<Uri> = MutableStateFlow(uriMock)
            }
        )
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { sharedFolder } returns sharedFolderMock
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns meMock
            })
        }
        with(meMock) {
            every { userId } returns "myUserId"
            every { combinedDisplayName } returns MutableStateFlow("myDisplayName")
        }
        with(senderMock) {
            every { userId } returns "userId"
            every { combinedDisplayName } returns MutableStateFlow("displayName")
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
            every { size } returns 1024L
            every { creationTime } returns 2345L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
        with(sharedFileMock3) {
            every { id } returns "sharedFileId3"
            every { name } returns "sharedFileName3"
            every { size } returns 1024L
            every { creationTime } returns 3456L
            every { uri } returns uriMock
            every { state } returns MutableStateFlow(SharedFile.State.Available)
            every { sender } returns senderMock
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testFileShareUiState_sharedFilesUpdated() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock1))
        val current = viewModel.uiState.first().sharedFiles
        assertEquals(ImmutableList(listOf<SharedFileUi>()), current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().sharedFiles
        val expected = ImmutableList(listOf(sharedFileUi1))
        assertEquals(expected, new)
    }

    @Test
    fun testFileShareUiState_sharedFilesAreOrderedByUpdatedTime() = runTest {
        every { sharedFolderMock.files } returns MutableStateFlow(setOf(sharedFileMock2, sharedFileMock1, sharedFileMock3))
        advanceUntilIdle()
        val new = viewModel.uiState.first().sharedFiles
        val expected = ImmutableList(listOf(sharedFileUi3, sharedFileUi2, sharedFileUi1))
        assertEquals(expected, new)
    }

    @Test
    fun testFileShareUiState_showFileSizeLimitUpdated() = runTest {
        every { any<Uri>().getFileSize() } returns MaxFileUploadBytes + 1L
        advanceUntilIdle()
        val new = viewModel.uiState.first().showFileSizeLimit
        assertEquals(true, new)
    }

    @Test
    fun testUploadOnFilePick() = runTest {
        advanceUntilIdle()
        verify { sharedFolderMock.upload(uriMock) }
        verify { callMock.show() }
    }

    @Test
    fun testUpload() = runTest {
        val uriMock = mockk<Uri>()
        advanceUntilIdle()
        viewModel.upload(uriMock)
        verify { sharedFolderMock.upload(uriMock) }
    }

    @Test
    fun testDownload() = runTest {
        advanceUntilIdle()
        viewModel.download("id")
        verify { sharedFolderMock.download("id") }
    }

    @Test
    fun testCancel() = runTest {
        advanceUntilIdle()
        viewModel.cancel("id")
        verify { sharedFolderMock.cancel("id") }
    }

    @Test
    fun testDismissUploadLimit() = runTest {
        viewModel.dismissUploadLimit()
        assertEquals(false,  viewModel.uiState.first().showFileSizeLimit)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }
}