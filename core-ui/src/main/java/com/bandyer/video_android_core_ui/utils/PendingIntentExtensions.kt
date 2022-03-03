package com.bandyer.video_android_core_ui.utils

import android.app.PendingIntent
import android.os.Build

internal object PendingIntentExtensions {
    val updateFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
}