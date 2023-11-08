package com.kaleyra.video_common_ui

import android.content.Context
import com.kaleyra.video.conference.Call
import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.call.ScreenShareOverlayDelegate
import com.kaleyra.video_common_ui.overlay.AppViewOverlay
import com.kaleyra.video_common_ui.overlay.StatusBarOverlayView
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class ScreenShareOverlayDelegateTest {

    private val contextMock = mockk<Context>(relaxed = true)

    private val callMock = mockk<Call>()

    private val inputsMock = mockk<Inputs>()

    private val delegate = object : ScreenShareOverlayDelegate { }

    @Before
    fun setUp() {
        mockkConstructor(AppViewOverlay::class)
        mockkConstructor(StatusBarOverlayView::class)
        every { anyConstructed<AppViewOverlay>().show(any()) } returns Unit
        every { anyConstructed<AppViewOverlay>().hide() } returns Unit
        every { callMock.inputs } returns inputsMock
    }

    @Test
    fun enableDeviceScreenShare_overlayIsShown() = runTest(UnconfinedTestDispatcher()) {
        val screenMock = mockk<Input.Video.Screen.My>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.enabled } returns MutableStateFlow(true)
        delegate.syncScreenShareOverlay(contextMock, callMock, backgroundScope)
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().show(contextMock) }
    }

    @Test
    fun disableDeviceScreenShare_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val screenMock = mockk<Input.Video.Screen.My>()
        val enabled = MutableStateFlow(true)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(screenMock))
        every { screenMock.enabled } returns enabled
        delegate.syncScreenShareOverlay(contextMock, callMock, backgroundScope)
        enabled.value = false
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun enableApplicationScreenShare_overlayIsShown() = runTest(UnconfinedTestDispatcher()) {
        val applicationMock = mockk<Input.Video.Application>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.enabled } returns MutableStateFlow(true)
        delegate.syncScreenShareOverlay(contextMock, callMock, backgroundScope)
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().show(contextMock) }
    }

    @Test
    fun disableApplicationScreenShare_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val applicationMock = mockk<Input.Video.Application>()
        val enabled = MutableStateFlow(true)
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.enabled } returns enabled
        delegate.syncScreenShareOverlay(contextMock, callMock, backgroundScope)
        enabled.value = false
        verify(exactly = 1) {  anyConstructed<AppViewOverlay>().hide() }
    }

    @Test
    fun cancelScopeWithOverlayActive_overlayIsHidden() = runTest(UnconfinedTestDispatcher()) {
        val applicationMock = mockk<Input.Video.Application>()
        every { inputsMock.availableInputs } returns MutableStateFlow(setOf(applicationMock))
        every { applicationMock.enabled } returns MutableStateFlow(true)
        delegate.syncScreenShareOverlay(contextMock, callMock, backgroundScope)
        backgroundScope.cancel()
        verify(exactly = 1) { anyConstructed<AppViewOverlay>().hide() }
    }

}