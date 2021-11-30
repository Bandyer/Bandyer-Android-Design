package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bandyer.video_android_glass_ui.model.Volume
import com.bandyer.video_android_glass_ui.model.*
import kotlinx.coroutines.flow.Flow
import java.lang.ref.WeakReference

object GlassUi {

    fun Context.launchCallGlass(
        callManager: CallManager,
        options: List<Option>,
        enableTilt: Boolean = false
    ) = launchCall(GlassActivity::class.java, callManager, options, enableTilt)

    private fun <T : Activity> Context.launchCall(
        cls: Class<T>,
        callManager: CallManager,
        options: List<Option>,
        enableTilt: Boolean
    ) {
        ManagersHolder.callManagerInstance = WeakReference(callManager)
        startActivity(Intent(this, cls).apply {
            putExtra("enableTilt", enableTilt)
            putExtra("options", options.toTypedArray())
        })
    }
}

internal object ManagersHolder {
    var callManagerInstance: WeakReference<CallManager>? = null
}

interface CallManager {

    val call: Call

    val battery: Flow<Battery>

    val wifi: Flow<WiFi>

    val permissions: Flow<Permissions>

    fun requestMicPermission(context: FragmentActivity)

    fun requestCameraPermission(context: FragmentActivity)

    fun answer()

    fun hangup()

    fun enableCamera(enable: Boolean)

    fun enableMic(enable: Boolean)

    fun switchCamera()

    fun getVolume(): Volume

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

