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

package com.bandyer.video_android_glass_ui.participants

import android.os.Parcelable
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.bandyer.video_android_glass_ui.model.internal.UserState
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/**
 * ParticipantData
 *
 * @property name The name
 * @property userAlias The user alias
 * @property userState UserState
 * @property avatarImageId The local avatar resource
 * @property avatarImageUrl The remote avatar url resource
 * @property lastSeenTime The last time the user was online expressed as a long timestamp
 * @constructor
 */
@Keep
@Parcelize
internal data class ParticipantData(
    val name: String,
    val userAlias: String,
    val userState: @RawValue UserState,
    @DrawableRes val avatarImageId: Int? = null,
    val avatarImageUrl: String? = null,
    val lastSeenTime: Long = 0
) : Parcelable