package com.bandyer.video_android_glass_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.StateFlow

interface CallUIController {

    companion object Launcher {
        fun launchGlassUI(context: Context, controllerCall: CallUIController) =
            context.launchUI(GlassActivity::class.java, controllerCall)

        private fun <T: Activity> Context.launchUI(cls: Class<T>, controllerCall: CallUIController) =
            startActivity(
                Intent(this, cls).apply {
                    putExtra("provider", CallLogicProvider.create(controllerCall))
                }
            )
    }

    var state: StateFlow<CallState>

    var recording: StateFlow<Boolean>

    var duration: StateFlow<Long>

    var participants: StateFlow<List<CallParticipant>>

    fun hangup()

    fun disableCamera(disable: Boolean)

    fun disableMic(disable: Boolean)

    fun switchCamera()

    fun setVolume(value: Int)

    fun setZoom(value: Int)
}


