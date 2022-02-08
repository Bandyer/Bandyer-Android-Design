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
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

object GlassUIProvider {
    fun showCall(context: Context) {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, GlassActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            // TODO
            putExtra("enableTilt", false)
//            putExtra("options", listOf().toTypedArray())
        }
        applicationContext.startActivity(intent)
    }
}

//interface CallWaitingUIController {
//    fun onHangUpAndAnswer(newCall: Call)
//
//    fun onDecline(newCall: Call)
//
//    fun onHoldAndAnswer(newCall: Call)
//}

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
    val call: SharedFlow<Call>
    val userDetailsDelegate: StateFlow<UserDetailsDelegate?>
}

interface DeviceStatusDelegate {
    val battery: SharedFlow<BatteryInfo>
    val wifi: SharedFlow<WiFiInfo>
}