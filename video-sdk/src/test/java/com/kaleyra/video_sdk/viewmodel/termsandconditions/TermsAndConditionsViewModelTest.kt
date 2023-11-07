package com.kaleyra.video_sdk.viewmodel.termsandconditions

import com.kaleyra.video.State
import com.kaleyra.video_common_ui.ConferenceUI
import com.kaleyra.video_common_ui.CollaborationViewModel.Configuration
import com.kaleyra.video_common_ui.ConversationUI
import com.kaleyra.video_sdk.MainDispatcherRule
import com.kaleyra.video_sdk.termsandconditions.viewmodel.TermsAndConditionsViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TermsAndConditionsViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: TermsAndConditionsViewModel

    private val conferenceMock = mockk<ConferenceUI>(relaxed = true)

    private val conversationMock = mockk<ConversationUI>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = TermsAndConditionsViewModel { Configuration.Success(conferenceMock, conversationMock, mockk(relaxed = true), MutableStateFlow(mockk())) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testTermsAndConditionsUiState_isConnectedUpdated() = runTest {
        every { conferenceMock.state } returns MutableStateFlow(State.Connected)
        every { conversationMock.state } returns MutableStateFlow(State.Connected)
        val actual = viewModel.uiState.first()
        assertEquals(false, actual.isConnected)
        advanceUntilIdle()
        val new = viewModel.uiState.first()
        assertEquals(true, new.isConnected)
    }

    @Test
    fun testDecline() = runTest {
        viewModel.decline()
        val actual = viewModel.uiState.first()
        assertEquals(true, actual.isDeclined)
    }
}