package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal fun <T, M> StateFlow<T>.mapToStateFlow(coroutineScope: CoroutineScope, mapper: (value: T) -> M): StateFlow<M> =
    map { mapper(value) }.stateIn(coroutineScope, SharingStarted.Eagerly, mapper(value))