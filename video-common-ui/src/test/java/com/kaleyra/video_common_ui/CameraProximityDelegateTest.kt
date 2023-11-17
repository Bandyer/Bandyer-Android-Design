/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.video_common_ui

import com.kaleyra.video.conference.Input
import com.kaleyra.video_common_ui.proximity.CameraProximityDelegate
import com.kaleyra.video_common_ui.proximity.CameraProximityDelegateImpl
import com.kaleyra.video_common_ui.utils.CallExtensions
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class CameraProximityDelegateTest {

    private val callMock = mockk<CallUI>()

    private val cameraMock = mockk<Input>(relaxed = true)

    private val cameraProximityDelegate: CameraProximityDelegate = CameraProximityDelegateImpl(callMock)

    @Before
    fun setUp() {
        mockkObject(CallExtensions)
        with(CallExtensions) {
            every { callMock.getMyInternalCamera() } returns cameraMock
            every { callMock.isMyInternalCameraUsingFrontLens() } returns false
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `disable my camera if it is using front lens`() {
        with(CallExtensions) {
            every { callMock.isMyInternalCameraEnabled() } returns true
            every { callMock.isMyInternalCameraUsingFrontLens() } returns true
        }
        cameraProximityDelegate.tryDisableCamera()
        verify(exactly = 1) { cameraMock.tryDisable() }
    }

    @Test
    fun `disable my camera if force disable camera flag is true`() {
        with(CallExtensions) {
            every { callMock.isMyInternalCameraEnabled() } returns true
            every { callMock.isMyInternalCameraUsingFrontLens() } returns false
        }
        cameraProximityDelegate.tryDisableCamera(forceDisableCamera = true)
        verify(exactly = 1) { cameraMock.tryDisable() }
    }

    @Test
    fun `re-enable my camera if was previously enabled on disable`() {
        with(CallExtensions) {
            every { callMock.isMyInternalCameraEnabled() } returns true
        }
        cameraProximityDelegate.tryDisableCamera()
        cameraProximityDelegate.restoreCamera()
        verify(exactly = 1) { cameraMock.tryEnable() }
    }

    @Test
    fun `do not re-enable my camera if was previously disabled on disable`() {
        with(CallExtensions) {
            every { callMock.isMyInternalCameraEnabled() } returns false
        }
        cameraProximityDelegate.tryDisableCamera()
        cameraProximityDelegate.restoreCamera()
        verify(exactly = 0) { cameraMock.tryEnable() }
    }
}