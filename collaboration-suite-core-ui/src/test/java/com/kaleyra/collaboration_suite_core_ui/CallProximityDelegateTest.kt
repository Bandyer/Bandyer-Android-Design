package com.kaleyra.collaboration_suite_core_ui

import androidx.lifecycle.LifecycleService
import com.kaleyra.collaboration_suite_core_ui.proximity.AudioProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.CallProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.CameraProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.ProximityCallActivity
import com.kaleyra.collaboration_suite_core_ui.proximity.WakeLockProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions
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

    private val proximityCallActivityMock = mockk<ProximityCallActivity>(relaxed = true)

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
                proximityCallActivity = proximityCallActivityMock,
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
    fun `test proximity on when the screen is not turned off`() {
        every { proximityCallActivityMock.disableProximity } returns false
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns false
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 1) { proximityCallActivityMock.disableWindowTouch() }
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test proximity on when the screen is turned off, the camera is forced to be disabled`() {
        every { proximityCallActivityMock.disableProximity } returns false
        every { wakeLockProximityDelegateMock.isScreenTurnedOff } returns true
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 1) { proximityCallActivityMock.disableWindowTouch() }
        verify(exactly = 1) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 1) { cameraProximityDelegateMock.tryDisableCamera(true) }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test call proximity activity disable proximity true`() {
        every { proximityCallActivityMock.disableProximity } returns true
        proximityDelegate?.onProximitySensorChanged(true)
        verify(exactly = 1) { proximityCallActivityMock.disableWindowTouch() }
        verify(exactly = 0) { wakeLockProximityDelegateMock.tryTurnScreenOff() }
        verify(exactly = 0) { cameraProximityDelegateMock.tryDisableCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.trySwitchToEarpiece() }
    }

    @Test
    fun `test proximity off`() {
        proximityDelegate?.onProximitySensorChanged(false)
        verify(exactly = 1) { proximityCallActivityMock.enableWindowTouch() }
        verify(exactly = 1) { wakeLockProximityDelegateMock.restoreScreenOn() }
        verify(exactly = 1) { cameraProximityDelegateMock.restoreCamera() }
        verify(exactly = 1) { audioProximityDelegateMock.tryRestoreToLoudspeaker() }
    }
}