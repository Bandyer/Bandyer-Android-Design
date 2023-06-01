package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.os.PowerManager
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyInternalCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallExtensions.isNotConnected

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
) : WakeLockProximityDelegate, Application.ActivityLifecycleCallbacks  {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var proximityCallActivity: ProximityCallActivity? = null

    override var isScreenTurnedOff: Boolean = false
        private set

    private val isDeviceLandscape: Boolean
        get() = application.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    init {
        val powerManager = application.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
        proximityWakeLock!!.setReferenceCounted(false)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity !is ProximityCallActivity) return
        proximityCallActivity = activity
    }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        proximityCallActivity = null
    }

    override fun bind() {
        application.registerActivityLifecycleCallbacks(this)
    }

    override fun destroy() {
        application.unregisterActivityLifecycleCallbacks(this)
    }

    override fun tryTurnScreenOff() {
        proximityCallActivity?.disableWindowTouch()
        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(WakeLockTimeout)
        }
        isScreenTurnedOff = shouldAcquireProximityLock
    }

    override fun restoreScreenOn() {
        proximityCallActivity?.enableWindowTouch()
        proximityWakeLock?.release()
        isScreenTurnedOff = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        return when {
            call.disableProximitySensor -> false
            proximityCallActivity?.enableProximity != true -> false
            isDeviceLandscape && call.isNotConnected() && call.isMyInternalCameraEnabled() -> false
            isDeviceLandscape && call.hasUsersWithCameraEnabled() -> false
            call.isMyScreenShareEnabled() -> false
            call.hasUsbInput() -> false
            else -> true
        }
    }

    companion object {
        private const val WakeLockTimeout = 60 * 60 * 1000L /*1 hour*/
    }
}