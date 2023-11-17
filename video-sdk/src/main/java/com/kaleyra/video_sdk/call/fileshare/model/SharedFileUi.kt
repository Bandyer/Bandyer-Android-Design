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

package com.kaleyra.video_sdk.call.fileshare.model

import androidx.compose.runtime.Immutable
import com.kaleyra.video_sdk.common.avatar.model.ImmutableUri

@Immutable
data class SharedFileUi(
    val id: String,
    val name: String,
    val uri: ImmutableUri,
    val size: Long?,
    val sender: String,
    val time: Long,
    val state: State,
    val isMine: Boolean
) {

    @Immutable
    sealed class State {
        object Available : State()
        object Pending : State()
        data class InProgress(val progress: Float) : State()
        data class Success(val uri: ImmutableUri) : State()
        object Error : State()
        object Cancelled : State()
    }
}

