package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bandyer.video_android_glass_ui.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.lang.ref.WeakReference

object GlassUi {

    fun Context.launchCallGlass(
        callManager: CallManager,
        utilityManager: UtilityManager,
        options: List<Option>,
        enableTilt: Boolean = false
    ) = launchCall(GlassActivity::class.java, callManager, utilityManager, options, enableTilt)

    private fun <T : Activity> Context.launchCall(
        cls: Class<T>,
        callManager: CallManager,
        utilityManager: UtilityManager? = null,
        options: List<Option>,
        enableTilt: Boolean
    ) {
        ManagersHolder.callManagerInstance = WeakReference(callManager)
        ManagersHolder.utilityManagerInstance = WeakReference(utilityManager)
        startActivity(Intent(this, cls).apply {
            putExtra("enableTilt", enableTilt)
            putExtra("options", options.toTypedArray())
        })
    }
}

internal object ManagersHolder {
    var callManagerInstance: WeakReference<CallManager>? = null
    var utilityManagerInstance: WeakReference<UtilityManager>? = null
}

interface CallManager {

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

interface UtilityManager {
    val battery: Flow<Battery>

    val wifi: Flow<WiFi>
}

