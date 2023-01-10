package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.call.compose.ringing.viewmodel.RingingViewModel
import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class RingingViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: RingingViewModel

    @Before
    fun setUp() {
        viewModel = spyk(RingingViewModel { Configuration.Success(phoneBoxMock, chatBoxMock, usersDescriptionMock) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
    }

    @Test
    fun testCallAnswer() = runTest {
        advanceUntilIdle()
        viewModel.answer()
        verify { callMock.connect() }
    }

    @Test
    fun testCallDecline() = runTest  {
        advanceUntilIdle()
        viewModel.decline()
        verify { callMock.end() }
    }

//    @Test
//    fun asds() {
//        viewModel.getMyStream()
//    }
}