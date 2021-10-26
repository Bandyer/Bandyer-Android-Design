package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

@Suppress("UNCHECKED_CAST")
internal object NavGraphViewModelFactory: ViewModelProvider.Factory { override fun <T : ViewModel?> create(modelClass: Class<T>): T = NavGraphViewModel(ProvidersHolder.callProvider!!) as T }

internal class NavGraphViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    val call: Flow<Call> = callLogicProvider.call

    val callState: Flow<Call.State> = call.flatMapConcat { call -> call.state }

    val participants: Flow<CallParticipants> = call.flatMapConcat { call -> call.participants }

    fun disableCamera(disable: Boolean) = callLogicProvider.disableCamera(disable)

    fun disableMic(disable: Boolean) = callLogicProvider.disableMic(disable)

    fun hangUp() = callLogicProvider.hangup()
}