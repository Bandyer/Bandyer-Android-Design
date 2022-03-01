package com.bandyer.video_android_core_ui

import android.app.Activity
import android.app.PendingIntent
import android.content.Context

interface UIProvider {
    fun showCall(context: Context)
    fun isUIActivity(activity: Activity): Boolean
}