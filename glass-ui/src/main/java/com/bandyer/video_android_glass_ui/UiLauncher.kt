package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bandyer.video_android_glass_ui.model.Battery
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.model.Option
import com.bandyer.video_android_glass_ui.model.WiFi
import kotlinx.coroutines.flow.Flow
import java.lang.ref.WeakReference

object UiLauncher {

    private val uiListener = object : UiEventObserver {
        override fun onEvent(event: UiEvent) {
            if(event == UiEvent.DESTROY) {
                ManagersHolder.callManagerInstance = null
                UiEventNotifier.removeObserver(this)
            }
        }
    }

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
        UiEventNotifier.addObserver(this@UiLauncher.uiListener)
        ManagersHolder.callManagerInstance = callManager
        startActivity(Intent(this, cls).apply {
            putExtra("enableTilt", enableTilt)
            putExtra("options", options.toTypedArray())
        })
    }
}

internal object ManagersHolder {
    var callManagerInstance: CallManager? = null
}

interface CallManager {

    val call: Flow<Call>

    val battery: Flow<Battery>

    val wifi: Flow<WiFi>

//    val volume: Flow<Volume>

    fun requestPermissions(context: FragmentActivity)

    fun answer()

    fun hangup()

    fun enableCamera(enable: Boolean)

    fun enableMic(enable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

