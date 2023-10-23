package com.kaleyra.collaboration_suite_core_ui.mapper

import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite.conference.Input
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal object InputMapper {

    inline fun <reified T:Input> Flow<Call>.isInputActive(): Flow<Boolean> =
        flatMapLatest { it.inputs.availableInputs }
            .map { inputs -> inputs.firstOrNull { it is T } }
            .flatMapLatest { it?.state ?: flowOf(null) }
            .map { it is Input.State.Active }
            .distinctUntilChanged()

    fun Flow<Call>.isDeviceScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Screen>()

    fun Flow<Call>.isAppScreenInputActive(): Flow<Boolean> = isInputActive<Input.Video.Application>()

    fun Flow<Call>.isAnyScreenInputActive(): Flow<Boolean> =
        combine(isDeviceScreenInputActive(), isAppScreenInputActive()) { isDeviceScreenInputActive, isAppScreenInputActive ->
            isDeviceScreenInputActive || isAppScreenInputActive
    }
}
