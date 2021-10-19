package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.Flow

interface CallUIController {

    companion object Launcher {
        fun launchGlassUI(context: Context, controllerCall: CallUIController) =
            context.launchUI(GlassActivity::class.java, controllerCall)

        private fun <T: Activity> Context.launchUI(cls: Class<T>, controllerCall: CallUIController) {
            ProvidersHolder.callProvider = CallLogicProvider.create(controllerCall)
            startActivity(Intent(this, cls))
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


