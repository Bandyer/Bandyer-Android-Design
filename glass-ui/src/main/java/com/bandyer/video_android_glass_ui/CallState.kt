package com.bandyer.video_android_glass_ui

sealed class CallState {
    object Dialing : CallState()
    object Connecting : CallState()
    object Ringing : CallState()
    data class Ended(val reason: String) : CallState()
    data class Error(val reason: String): CallState()
}