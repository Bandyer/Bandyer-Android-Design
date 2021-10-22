package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow

interface CallUIController {

    companion object Launcher {
        fun launchCallGlass(context: Context, controllerCall: CallUIController, tiltEnabled: Boolean = false) =
            context.launchCall(GlassActivity::class.java, controllerCall, tiltEnabled)

        private fun <T: Activity> Context.launchCall(cls: Class<T>, controllerCall: CallUIController, tiltEnabled: Boolean) {
            ProvidersHolder.callProvider = CallLogicProvider.create(controllerCall)
            startActivity(Intent(this, cls).apply { putExtra("tiltEnabled", tiltEnabled) })
        }
    }

    val call: Flow<Call>

    fun hangup()

    fun disableCamera(disable: Boolean)

    fun disableMic(disable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}

internal object ProvidersHolder {
    var callProvider: CallLogicProvider? = null
}


