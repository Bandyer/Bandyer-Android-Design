package com.kaleyra.demo_video_sdk.ui.custom_views

import com.google.gson.Gson
import com.kaleyra.video_common_ui.CallUI

val gson by lazy { Gson() }

data class CallConfiguration(
    val actions: Set<CallUI.Action> = CallUI.Action.all,
    val options: CallOptions = CallOptions()
) {
    data class CallOptions(
        val recordingEnabled: Boolean = false,
        val feedbackEnabled: Boolean = false,
        val backCameraAsDefault: Boolean = false,
        val disableProximitySensor: Boolean = false
    )

    fun encode(): String = gson.toJson(this)

    companion object {
        fun decode(data: String): CallConfiguration = gson.fromJson(data, CallConfiguration::class.java)
    }
}