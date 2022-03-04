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

package com.bandyer.video_android_phone_ui.chat.layout

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.video_android_phone_ui.R
import com.bandyer.video_android_phone_ui.chat.imageviews.BandyerChatMessageStatusImageView
import com.google.android.material.textview.MaterialTextView

/**
 * Base Chat Message Layout
 * @constructor
 */
abstract class BandyerBaseChatMessageLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * TimeStamp view that displays when the message has been sent
     */
    var timestampTextView: MaterialTextView? = null
        private set

    /**
     * Message Status view which represents if a message is pending, sent or seen
     */
    var statusView: BandyerChatMessageStatusImageView? = null
        private set

    /**
     * Container of the dataView
     */
    var dataViewContainer: View? = null
        private set
    /**
     * View containing the data of the message for example imageView, textView or other types of layout may be used
     */
    protected var dataView: ViewStub? = null
        private set

    init {
        LayoutInflater.from(context).inflate(R.layout.bandyer_chat_message_base, this, true)
        this.id = R.id.bandyer_id_base_chat_message_layout
        this.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        dataViewContainer = findViewById(R.id.bandyer_data_view_container)
        statusView = findViewById(R.id.bandyer_status_view)
        timestampTextView = findViewById(R.id.bandyer_timestamp_view)
        dataView = findViewById(R.id.bandyer_data_view)
    }

    /**
     * Display status
     * @param show true to display, false otherwise
     */
    fun showStatus(show: Boolean) {
        statusView?.visibility = if (show) View.VISIBLE else View.GONE
    }


    /**
     * @suppress
     */
    override fun clearFocus() {
        if (parent != null) super.clearFocus()
    }
}
