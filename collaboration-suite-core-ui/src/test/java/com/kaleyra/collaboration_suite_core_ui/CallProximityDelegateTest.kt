package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.LifecycleService
import com.kaleyra.collaboration_suite_core_ui.proximity.AudioProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.CallProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.CameraProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.WakeLockProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isIncoming
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.verify
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

    @Before
    fun setUp() {
        mockkObject(ProximitySensor)
        mockkObject(CallExtensions)
        every { ProximitySensor.bind(any<LifecycleService>(), any()) } returns proximitySensorMock
        with(CallExtensions) {
            every { callMock.isIncoming() } returns false
        }
        proximityDelegate = spyk(
            CallProximityDelegate(
                lifecycleContext = serviceMock,
                call = callMock,
                wakeLockProximityDelegate = wakeLockProximityDelegateMock,
                cameraProximityDelegate = cameraProximityDelegateMock,
                audioProximityDelegate = audioProximityDelegateMock,
            )
        )
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
    fun `incoming call, nothing is performed on proximity`() {
        every { callMock.isIncoming() } returns true
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 0) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 0) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 0) { audioProximityDelegateMock.trySwitchToEarpiece() }
        verify(exactly = 0) { wakeLockProximityDelegateMock.restoreScreenOn() }
        verify(exactly = 0) { cameraProximityDelegateMock.restoreCamera() }
        verify(exactly = 0) { audioProximityDelegateMock.tryRestoreToLoudspeaker() }
    }

    @Test
    fun `test on proximity on`() {
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns false
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test on proximity on, the screen is turned off, camera is forced to be disabled`() {
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns true
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera(true) }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test on proximity off`() {
        proximityDelegate?.onProximitySensorChanged(false)
        verify(exactly = 1) { wakeLockProximityDelegateMock.restoreScreenOn() }
        verify(exactly = 1) { cameraProximityDelegateMock.restoreCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.tryRestoreToLoudspeaker() }
    }
}