package com.bandyer.video_android_glass_ui

import android.view.View
import kotlinx.coroutines.flow.StateFlow

sealed class CallParticipant(val id: String, val name: String, val avatarUrl: String?, val renderView: View, val state: StateFlow<State>) {
    class Me(id: String, name: String, avatarUrl: String?, renderView: View, state: StateFlow<State>) : CallParticipant(id, name, avatarUrl, renderView, state)
    class Other(id: String, name: String, avatarUrl: String?, renderView: View, state: StateFlow<State>) : CallParticipant(id, name, avatarUrl, renderView, state)

    enum class State {
        IN_CALL,
        OFFLINE,
        INVITED
    }
}