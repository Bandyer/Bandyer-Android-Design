package com.kaleyra.collaboration_suite_core_ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.PowerManager
import com.kaleyra.collaboration_suite_core_ui.proximity.ProximityCallActivity
import com.kaleyra.collaboration_suite_core_ui.proximity.WakeLockProximityDelegate
import com.kaleyra.collaboration_suite_core_ui.proximity.WakeLockProximityDelegateImpl
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isOrientationLandscape
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test

internal class WakeLockProximityDelegateTest {

    private val applicationMock = mockk<Application>(relaxed = true)

    private val proximityCallActivityMock = spyk(object : Activity(), ProximityCallActivity {
        override val disableProximity: Boolean = false
        override fun disableWindowTouch() = Unit
        override fun enableWindowTouch() = Unit
    })

    private val powerManager = mockk<PowerManager>()

    private val proximityWakeLock = mockk<PowerManager.WakeLock>(relaxed = true)

    private val callMock = mockk<CallUI>()

    private var wakeLockProximityDelegate: WakeLockProximityDelegate? = null

    @Before
    fun setUp() {
        mockkObject(CallExtensions)
        mockkObject(ContextExtensions)
        with(CallExtensions) {
            every { callMock.disableProximitySensor } returns false
            every { callMock.isNotConnected() } returns false
            every { callMock.isMyInternalCameraEnabled() } returns false
            every { callMock.hasUsersWithCameraEnabled() } returns false
            every { callMock.isMyScreenShareEnabled() } returns false
            every { callMock.hasUsbInput() } returns false
        }
        with(ContextExtensions) {
            every { applicationMock.isOrientationLandscape() } returns false
        }
        every { applicationMock.getSystemService(Context.POWER_SERVICE) } returns powerManager
        every { powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, any()) } returns proximityWakeLock
        wakeLockProximityDelegate = WakeLockProximityDelegateImpl(applicationMock, callMock).apply {
            onActivityCreated(proximityCallActivityMock, null)
        }
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `set proximity wake lock to not reference counted`() {
        verify(exactly = 1) { proximityWakeLock.setReferenceCounted(false) }
    }

    @Test
    fun testBind() {
        wakeLockProximityDelegate!!.bind()
        verify(exactly = 1) {
            applicationMock.registerActivityLifecycleCallbacks(withArg {
                assert(it is WakeLockProximityDelegate)
            })
        }
    }

    @Test
    fun testDestroy() {
        wakeLockProximityDelegate!!.destroy()
        verify(exactly = 1) {
            applicationMock.unregisterActivityLifecycleCallbacks(withArg {
                assert(it is WakeLockProximityDelegate)
            })
        }
    }

    @Test
    fun testTryScreenOff() {
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 1) { proximityCallActivityMock.disableWindowTouch() }
        verify(exactly = 1) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `call disable proximity sensor flag is true, screen is not turned off`() {
        every { callMock.disableProximitySensor } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `proximity call disable proximity flag is true, screen is not turned off`() {
        every { proximityCallActivityMock.disableProximity } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `my screen share is enabled, screen is not turned off`() {
        every { callMock.isMyScreenShareEnabled() } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `usb camera connected, screen is not turned off`() {
        every { callMock.hasUsbInput() } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `device in landscape and there are users with camera enabled, screen is not turned off`() {
        every { applicationMock.isOrientationLandscape() } returns true
        every { callMock.hasUsersWithCameraEnabled() } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun `device in landscape and call is not connected and my internal camera is enabled, screen is not turned off`() {
        every { applicationMock.isOrientationLandscape() } returns true
        every { callMock.isNotConnected() } returns true
        every { callMock.isMyInternalCameraEnabled() } returns true
        wakeLockProximityDelegate!!.tryTurnScreenOff()
        verify(exactly = 0) { proximityWakeLock.acquire(any()) }
    }

    @Test
    fun testRestoreScreenOn() {
        wakeLockProximityDelegate!!.restoreScreenOn()
        verify(exactly = 1) { proximityCallActivityMock.enableWindowTouch() }
        verify(exactly = 1) { proximityWakeLock.release() }
    }
}