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

package com.kaleyra.collaboration_suite_core_ui

import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import kotlinx.coroutines.flow.StateFlow

/**
 * Call UI delegate
 */
interface ChatUIDelegate {
    /**
     * Call
     */
    val chats: StateFlow<List<ChatUI>>

    /**
     * Users description
     */
    val usersDescription: UsersDescription
}

/**
 * The chat delegate
 *
 * @constructor
 */
class ChatDelegate(
    override val chats: StateFlow<List<ChatUI>>,
    override val usersDescription: UsersDescription
) : ChatUIDelegate