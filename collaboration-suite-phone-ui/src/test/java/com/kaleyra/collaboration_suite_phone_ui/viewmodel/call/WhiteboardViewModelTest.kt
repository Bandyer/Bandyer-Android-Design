package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import android.net.Uri
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.sharedfolder.SharedFile
import com.kaleyra.collaboration_suite.whiteboard.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.model.MutedMessage
import com.kaleyra.collaboration_suite_phone_ui.call.compose.usermessages.provider.CallUserMessagesProvider
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.model.WhiteboardUploadUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WhiteboardViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WhiteboardViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val uriMock = mockk<Uri>()

    private val sharedFileMock = mockk<SharedFile>()

    private val whiteboardMock = mockk<Whiteboard>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(CallUserMessagesProvider)
        viewModel = spyk(WhiteboardViewModel(configure = { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true)) }, whiteboardView = mockk(relaxed = true)))
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.whiteboard } returns whiteboardMock
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Available)
        }
    }

    @Test
    fun testWhiteboardViewSetUp() = runTest {
        every { whiteboardMock.view } returns MutableStateFlow(null)
        advanceUntilIdle()
        val result = viewModel.uiState.first().whiteboardView
        verify(exactly = 1) { whiteboardMock.load() }
        assertNotEquals(null, result)
        assertEquals(result, whiteboardMock.view.value)
    }

    @Test
    fun testWhiteboardUnloadedOnCallEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        advanceUntilIdle()
        verify(exactly = 1) { whiteboardMock.unload() }
    }

    @Test
    fun testWhiteboardUiState_isLoadingUpdated() = runTest {
        assertEquals(false, viewModel.uiState.first().isLoading)
        every { whiteboardMock.state } returns MutableStateFlow(Whiteboard.State.Loading)
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.first().isLoading)
    }

    @Test
    fun testWhiteboardUiState_textUpdatedOnTextEditEvent() = runTest {
        every { whiteboardMock.events } returns MutableStateFlow(Whiteboard.Event.Text.Edit(oldText = "text", mockk()))
        advanceUntilIdle()
        assertEquals("text", viewModel.uiState.first().text)
    }

    @Test
    fun testWhiteboardUiState_textUpdatedOnTextAddEvent() = runTest {
        every { whiteboardMock.events } returns MutableStateFlow(Whiteboard.Event.Text.Add(mockk()))
        advanceUntilIdle()
        assertEquals("", viewModel.uiState.first().text)
    }

    @Test
    fun testOnReloadClick() = runTest {
        advanceUntilIdle()
        verify(exactly = 1) { whiteboardMock.load() }
        viewModel.onReloadClick()
        verify(exactly = 2) { whiteboardMock.load() }
    }

    @Test
    fun testOnTextDismissed() = runTest {
        every { whiteboardMock.events } returns MutableStateFlow(Whiteboard.Event.Text.Edit(oldText = "text", mockk()))
        advanceUntilIdle()
        assertEquals("text", viewModel.uiState.first().text)
        viewModel.onTextDismissed()
        assertEquals(null, viewModel.uiState.first().text)
    }

    @Test
    fun testOnTextConfirmed() = runTest {
        var onCompletionInvoked = false
        every { whiteboardMock.events } returns MutableStateFlow(Whiteboard.Event.Text.Edit(oldText = "oldText") {
            onCompletionInvoked = true
        })
        advanceUntilIdle()
        viewModel.onTextConfirmed("newText")
        assertEquals(null, viewModel.uiState.first().text)
        assertEquals(true, onCompletionInvoked)
    }

    @Test
    fun testUploadMediaFile_addMediaFileInvoked() = runTest {
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)
        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)
        verify(exactly = 1) { whiteboardMock.addMediaFile(uriMock) }
    }

    @Test
    fun testUploadMediaFile_whiteboardUploadStateUpdated() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.InProgress(500L))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceUntilIdle()
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Uploading(.5f)
        assertEquals(expected, actual)
    }

    @Test
    fun testUploadMediaFileError_whiteboardUploadStateResetAfter3000Ms() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Error(Throwable()))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceTimeBy(1000L)
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Error
        assertEquals(expected, actual)

        advanceTimeBy(3000L)
        val newActual = viewModel.uiState.first().upload
        assertEquals(null, newActual)
    }

    @Test
    fun testUploadMediaFileSuccess_whiteboardUploadStateResetAfter300Ms() = runTest {
        with(sharedFileMock) {
            every { size } returns 1000L
            every { state } returns MutableStateFlow(SharedFile.State.Success(id = "", uri = mockk()))
        }
        every { whiteboardMock.addMediaFile(uriMock) } returns Result.success(sharedFileMock)

        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)

        advanceTimeBy(100L)
        val actual = viewModel.uiState.first().upload
        val expected = WhiteboardUploadUi.Uploading(1f)
        assertEquals(expected, actual)

        advanceTimeBy(300L)
        val newActual = viewModel.uiState.first().upload
        assertEquals(null, newActual)
    }

    @Test
    fun testUserMessage() = runTest {
        every { CallUserMessagesProvider.userMessage } returns flowOf(MutedMessage("admin"))
        advanceUntilIdle()
        val actual = viewModel.userMessage.first()
        assert(actual is MutedMessage && actual.admin == "admin")
    }
}