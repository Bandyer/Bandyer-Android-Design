package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Input
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCameraDelegateTest {


    private val defaultCameraDelegate = object : DefaultCameraDelegate { }

    private val callMock = mockk<CallUI>(relaxed = true)

    private val videoMock = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val rearLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns true
    }

    private val frontLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns false
    }

    @Before
    fun setUp() {
        every { videoMock.lenses } returns listOf(frontLens, rearLens)
    }

    @Test
    fun backLens_setBackCameraAsDefault_lensIsNotChanged() = runTest {
        every { videoMock.currentLens } returns MutableStateFlow(rearLens)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))

        defaultCameraDelegate.setBackCameraAsDefault(callMock, this)
        advanceUntilIdle()

        verify(exactly = 0) { videoMock.setLens(rearLens) }
    }

    @Test
    fun frontLens_setBackCameraAsDefault_changeToBackLens() = runTest {
        every { videoMock.currentLens } returns MutableStateFlow(frontLens)
        every { callMock.inputs.availableInputs } returns MutableStateFlow(setOf(videoMock))

        defaultCameraDelegate.setBackCameraAsDefault(callMock, this)
        advanceUntilIdle()

        verify(exactly = 1) { videoMock.setLens(rearLens) }
    }
}