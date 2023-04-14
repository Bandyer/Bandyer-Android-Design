package com.kaleyra.collaboration_suite_phone_ui

import com.kaleyra.collaboration_suite.phonebox.CallParticipant
import com.kaleyra.collaboration_suite.phonebox.CallParticipants
import com.kaleyra.collaboration_suite.phonebox.Effect
import com.kaleyra.collaboration_suite.phonebox.Effects
import com.kaleyra.collaboration_suite.phonebox.Input
import com.kaleyra.collaboration_suite.phonebox.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.Configuration
import com.kaleyra.collaboration_suite_core_ui.PhoneBoxUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.compose.virtualbackground.viewmodel.VirtualBackgroundViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VirtualBackgroundViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: VirtualBackgroundViewModel

    private val phoneBoxMock = mockk<PhoneBoxUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.My>(relaxed = true)

    private val effectsMock = mockk<Effects>(relaxed = true)

    @Before
    fun setUp() {
        viewModel =
            VirtualBackgroundViewModel { Configuration.Success(phoneBoxMock, mockk(), mockk()) }
        every { phoneBoxMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        with(myStreamMock) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideoMock)
        }
        every { callMock.effects } returns effectsMock
        with(effectsMock) {
            every { preselected } returns MutableStateFlow(Effect.Video.Background.Image(image = mockk()))
            every { available } returns MutableStateFlow(setOf(Effect.Video.None, Effect.Video.Background.Blur(factor = 1f)))
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testVirtualBackgroundUiState_currentBackgroundUpdated() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur(factor = 1f))
        advanceUntilIdle()
        val actual = viewModel.uiState.first().currentBackground
        val expected = VirtualBackgroundUi.Blur
        assertEquals(expected, actual)
    }

    @Test
    fun testVirtualBackgroundUiState_backgroundsUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().backgrounds.value
        assertEquals(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur), actual)
    }

    @Test
    fun testSetNoneEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.None)
        verify(exactly = 1) { myVideoMock.tryApplyEffect(Effect.Video.None) }
    }

    @Test
    fun testSetBlurEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.Blur)
        verify(exactly = 1) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Blur)
            })
        }
    }

    @Test
    fun testSetImageEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.Image)
        verify(exactly = 1) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Image)
            })
        }
    }
}