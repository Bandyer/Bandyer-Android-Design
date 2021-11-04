package com.bandyer.video_android_glass_ui.common

/**
 * The user online state
 */
internal sealed class UserState {

    object Online : UserState()

    object Offline : UserState()

    data class Invited(val isOnline: Boolean) : UserState() {
        companion object : UserState() {
            override fun hashCode(): Int = "Invited".hashCode()
            override fun equals(other: Any?) = other is Invited
            override fun toString() = "Invited"
        }
    }
}