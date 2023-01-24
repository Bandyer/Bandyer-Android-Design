package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_phone_ui.Mocks.callMock
import com.kaleyra.collaboration_suite_phone_ui.Mocks.phoneBoxMock
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScreenShareViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ScreenShareViewModel

    private val inputsMock = mockk<Inputs>()

    private val videoDeviceMock = mockk<Input.Video.Screen>(relaxed = true)

    private val videoAppMock = mockk<Input.Video.Application>(relaxed = true)

    @Before
    fun setUp() {
        viewModel = spyk(ScreenShareViewModel { Configuration.Success(Mocks.phoneBoxMock, Mocks.chatBoxMock, Mocks.usersDescriptionMock) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.inputs } returns inputsMock
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(videoDeviceMock, videoAppMock))
    }

    @Test
    fun testShareDeviceScreen() = runTest {
        advanceUntilIdle()
        viewModel.shareScreen(ScreenShareTargetUi.Device)
        verify { videoDeviceMock.tryEnable() }
    }

    @Test
    fun testShareAppScreen() = runTest {
        advanceUntilIdle()
        viewModel.shareScreen(ScreenShareTargetUi.Application)
        verify { videoAppMock.tryEnable() }
    }
}