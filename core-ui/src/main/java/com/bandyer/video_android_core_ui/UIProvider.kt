package com.bandyer.video_android_core_ui

import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import com.kaleyra.collaboration_suite_utils.ContextRetainer
import com.bandyer.video_android_core_ui.utils.DeviceUtils.isSmartGlass

internal object UIProvider {

    fun <T> showCall(activityClazz: Class<T>) =
        with(ContextRetainer.context) {
            val intent = Intent(this, activityClazz).apply {
                addFlags(FLAG_ACTIVITY_NEW_TASK)
                // TODO
                putExtra("enableTilt", isSmartGlass)
//            putExtra("options", listOf().toTypedArray())
            }
            startActivity(intent)
        }
}