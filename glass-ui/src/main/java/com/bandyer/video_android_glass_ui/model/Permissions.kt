package com.bandyer.video_android_glass_ui.model

data class Permissions(
    val microphoneAllowed: Boolean,
    val deviceCameraAllowed: Boolean,
    val usbCameraAllowed: Boolean,
    val nOfRetries: Int
)