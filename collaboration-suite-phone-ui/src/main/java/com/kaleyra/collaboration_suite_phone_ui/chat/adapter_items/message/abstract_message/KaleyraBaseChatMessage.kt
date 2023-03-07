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

package com.kaleyra.collaboration_suite_phone_ui.chat.adapter_items.message.abstract_message

/**
 * Base Model for a Chat Message
 * @property style message style to apply
 * @property mine true if the message is mine, false otherwise
 * @property timestamp timestamp of the message
 * @property pending true if the message is still pending, false otherwise
 * @property sent true if the message has been sent, false otherwise
 * @property seen Function0<Boolean> return true if has been seen, false otherwise
 */
interface KaleyraBaseChatMessage {
    val style: Int?
    val mine: Boolean
    val timestamp: Long
    val pending: Boolean
    val sent: Boolean
    val seen: () -> Boolean
}