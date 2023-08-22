/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_core_ui.utils.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn

internal fun <T, M> StateFlow<T>.mapToStateFlow(coroutineScope: CoroutineScope, mapper: (value: T) -> M): StateFlow<M> =
    map { mapper(value) }.stateIn(coroutineScope, SharingStarted.Eagerly, mapper(value))

internal fun <T, M> SharedFlow<T>.mapToSharedFlow(coroutineScope: CoroutineScope, mapper: (value: T) -> M): SharedFlow<M> =
    map {
        mapper(replayCache[0])
    }.shareIn(coroutineScope, SharingStarted.Eagerly, 1)