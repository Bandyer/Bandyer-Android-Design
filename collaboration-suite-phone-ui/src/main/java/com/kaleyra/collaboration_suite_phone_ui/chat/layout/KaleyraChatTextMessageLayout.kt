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

package com.kaleyra.collaboration_suite_phone_ui.chat.layout

import android.content.Context
import android.util.AttributeSet

import com.kaleyra.collaboration_suite_phone_ui.R
import com.google.android.material.textview.MaterialTextView

/**
 * Kaleyra Chat TextMessage
 * @property messageTextView KaleyraTextView?
 * @constructor
 */
class KaleyraChatTextMessageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : KaleyraBaseChatMessageLayout(context, attrs, defStyleAttr) {

    var messageTextView: MaterialTextView? = null
        private set

    init {
        dataView?.layoutResource = R.layout.kaleyra_chat_message_text_data
        messageTextView = dataView?.inflate() as? MaterialTextView
    }

}