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
        context: FragmentActivity,
        callUIDelegate: CallUIDelegate,
        deviceStatusDelegate: DeviceStatusDelegate,
    ) = context.launchCall(GlassActivity::class.java, callUIDelegate, deviceStatusDelegate, listOf(Option.PARTICIPANTS), true)

    private fun <T : Activity> Context.launchCall(
        cls: Class<T>,
        callUIDelegate: CallUIDelegate,
        deviceStatusDelegate: DeviceStatusDelegate? = null,
        options: List<Option>,
        enableTilt: Boolean
    ) {
        ManagersHolder.callUIDelegateInstance = WeakReference(callUIDelegate)
        ManagersHolder.deviceStatusDelegateInstance = WeakReference(deviceStatusDelegate)
        startActivity(Intent(this, cls).apply {
            putExtra("enableTilt", enableTilt)
            putExtra("options", options.toTypedArray())
        })
    }
}

internal object ManagersHolder {
    var callUIDelegateInstance: WeakReference<CallUIDelegate>? = null
    var deviceStatusDelegateInstance: WeakReference<DeviceStatusDelegate>? = null
}

interface CallUIDelegate {

    val call: Call

    val userDetailsWrapper: StateFlow<UserDetailsWrapper>

    suspend fun requestMicPermission(context: FragmentActivity): Permission

    suspend fun requestCameraPermission(context: FragmentActivity): Permission

    fun answer()

    fun hangup()

    fun enableCamera(enable: Boolean)

    fun enableMic(enable: Boolean)

    fun switchCamera()

    fun getVolume(): Volume

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

interface DeviceStatusDelegate {
    val battery: Flow<BatteryInfo>
    val wifi: Flow<WiFiInfo>
}

