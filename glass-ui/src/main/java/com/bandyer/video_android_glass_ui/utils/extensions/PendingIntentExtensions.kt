package com.bandyer.video_android_glass_ui.utils.extensions

import android.app.PendingIntent
import android.os.Build

object PendingIntentExtensions {
    val updateFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE else PendingIntent.FLAG_UPDATE_CURRENT
}