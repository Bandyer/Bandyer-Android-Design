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

import androidx.lifecycle.LifecycleService
import com.kaleyra.video_common_ui.proximity.AudioProximityDelegate
import com.kaleyra.video_common_ui.proximity.CallProximityDelegate
import com.kaleyra.video_common_ui.proximity.CameraProximityDelegate
import com.kaleyra.video_common_ui.proximity.WakeLockProximityDelegate
import com.kaleyra.video_common_ui.utils.CallExtensions
import com.kaleyra.video_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

internal class CallProximityDelegateTest {

    private var proximityDelegate: CallProximityDelegate<*>? = null

    private val serviceMock = mockk<LifecycleService>(relaxed = true)

    private val proximitySensorMock = mockk<ProximitySensor>(relaxed = true)

    private val wakeLockProximityDelegateMock = mockk<WakeLockProximityDelegate>(relaxed = true)

    private val cameraProximityDelegateMock = mockk<CameraProximityDelegate>(relaxed = true)

    private val audioProximityDelegateMock = mockk<AudioProximityDelegate>(relaxed = true)

    private val callMock = mockk<CallUI>()

    private var isProximityDisabled = false

    private var isWindowDisabled: Boolean? = null

    @Before
    fun setUp() {
        mockkObject(ProximitySensor)
        mockkObject(CallExtensions)
        every { ProximitySensor.bind(any<LifecycleService>(), any()) } returns proximitySensorMock
        with(CallExtensions) {
            every { isIncoming(any(), any()) } returns false
        }
        proximityDelegate = spyk(
            CallProximityDelegate(
                lifecycleContext = serviceMock,
                call = callMock,
                disableProximity = { isProximityDisabled },
                disableWindowTouch = { isWindowDisabled = it },
                wakeLockProximityDelegate = wakeLockProximityDelegateMock,
                cameraProximityDelegate = cameraProximityDelegateMock,
                audioProximityDelegate = audioProximityDelegateMock,
            )
        )
    }

    @After
    fun tearDown() {
        isWindowDisabled = null
        isProximityDisabled = false
    }

    @Test
    fun testBind() {
        proximityDelegate!!.bind()
        verify(exactly = 1) { ProximitySensor.bind(serviceMock, proximityDelegate!!) }
        verify(exactly = 1) { wakeLockProximityDelegateMock.bind() }
    }

    @Test
    fun testDestroy() {
        proximityDelegate!!.bind()
        proximityDelegate!!.destroy()
        verify(exactly = 1) { proximitySensorMock.destroy() }
        verify(exactly = 1) { wakeLockProximityDelegateMock.destroy() }
    }

    @Test
    fun `test proximity on when the screen is not turned off`() {
        isProximityDisabled = false
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns false
        proximityDelegate?.onProximitySensorChanged(true)
        assertEquals(null, isWindowDisabled)
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test proximity on when the screen is turned off, the camera is forced to be disabled`() {
        isProximityDisabled = false
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns true
        proximityDelegate?.onProximitySensorChanged(true)
        assertEquals(true, isWindowDisabled)
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera(true) }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test call proximity activity disable proximity true`() {
        isProximityDisabled = true
        proximityDelegate?.onProximitySensorChanged(true)
        assertEquals(null, isWindowDisabled)
        verify(exactly = 0) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 0) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test proximity off`() {
        proximityDelegate?.onProximitySensorChanged(false)
        assertEquals(false, isWindowDisabled)
        verify(exactly = 1) { wakeLockProximityDelegateMock.restoreScreenOn() }
        verify(exactly = 1) { cameraProximityDelegateMock.restoreCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.tryRestoreToLoudspeaker() }
    }
}