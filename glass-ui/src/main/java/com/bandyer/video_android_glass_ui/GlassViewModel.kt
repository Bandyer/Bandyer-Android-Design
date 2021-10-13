package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

internal class GlassViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    fun getParticipants(): StateFlow<List<CallParticipant>> = callLogicProvider.getParticipants()
}
