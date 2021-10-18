package com.bandyer.video_android_glass_ui

import android.view.View
import kotlinx.coroutines.flow.StateFlow

data class Stream(val id: String, val view: View, val state: StateFlow<State>) {

    sealed class State {

        object Opening: State()

        object Open: State()

        sealed class Closed: State() {

            companion object : Closed() {
                override fun hashCode(): Int = "Closed".hashCode()
                override fun equals(other: Any?) = other !is Error && other is Closed
                override fun toString() = "Closed"
            }

            data class Error(val reason: String): Closed()
        }
    }
}