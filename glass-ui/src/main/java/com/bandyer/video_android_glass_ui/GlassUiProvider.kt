package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build
import androidx.fragment.app.FragmentActivity
import com.bandyer.android_common.battery_observer.BatteryInfo
import com.bandyer.android_common.network_observer.WiFiInfo
import com.bandyer.collaboration_center.phonebox.Call
import com.bandyer.video_android_core_ui.UIProvider
import com.bandyer.video_android_core_ui.UsersDescription
import com.bandyer.video_android_core_ui.model.Permission
import com.bandyer.video_android_core_ui.model.Volume
import kotlinx.coroutines.flow.SharedFlow

object GlassUIProvider: UIProvider {
    override fun showCall(context: Context) {
        val applicationContext = context.applicationContext
        val intent = Intent(applicationContext, GlassCallActivity::class.java).apply {
            addFlags(FLAG_ACTIVITY_NEW_TASK)
            // TODO
            putExtra("enableTilt", false)
//            putExtra("options", listOf().toTypedArray())
        }
        applicationContext.startActivity(intent)
    }

    override fun isUIActivity(activity: Activity) = activity is GlassCallActivity

    override fun createCallPendingIntent(context: Context): PendingIntent {
        val applicationContext = context.applicationContext
        val notifyIntent = Intent(applicationContext, GlassCallActivity::class.java).apply {
            flags = FLAG_ACTIVITY_NEW_TASK or FLAG_ACTIVITY_CLEAR_TASK
            putExtra("enableTilt", false)
        }
        val notifyFlags =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            else PendingIntent.FLAG_UPDATE_CURRENT
        return PendingIntent.getActivity(applicationContext, 0, notifyIntent, notifyFlags)
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
    val usersDescription: UsersDescription
}

interface DeviceStatusDelegate {
    val battery: SharedFlow<BatteryInfo>
    val wifi: SharedFlow<WiFiInfo>
}