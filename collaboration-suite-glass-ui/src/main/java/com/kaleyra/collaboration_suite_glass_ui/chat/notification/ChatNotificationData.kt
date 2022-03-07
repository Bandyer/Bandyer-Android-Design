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

package com.kaleyra.collaboration_suite_glass_ui.chat.notification

import androidx.annotation.DrawableRes

/**
 * NotificationData
 *
 * @property name The user name
 * @property userId The user identifier
 * @property message The message sent by the user
 * @property imageRes The local resource id to be set as user avatar
 * @property imageUrl The remote resource url to be set as user avatar
 * @constructor
 */
internal data class ChatNotificationData(
    val name: String,
    val userId: String,
    val message: String? = null,
    @DrawableRes val imageRes: Int? = null,
    val imageUrl: String? = null
)