package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_glass_ui.model.Option
import com.bandyer.video_android_glass_ui.model.Permission
import com.bandyer.video_android_glass_ui.model.Volume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

object GlassUI {

    fun show(
        context: Context,
        callUIDelegate: CallUIDelegate,
        deviceStatusDelegate: DeviceStatusDelegate,
        callUIController: CallUIController
    ) = context.launchCall(GlassActivity::class.java, callUIDelegate, deviceStatusDelegate, callUIController, listOf(Option.PARTICIPANTS), false)

    private fun <T : Activity> Context.launchCall(
        cls: Class<T>,
        callUIDelegate: CallUIDelegate,
        deviceStatusDelegate: DeviceStatusDelegate? = null,
        callUIController: CallUIController,
        options: List<Option>,
        enableTilt: Boolean
    ) {
        ManagersHolder.callUIDelegateInstance = WeakReference(callUIDelegate)
        ManagersHolder.callUIControllerInstance = WeakReference(callUIController)
        ManagersHolder.deviceStatusDelegateInstance = WeakReference(deviceStatusDelegate)
        startActivity(Intent(this, cls).apply {
            putExtra("enableTilt", enableTilt)
            putExtra("options", options.toTypedArray())
        })
    }
}

internal object ManagersHolder {
    var callUIDelegateInstance: WeakReference<CallUIDelegate>? = null
    var callUIControllerInstance: WeakReference<CallUIController>? = null
    var deviceStatusDelegateInstance: WeakReference<DeviceStatusDelegate>? = null
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

interface CallUIDelegate {
    val call: Call
    val userDetailsWrapper: StateFlow<UserDetailsWrapper>
}

interface DeviceStatusDelegate {
    val battery: Flow<BatteryInfo>
    val wifi: Flow<WiFiInfo>
}

