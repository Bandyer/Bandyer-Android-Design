package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.recording.model.RecordingTypeUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.model.RingingUiState
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel.RingingViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class RingingViewModelTest: PreCallViewModelTest<RingingViewModel, RingingUiState>() {

    private val recordingMock = mockk<Call.Recording>()

    @Before
    override fun setUp() {
        super.setUp()
        every { callMock.extras.recording } returns recordingMock
        every { recordingMock.type } returns Call.Recording.Type.OnConnect
        viewModel = spyk(RingingViewModel { Configuration.Success(phoneBoxMock, mockk(), MutableStateFlow(companyNameMock), MutableStateFlow(themeMock), mockk()) })
    }

    @After
    override fun tearDown() {
        super.tearDown()
    }

        @Test
    fun testPreCallUiState_recordingUpdated() = runTest {
        val current = viewModel.uiState.first().recording
        assertEquals(null, current)
        advanceUntilIdle()
        val new = viewModel.uiState.first().recording
        val expected = RecordingTypeUi.OnConnect
        assertEquals(expected, new)
    }

    @Test
    fun testCallAnswer() = runTest {
        advanceUntilIdle()
        viewModel.accept()
        val actual = viewModel.uiState.first().answered
        assertEquals(true, actual)
        verify(exactly = 1) { callMock.connect() }
    }

    @Test
    fun testCallDecline() = runTest {
        advanceUntilIdle()
        viewModel.decline()
        val actual = viewModel.uiState.first().answered
        assertEquals(true, actual)
        verify(exactly = 1) { callMock.end() }
    }
}