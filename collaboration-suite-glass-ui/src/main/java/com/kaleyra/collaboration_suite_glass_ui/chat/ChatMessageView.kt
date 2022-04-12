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

import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassChatMessageLayoutBinding
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

    var binding: KaleyraGlassChatMessageLayoutBinding =
        KaleyraGlassChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Set the user avatar
     *
     * @param resId The local resource id
     */
    fun setAvatar(@DrawableRes resId: Int?) = binding.kaleyraAvatar.setImage(resId)

    /**
     * Set the user avatar
     *
     * @param url String
     */
    fun setAvatar(url: String) = binding.kaleyraAvatar.setImage(url)

    /**
     * Set the sender's name
     *
     * @param text The name
     */
    fun setName(text: String?) = with(binding) {
        kaleyraName.text = text
    }

    /**
     * Show the sender's name
     */
    fun showName() {
        binding.kaleyraName.visibility = VISIBLE
    }

    /**
     * Hide the sender's name
     */
    fun hideName() {
        binding.kaleyraName.visibility = GONE
    }

    /**
     * Set the avatar background color
     *
     * @param color The color resource
     */
    fun setAvatarBackground(@ColorInt color: Int?) = binding.kaleyraAvatar.setBackground(color)

    /**
     * Set the avatar text
     *
     * @param text String?
     */
    fun setAvatarText(text: String?) = binding.kaleyraAvatar.setText(text?.first().toString())

    /**
     * Set the message text
     *
     * @param text The message
     */
    fun setMessage(text: String?) {
        binding.kaleyraMessage.text = text
    }

    /**
     * Set the message time
     *
     * @param millis Time in millis
     */
    fun setTime(millis: Long?) {
        binding.kaleyraTime.text = if(millis == null) null else Iso8601.parseTimestamp(context, millis)
    }
}