package com.kaleyra.collaboration_suite_phone_ui.viewmodel.termsandconditions

import com.kaleyra.collaboration_suite.conversation.Conversation
import com.kaleyra.collaboration_suite.conference.Conference
import com.kaleyra.collaboration_suite_core_ui.ConversationUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.termsandconditions.viewmodel.TermsAndConditionsViewModel
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
        viewModel = TermsAndConditionsViewModel { Configuration.Success(conferenceMock, conversationMock, mockk(relaxed = true)) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testTermsAndConditionsUiState_isConnectedUpdated() = runTest {
        every { conferenceMock.state } returns MutableStateFlow(Conference.State.Connected)
        every { conversationMock.state } returns MutableStateFlow(Conversation.State.Connected)
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