package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapConcat

@Suppress("UNCHECKED_CAST")
internal object GlassViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T =
        GlassViewModel(ProvidersHolder.callProvider!!) as T
}

internal class GlassViewModel(callLogicProvider: CallLogicProvider) : ViewModel() {
    val call: Flow<Call> = callLogicProvider.call
}

internal data class ParticipantStreamInfo(
    val isMyStream: Boolean,
    val username: String,
    val avatarUrl: String?,
    val stream: Stream
)
