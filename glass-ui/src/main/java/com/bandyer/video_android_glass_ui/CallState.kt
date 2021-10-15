package com.bandyer.video_android_glass_ui

sealed class CallState {
    object Dialing : CallState()
    object Reconnecting : CallState()
    object Ringing : CallState()
    object Started : CallState()
    sealed class Disconnected : CallState() {

        companion object : Disconnected() {
            override fun hashCode(): Int = "Disconnected".hashCode()
            override fun equals(other: Any?) = other !is Ended && other is Disconnected
            override fun toString() = "Disconnected"
        }

        data class Ended(val reason: String) : Disconnected()
        data class Error(val reason: String) : Disconnected()
    }
}