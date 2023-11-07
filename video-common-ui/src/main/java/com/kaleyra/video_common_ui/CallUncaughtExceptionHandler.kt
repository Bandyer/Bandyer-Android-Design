package com.kaleyra.video_common_ui

import com.kaleyra.video_common_ui.call.CallNotificationDelegate
import com.kaleyra.video_common_ui.notification.NotificationManager

internal object CallUncaughtExceptionHandler: Thread.UncaughtExceptionHandler {

    private val oldExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        NotificationManager.cancel(CallNotificationDelegate.CALL_NOTIFICATION_ID)
        oldExceptionHandler?.uncaughtException(t, e)
    }
}