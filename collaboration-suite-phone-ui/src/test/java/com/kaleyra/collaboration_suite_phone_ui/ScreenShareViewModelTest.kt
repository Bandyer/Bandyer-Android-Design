package com.kaleyra.collaboration_suite_phone_ui

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Inputs
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.model.ScreenShareTargetUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import io.mockk.*
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

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>()

    private val inputsMock = mockk<Inputs>()

    private val videoDeviceMock = mockk<Input.Video.Screen>(relaxed = true)

    private val videoAppMock = mockk<Input.Video.Application>(relaxed = true)

    private val context = mockk<FragmentActivity>()


    @Before
    fun setUp() {
        viewModel = spyk(ScreenShareViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk()) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.inputs } returns inputsMock
    }

    @Test
    fun testShareDeviceScreen() = runTest {
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareScreen(context, ScreenShareTargetUi.Device)
        advanceUntilIdle()
        verify { videoDeviceMock.tryEnable() }
    }

    @Test
    fun testShareAppScreen() = runTest {
        coEvery { inputsMock.request(context, Inputs.Type.Application) } returns Inputs.RequestResult.Success(videoAppMock)
        advanceUntilIdle()
        viewModel.shareScreen(context, ScreenShareTargetUi.Application)
        advanceUntilIdle()
        verify { videoAppMock.tryEnable() }
    }
}