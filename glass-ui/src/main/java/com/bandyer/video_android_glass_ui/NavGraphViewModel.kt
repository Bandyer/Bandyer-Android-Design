package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

internal class NavGraphViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }

    fun hangUp() = callLogicProvider.hangup()
}

internal class GlassViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    var tiltEnabled = false

    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }
}