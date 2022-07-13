/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.participants

import android.os.Parcelable
import androidx.annotation.Keep
import com.kaleyra.collaboration_suite_glass_ui.model.internal.UserState
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * ChatParticipant
 *
 * @property userDescription The user description
 * @property userImage The user image
 * @property userState UserState
 * @property lastSeenTime The last time the user was online expressed as a long timestamp
 * @constructor
 */
@Keep
@Parcelize
internal data class ChatParticipant(
    val userDescription: String,
    val userImage: String,
    val userState: @RawValue UserState,
    val lastSeenTime: Long = 0
) : Parcelable