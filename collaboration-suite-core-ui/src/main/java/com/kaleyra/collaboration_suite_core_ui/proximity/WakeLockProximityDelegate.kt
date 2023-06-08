package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.isOrientationLandscape

interface WakeLockProximityDelegate {

    val application: Application

    val call: CallUI

    val isScreenTurnedOff: Boolean

    fun bind()

    fun destroy()

    fun tryTurnScreenOff()

    fun restoreScreenOn()
}

internal class WakeLockProximityDelegateImpl(
    override val application: Application,
    override val call: CallUI,
) : WakeLockProximityDelegate  {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    override var isScreenTurnedOff: Boolean = false
        private set

    override fun bind() {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
        proximityWakeLock!!.setReferenceCounted(false)
    }

    override fun destroy() {
        proximityWakeLock?.release()
        proximityWakeLock = null
        isScreenTurnedOff = false
    }

    override fun tryTurnScreenOff() {
        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(WakeLockTimeout)
        }
        isScreenTurnedOff = shouldAcquireProximityLock
    }

    override fun restoreScreenOn() {
        proximityWakeLock?.release()
        isScreenTurnedOff = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        val isDeviceInLandscape = application.isOrientationLandscape()
        return when {
            call.disableProximitySensor -> false
            isDeviceInLandscape && call.isNotConnected() && call.isMyInternalCameraEnabled() -> false
            isDeviceInLandscape && call.hasUsersWithCameraEnabled() -> false
            call.isMyScreenShareEnabled() -> false
            call.hasUsbInput() -> false
            else -> true
        }
    }

    companion object {
        const val WakeLockTimeout = 60 * 60 * 1000L /*1 hour*/
    }
}