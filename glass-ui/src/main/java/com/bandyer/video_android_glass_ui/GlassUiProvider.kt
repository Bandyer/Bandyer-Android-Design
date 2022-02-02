package com.bandyer.video_android_glass_ui

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_glass_ui.model.Permission
import com.bandyer.video_android_glass_ui.model.Volume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

object GlassUIProvider {

    @set:JvmSynthetic
    @get:JvmSynthetic
    internal var callUIController: WeakReference<CallUIController>? = null
        private set

    @set:JvmSynthetic
    @get:JvmSynthetic
    internal var callUIDelegate: WeakReference<CallUIDelegate>? = null
        private set

    @set:JvmSynthetic
    @get:JvmSynthetic
    internal var deviceStatusDelegate: WeakReference<DeviceStatusDelegate>? = null
        private set

    @set:JvmSynthetic
    @get:JvmSynthetic
    internal var callUIControllerExtension: WeakReference<CallUIControllerExtension>? = null
        private set

    @set:JvmSynthetic
    @get:JvmSynthetic
    internal var callUIDelegateExtension: WeakReference<CallUIDelegateExtension>? = null
        private set

    fun showCall(
        context: Context,
        callUIController: CallUIController,
        callUIDelegate: CallUIDelegate,
        deviceStatusDelegate: DeviceStatusDelegate
    ) {
        this.callUIController = WeakReference(callUIController)
        this.callUIDelegate = WeakReference(callUIDelegate)
        this.deviceStatusDelegate = WeakReference(deviceStatusDelegate)
//        this.callUIControllerExtension = WeakReference(callUIControllerExtension)
//        this.callUIDelegateExtension = WeakReference(callUIDelegateExtension)
        val intent = Intent(context, GlassActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            // TODO
            putExtra("enableTilt", false)
//            putExtra("options", listOf().toTypedArray())
        }
        context.startActivity(intent)
    }

}

interface CallUIControllerExtension {
    fun onHangUpAndAnswer()

    fun onDecline()

    fun onHoldAndAnswer()
}

interface CallUIController {
    suspend fun onRequestMicPermission(context: FragmentActivity): Permission

    suspend fun onRequestCameraPermission(context: FragmentActivity): Permission

    fun onAnswer()

    fun onHangup()

    fun onEnableCamera(enable: Boolean)

    fun onEnableMic(enable: Boolean)

    fun onSwitchCamera()

    fun onGetVolume(): Volume

    fun onSetVolume(value: Int)

    fun onSetZoom(value: Int)
}

interface CallUIDelegateExtension {
    val incomingCall: SharedFlow<Call>
}

interface CallUIDelegate {
    val call: Call
    val userDetailsWrapper: StateFlow<UserDetailsWrapper>
}

interface DeviceStatusDelegate {
    val battery: SharedFlow<BatteryInfo>
    val wifi: SharedFlow<WiFiInfo>
}