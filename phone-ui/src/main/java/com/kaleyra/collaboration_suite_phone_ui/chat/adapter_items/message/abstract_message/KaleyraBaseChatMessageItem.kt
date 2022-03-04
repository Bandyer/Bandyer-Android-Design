/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.chat.adapter_items.message.abstract_message

import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Base Kaleyra Chat Message Item containing data of type T
 * @property data contained in the message
 * @constructor
 * @author kristiyan
 */
abstract class KaleyraBaseChatMessageItem<T : KaleyraBaseChatMessage>(var data: T) : AbstractItem<KaleyraBaseChatMessageViewHolder<*, *>>() {

    /**
     * @suppress
     */
    override val type = data.hashCode()
}