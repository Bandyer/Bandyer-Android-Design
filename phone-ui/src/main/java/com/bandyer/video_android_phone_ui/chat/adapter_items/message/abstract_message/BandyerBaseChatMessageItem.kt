/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.chat.adapter_items.message.abstract_message

import com.mikepenz.fastadapter.items.AbstractItem

/**
 * Base Bandyer Chat Message Item containing data of type T
 * @property data contained in the message
 * @constructor
 * @author kristiyan
 */
abstract class BandyerBaseChatMessageItem<T : BandyerBaseChatMessage>(var data: T) : AbstractItem<BandyerBaseChatMessageViewHolder<*, *>>() {

    /**
     * @suppress
     */
    override val type = data.hashCode()
}