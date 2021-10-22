package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory: ViewModelProvider.Factory { override fun <T : ViewModel?> create(modelClass: Class<T>): T = GlassViewModel(ProvidersHolder.callProvider!!) as T }

internal class GlassViewModel(callLogicProvider: CallLogicProvider): ViewModel() {

    var tiltEnabled = false

    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }
}