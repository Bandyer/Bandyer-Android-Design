package com.bandyer.video_android_core_ui

import android.net.Uri

class UsersDescription(
    val name: suspend (userIds: List<String>) -> String = { it.joinToString() },
    val image: suspend (userIds: List<String>) -> Uri = { Uri.EMPTY }
)
