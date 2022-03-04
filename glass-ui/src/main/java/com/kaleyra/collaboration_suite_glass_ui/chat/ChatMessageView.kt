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

package com.kaleyra.collaboration_suite_glass_ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_glass_ui.databinding.BandyerGlassChatMessageLayoutBinding
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601

/**
 * ChatMessageView
 *
 * @constructor
 */
internal class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var binding: BandyerGlassChatMessageLayoutBinding =
        BandyerGlassChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Set the user avatar
     *
     * @param resId The local resource id
     */
    fun setAvatar(@DrawableRes resId: Int?) = binding.bandyerAvatar.setImage(resId)

    /**
     * Set the user avatar
     *
     * @param url String
     */
    fun setAvatar(url: String) = binding.bandyerAvatar.setImage(url)

    /**
     * Set the sender's name
     *
     * @param text The name
     */
    fun setName(text: String?) = with(binding) {
        bandyerName.text = text
        bandyerAvatar.setText(text?.first().toString())
    }

    /**
     * Show the sender's name
     */
    fun showName() {
        binding.bandyerName.visibility = VISIBLE
    }

    /**
     * Hide the sender's name
     */
    fun hideName() {
        binding.bandyerName.visibility = GONE
    }

    /**
     * Set the avatar background color
     *
     * @param color The color resource
     */
    fun setAvatarBackground(@ColorInt color: Int?) = binding.bandyerAvatar.setBackground(color)

    /**
     * Set the message text
     *
     * @param text The message
     */
    fun setMessage(text: String?) {
        binding.bandyerMessage.text = text
    }

    /**
     * Set the message time
     *
     * @param millis Time in millis
     */
    fun setTime(millis: Long?) {
        binding.bandyerTime.text = if(millis == null) null else Iso8601.parseTimestamp(context, millis)
    }
}