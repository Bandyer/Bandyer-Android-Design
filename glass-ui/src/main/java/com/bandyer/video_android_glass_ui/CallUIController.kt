package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.bandyer.video_android_glass_ui.model.Call
import com.bandyer.video_android_glass_ui.model.Option
import kotlinx.coroutines.flow.Flow

interface CallUIController {

    companion object Launcher {
        fun Context.launchCallGlass(controllerCall: CallUIController, options: List<Option>, enableTilt: Boolean = false) =
            launchCall(GlassActivity::class.java, controllerCall, options, enableTilt)

        private fun <T: Activity> Context.launchCall(cls: Class<T>, controllerCall: CallUIController, options: List<Option>, enableTilt: Boolean) {
            ProvidersHolder.callProvider = CallLogicProvider.create(controllerCall)
            startActivity(Intent(this, cls).apply {
                putExtra("enableTilt", enableTilt)
                putExtra("options", options.toTypedArray())
            })
        }
    }

    val call: Flow<Call>

    fun requestPermissions(context: FragmentActivity)

    fun answer()

    fun hangup()

    fun enableCamera(enable: Boolean)

    fun enableMic(enable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

internal object ProvidersHolder {
    var callProvider: CallLogicProvider? = null
}


