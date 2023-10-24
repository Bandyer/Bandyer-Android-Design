package com.kaleyra.collaboration_suite_phone_ui.viewmodel.call

import com.kaleyra.collaboration_suite.conference.CallParticipant
import com.kaleyra.collaboration_suite.conference.CallParticipants
import com.kaleyra.collaboration_suite.conference.Effect
import com.kaleyra.collaboration_suite.conference.Effects
import com.kaleyra.collaboration_suite.conference.Input
import com.kaleyra.collaboration_suite.conference.Stream
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.CollaborationViewModel.Configuration
import com.kaleyra.collaboration_suite_core_ui.ConferenceUI
import com.kaleyra.collaboration_suite_core_ui.call.CameraStreamPublisher
import com.kaleyra.collaboration_suite_phone_ui.MainDispatcherRule
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.model.VirtualBackgroundUi
import com.kaleyra.collaboration_suite_phone_ui.call.virtualbackground.viewmodel.VirtualBackgroundViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
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
class VirtualBackgroundViewModelTest {

    @get:Rule
    var mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: VirtualBackgroundViewModel

    private val conferenceMock = mockk<ConferenceUI>()

    private val callMock = mockk<CallUI>(relaxed = true)

    private val participantsMock = mockk<CallParticipants>(relaxed = true)

    private val meMock = mockk<CallParticipant.Me>(relaxed = true)

    private val myStreamMock = mockk<Stream.Mutable>(relaxed = true)

    private val myVideoMock = mockk<Input.Video.My>(relaxed = true)

    private val effectsMock = mockk<Effects>(relaxed = true)

    @Before
    fun setUp() {
        viewModel =
            VirtualBackgroundViewModel { Configuration.Success(conferenceMock, mockk(), mockk(relaxed = true)) }
        every { conferenceMock.call } returns MutableStateFlow(callMock)
        every { callMock.participants } returns MutableStateFlow(participantsMock)
        every { participantsMock.me } returns meMock
        every { meMock.streams } returns MutableStateFlow(listOf(myStreamMock))
        with(myStreamMock) {
            every { id } returns CameraStreamPublisher.CAMERA_STREAM_ID
            every { video } returns MutableStateFlow(myVideoMock)
        }
        every { callMock.effects } returns effectsMock
        with(effectsMock) {
            every { preselected } returns MutableStateFlow(Effect.Video.Background.Image(id = "imageId", image = mockk()))
            every { available } returns MutableStateFlow(setOf(Effect.Video.Background.Blur(id = "blurId", factor = 1f)))
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testVirtualBackgroundUiState_currentBackgroundUpdated() = runTest {
        every { myVideoMock.currentEffect } returns MutableStateFlow(Effect.Video.Background.Blur(id = "blurId", factor = 1f))
        advanceUntilIdle()
        val actual = viewModel.uiState.first().currentBackground
        val expected = VirtualBackgroundUi.Blur("blurId")
        assertEquals(expected, actual)
    }

    @Test
    fun testVirtualBackgroundUiState_backgroundsUpdated() = runTest {
        advanceUntilIdle()
        val actual = viewModel.uiState.first().backgroundList.value
        assertEquals(listOf(VirtualBackgroundUi.None, VirtualBackgroundUi.Blur("blurId"), VirtualBackgroundUi.Image("imageId")), actual)
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
        viewModel.setEffect(VirtualBackgroundUi.Blur("blurId"))
        verify(exactly = 1) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Blur)
            })
        }
    }

    @Test
    fun testSetImageEffect() = runTest {
        advanceUntilIdle()
        viewModel.setEffect(VirtualBackgroundUi.Image("imageId"))
        verify(exactly = 1) {
            myVideoMock.tryApplyEffect(withArg {
                assert(it is Effect.Video.Background.Image)
            })
        }
    }
}