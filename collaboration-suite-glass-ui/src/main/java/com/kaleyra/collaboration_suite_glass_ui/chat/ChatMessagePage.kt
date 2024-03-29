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

package com.kaleyra.collaboration_suite_glass_ui.chat

import android.net.Uri

/**
 * ChatMessagePage
 *
 * @property messageId Id of the message
 * @property sender The message sender
 * @property userId The sender user identifier
 * @property message The message text
 * @property time The time the message was sent
 * @property avatar The uri resource for the user's avatar
 * @property isFirstPage True if it is the first page of the message, false otherwise
 * @constructor
 */
internal data class ChatMessagePage(
    val messageId: String,
    val userId: String,
    val sender: String,
    val avatar: Uri,
    val message: String,
    val time: Long,
    val isFirstPage: Boolean
)
