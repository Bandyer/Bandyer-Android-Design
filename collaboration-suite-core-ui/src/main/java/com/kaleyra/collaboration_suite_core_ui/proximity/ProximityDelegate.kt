package com.kaleyra.collaboration_suite_core_ui.proximity

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.os.Bundle
import android.os.PowerManager
import androidx.lifecycle.LifecycleOwner
import com.bandyer.android_audiosession.model.AudioOutputDevice
import com.bandyer.android_audiosession.session.AudioCallSession
import com.bandyer.android_audiosession.session.AudioCallSessionInstance
import com.kaleyra.collaboration_suite_core_ui.CallUI
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.getMyInternalCamera
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.hasMyCameraFrontLenses
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.hasUsbInput
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.hasUsersWithCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.isIncoming
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.isMyCameraEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.isMyScreenShareEnabled
import com.kaleyra.collaboration_suite_core_ui.utils.CallUtils.isNotConnected
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensor
import com.kaleyra.collaboration_suite_utils.proximity_listener.ProximitySensorListener

internal class ProximityDelegate<T>(private val lifecycleContext: T, private val call: CallUI): ProximitySensorListener where T: ContextWrapper, T: LifecycleOwner {

    private var proximityWakeLock: PowerManager.WakeLock? = null

    private var proximitySensor: ProximitySensor? = null

    private var callActivity: ProximityCallActivity? = null

    private val configuration = lifecycleContext.resources.configuration
    private val isDeviceLandscape: Boolean
        get() = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    private var wasCameraEnabled = false

    private var wasLoudspeakerActive = false

    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            if (activity !is ProximityCallActivity) return
            callActivity = activity
        }

        override fun onActivityStarted(activity: Activity) = Unit

        override fun onActivityResumed(activity: Activity) = Unit

        override fun onActivityPaused(activity: Activity) = Unit

        override fun onActivityStopped(activity: Activity) = Unit

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

        override fun onActivityDestroyed(activity: Activity) {
            callActivity = null
        }
    }

    init {
        val powerManager = lifecycleContext.getSystemService(Context.POWER_SERVICE) as PowerManager
        proximityWakeLock = powerManager
            .newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, javaClass.simpleName)
            .apply { setReferenceCounted(false) }
    }

    fun bind() {
        val application = lifecycleContext.applicationContext as? Application
        application?.registerActivityLifecycleCallbacks(callbacks)
        proximitySensor = ProximitySensor.bind(lifecycleContext, this)
    }

    fun destroy() {
        val application = lifecycleContext.applicationContext as? Application
        application?.unregisterActivityLifecycleCallbacks(callbacks)
        proximitySensor?.destroy()
        proximitySensor = null
    }

    override fun onProximitySensorChanged(isNear: Boolean) = if (isNear) onProximityOn() else onProximityOff()

    private fun onProximityOn() {
        if (isIncoming(call)) return

        callActivity?.disableWindowTouch()

        val shouldAcquireProximityLock = shouldAcquireProximityLock()
        if (shouldAcquireProximityLock) {
            proximityWakeLock?.acquire(10*60*1000L)
        }

        wasCameraEnabled = isMyCameraEnabled(call)
        val shouldDisableVideo = wasCameraEnabled && (shouldAcquireProximityLock || hasMyCameraFrontLenses(call))
        if (shouldDisableVideo) {
            getMyInternalCamera(call)?.tryDisable()
        }

        val audioCallSession = AudioCallSession.getInstance()
        wasLoudspeakerActive = audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Loudspeaker
        if (wasLoudspeakerActive) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Earpiece())
        }
    }

    private fun onProximityOff() {
        callActivity?.enableWindowTouch()

        proximityWakeLock?.release()

        if (wasCameraEnabled) {
            getMyInternalCamera(call)?.tryEnable()
        }

        val audioCallSession = AudioCallSession.getInstance()
        val shouldEnableLoudspeaker = wasLoudspeakerActive && audioCallSession.currentAudioOutputDevice is AudioOutputDevice.Earpiece
        if (shouldEnableLoudspeaker) {
            audioCallSession.tryEnableDevice(AudioOutputDevice.Loudspeaker())
        }

        wasCameraEnabled = false
        wasLoudspeakerActive = false
    }

    private fun shouldAcquireProximityLock(): Boolean {
        val callActivity = callActivity
        return when {
            call.disableProximitySensor -> false
            callActivity == null -> false
            !callActivity.isInForeground -> false
            callActivity.isInPip -> false
            callActivity.isWhiteboardDisplayed -> false
            callActivity.isFileShareDisplayed -> false
            isDeviceLandscape && isNotConnected(call) && isMyCameraEnabled(call) -> false
            isDeviceLandscape && hasUsersWithCameraEnabled(call) -> false
            isMyScreenShareEnabled(call) -> false
            hasUsbInput(call) -> false
            else -> true
        }
    }

    private fun AudioCallSessionInstance.tryEnableDevice(audioOutput: AudioOutputDevice) {
        val device = getAvailableAudioOutputDevices.firstOrNull { it.javaClass == audioOutput.javaClass }
        if (device != null) changeAudioOutputDevice(device)
    }
}