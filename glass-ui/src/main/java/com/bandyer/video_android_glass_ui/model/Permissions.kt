package com.bandyer.video_android_glass_ui.model

data class Permissions(
    val micPermission: Permission,
    val cameraPermission: Permission,
)

data class Permission(
    val isAllowed: Boolean,
    val neverAskAgain: Boolean
)