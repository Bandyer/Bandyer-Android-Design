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

package com.bandyer.video_android_glass_ui.chat

import androidx.annotation.DrawableRes

/**
 * The message data from the [ChatMessageItem]
 *
 * @property id Id of the message
 * @property sender The message sender
 * @property userAlias The sender user alias
 * @property message The message text
 * @property time The time the message was sent
 * @property userAvatarId The local resource id for the user's avatar
 * @property userAvatarUrl The remote resource url for the user's avatar
 * @property isFirstPage True if it is the first page of the message, false otherwise
 * @constructor
 */
internal data class ChatMessageData(
    val id: String,
    val sender: String? = null,
    val userAlias: String? = null,
    val message: String? = null,
    val time: Long? = null,
    @DrawableRes val userAvatarId: Int? = null,
    val userAvatarUrl: String? = null,
    val isFirstPage: Boolean = true
)
