package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Input
import com.kaleyra.video.conference.Inputs
import com.kaleyra.video_common_ui.utils.InputsExtensions.useBackCamera
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
class InputsExtensionsTest {

    private val inputs = mockk<Inputs>(relaxed = true)

    private val videoInput = mockk<Input.Video.Camera.Internal>(relaxed = true)

    private val rearLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns true
    }

    private val frontLens = mockk<Input.Video.Camera.Internal.Lens> {
        every { isRear } returns false
    }

    @Before
    fun setUp() {
        every { videoInput.lenses } returns listOf(frontLens, rearLens)
    }

    @Test
    fun internalCameraBackLens_useBackCamera_lensIsNotChanged() = runTest {
        every { videoInput.currentLens } returns MutableStateFlow(rearLens)
        every { inputs.availableInputs } returns MutableStateFlow(setOf(videoInput))

        inputs.useBackCamera(this)
        advanceUntilIdle()

        verify(exactly = 0) { videoInput.setLens(rearLens) }
    }

    @Test
    fun internalCameraFrontLens_useBackCamera_changeToBackLens() = runTest {
        every { videoInput.currentLens } returns MutableStateFlow(frontLens)
        every { inputs.availableInputs } returns MutableStateFlow(setOf(videoInput))

        inputs.useBackCamera(this)
        advanceUntilIdle()

        verify(exactly = 1) { videoInput.setLens(rearLens) }
    }
}