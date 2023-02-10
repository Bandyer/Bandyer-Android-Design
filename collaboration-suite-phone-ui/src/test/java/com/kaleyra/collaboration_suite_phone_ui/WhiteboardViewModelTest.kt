package com.kaleyra.collaboration_suite_phone_ui

import android.net.Uri
import android.webkit.WebView
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite.phonebox.Whiteboard
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.whiteboard.viewmodel.WhiteboardViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// TODO check if how I can test this
@OptIn(ExperimentalCoroutinesApi::class)
class WhiteboardViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: WhiteboardViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val whiteboardMock = mockk<Whiteboard>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = spyk(WhiteboardViewModel(configure = { Configuration.Success(phoneBoxMock, mockk(), mockk()) }, context = mockk(relaxed = true)))
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.whiteboard } returns whiteboardMock
//        mockkConstructor(WebView::class)
//        every { anyConstructed<WebView>().settings } returns mockk(relaxed = true)
    }

    @Test
    fun testWhiteboardViewSetUp() = runTest {
        advanceUntilIdle()
        val whiteboardView = viewModel.uiState.first()
        verify { whiteboardMock.load() }
        assertNotEquals(null, whiteboardView)
        assertEquals(whiteboardView, whiteboardMock.view.value)
    }

    @Test
    fun testWhiteboardUnloadedOnCallEnded() = runTest {
        every { callMock.state } returns MutableStateFlow(Call.State.Disconnected.Ended)
        advanceUntilIdle()
        verify { whiteboardMock.unload() }
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
        viewModel.onReloadClick()
        verify { whiteboardMock.load() }
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
    fun textUploadMediaFile() = runTest {
        val uriMock = mockk<Uri>()
        advanceUntilIdle()
        viewModel.uploadMediaFile(uriMock)
        verify { whiteboardMock.addMediaFile(uriMock) }
    }
}