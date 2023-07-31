package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.chatbox.ChatBox
import com.kaleyra.collaboration_suite.phonebox.PhoneBox
import com.kaleyra.collaboration_suite_core_ui.ChatBoxUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
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

    private val phoneBoxMock = mockk<PhoneBoxUI>(relaxed = true)

    private val chatBoxMock = mockk<ChatBoxUI>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = TermsAndConditionsViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, mockk(relaxed = true), mockk(relaxed = true), mockk()) }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testTermsAndConditionsUiState_isConnectedUpdated() = runTest {
        every { phoneBoxMock.state } returns MutableStateFlow(PhoneBox.State.Connected)
        every { chatBoxMock.state } returns MutableStateFlow(ChatBox.State.Connected)
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