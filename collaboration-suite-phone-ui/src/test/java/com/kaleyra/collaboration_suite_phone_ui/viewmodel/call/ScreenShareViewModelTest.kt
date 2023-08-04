package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import androidx.fragment.app.FragmentActivity
import com.kaleyra.collaboration_suite.phonebox.*
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.compose.screenshare.viewmodel.ScreenShareViewModel
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
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

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val screenShareStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val context = mockk<FragmentActivity>()

    @Before
    fun setUp() {
        viewModel = spyk(ScreenShareViewModel { Configuration.Success(phoneBoxMock, mockk(),  mockk(relaxed = true), mockk(relaxed = true)) })
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        with(callMock) {
            every { inputs } returns inputsMock
            every { participants } returns MutableStateFlow(mockk {
                every { me } returns meMock
            })
        }
        every { meMock.streams } returns MutableStateFlow(listOf(screenShareStreamMock))
        with(screenShareStreamMock) {
            every { id } returns ScreenShareViewModel.SCREEN_SHARE_STREAM_ID
            every { video } returns MutableStateFlow(null)
        }
    }

    @Test
    fun testShareDeviceScreen() = runTest {
        val videoDeviceMock = mockk<Input.Video.Screen.My>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareDeviceScreen(context)
        advanceUntilIdle()
        verify(exactly = 1) { videoDeviceMock.tryEnable() }
        verify(exactly = 1) { screenShareStreamMock.open() }
        assertEquals(videoDeviceMock, screenShareStreamMock.video.first())
    }

    @Test
    fun testShareApplicationScreen() = runTest {
        val videoAppMock = mockk<Input.Video.Application>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Application) } returns Inputs.RequestResult.Success(videoAppMock)
        advanceUntilIdle()
        viewModel.shareApplicationScreen(context)
        advanceUntilIdle()
        verify(exactly = 1) { videoAppMock.tryEnable() }
        verify(exactly = 1) { screenShareStreamMock.open() }
        assertEquals(videoAppMock, screenShareStreamMock.video.first())
    }

    @Test
    fun screenShareStreamDoesNotExists_shareApplicationScreen_streamIsAdded() = runTest {
        every { meMock.streams } returns MutableStateFlow(listOf())
        val videoDeviceMock = mockk<Input.Video.Application>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Application) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareApplicationScreen(context)
        advanceUntilIdle()
        verify(exactly = 1) { meMock.addStream(ScreenShareViewModel.SCREEN_SHARE_STREAM_ID) }
    }

    @Test
    fun screenShareStreamDoesNotExists_shareDeviceScreen_streamIsAdded() = runTest {
        every { meMock.streams } returns MutableStateFlow(listOf())
        val videoDeviceMock = mockk<Input.Video.Screen.My>(relaxed = true)
        coEvery { inputsMock.request(context, Inputs.Type.Screen) } returns Inputs.RequestResult.Success(videoDeviceMock)
        advanceUntilIdle()
        viewModel.shareDeviceScreen(context)
        advanceUntilIdle()
        verify(exactly = 1) { meMock.addStream(ScreenShareViewModel.SCREEN_SHARE_STREAM_ID) }
    }
}