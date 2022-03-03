package com.bandyer.video_android_core_ui

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.bandyer.android_common.ContextRetainer

internal object UIProvider {

    fun <T> showCall(activityClazz: Class<T>) =
        with(ContextRetainer.context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                // TODO
                putExtra("enableTilt", false)
//            putExtra("options", listOf().toTypedArray())
            }
            startActivity(intent)
        }
}