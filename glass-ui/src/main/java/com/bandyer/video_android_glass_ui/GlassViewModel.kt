package com.bandyer.video_android_glass_ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.Flow

internal class GlassViewModel(private val callLogicProvider: CallLogicProvider): ViewModel() {

    fun getCall(): Flow<Call> = callLogicProvider.getCall()

    fun hangUp() = callLogicProvider.hangup()
}
