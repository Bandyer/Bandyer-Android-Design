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

package com.bandyer.video_android_phone_ui.chat.layout

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_phone_ui.R
import com.google.android.material.textview.MaterialTextView

/**
 * Bandyer Chat TextMessage
 * @property messageTextView BandyerTextView?
 * @constructor
 */
class BandyerChatTextMessageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : BandyerBaseChatMessageLayout(context, attrs, defStyleAttr) {

    var messageTextView: MaterialTextView? = null
        private set

    init {
        dataView?.layoutResource = R.layout.bandyer_chat_message_text_data
        messageTextView = dataView?.inflate() as? MaterialTextView
    }

}