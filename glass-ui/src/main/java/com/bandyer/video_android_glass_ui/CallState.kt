package com.bandyer.video_android_glass_ui

sealed class CallState {
    object Disconnected : CallState()
    object Dialing : CallState()
    object Reconnecting : CallState()
    object Ringing : CallState()
    object Started: CallState()
    data class Ended(val reason: String) : CallState()
    data class Error(val reason: String): CallState()
}