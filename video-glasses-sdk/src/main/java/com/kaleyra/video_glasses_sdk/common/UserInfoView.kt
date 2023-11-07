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

package com.kaleyra.video_glasses_sdk.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.kaleyra.video.conversation.ChatParticipant
import com.kaleyra.video_common_ui.utils.extensions.StringExtensions.parseToColor
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassUserInfoLayoutBinding

internal class UserInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    val binding = KaleyraGlassUserInfoLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun setName(name: String) { binding.kaleyraName.text = name }

    fun setAvatarBackgroundAndLetter(name: String) = with(binding) {
        kaleyraAvatar.setText(name.first().toString())
        kaleyraAvatar.setBackground(name.parseToColor())
    }

    fun setAvatar(uri: Uri) = binding.kaleyraAvatar.setImage(uri)

    fun setAvatar(@DrawableRes resId: Int?) = binding.kaleyraAvatar.setImage(resId)

    fun setState(state: ChatParticipant.State) = with(binding) {
        kaleyraUserStateText.setUserState(state)
        kaleyraUserStateDot.isActivated = state == ChatParticipant.State.Joined.Online
    }

    fun hideName(value: Boolean) { binding.kaleyraName.visibility = if (value) View.GONE else View.VISIBLE }

    fun hideState(value: Boolean) {
        val visibility = if (value) View.GONE else View.VISIBLE
        binding.kaleyraUserStateText.visibility = visibility
        binding.kaleyraUserStateDot.visibility = visibility
    }
}