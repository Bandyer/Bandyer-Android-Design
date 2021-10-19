package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

internal class GlassViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    val call: Flow<Call> = callLogicProvider.getCall()

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }

    fun hangUp() = callLogicProvider.hangup()
}
