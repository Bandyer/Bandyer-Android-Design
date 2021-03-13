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

package com.bandyer.sdk_design.chat.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.call.imageviews.BandyerAvatarImageView
import com.bandyer.sdk_design.databinding.BandyerWidgetChatInfoBinding
import com.bandyer.sdk_design.extensions.setTextAppearance
import com.google.android.material.textview.MaterialTextView

/**
 * This class represent a widget used to display in-chat informations.
 * It has a tile, a subtitle and bouncing dots for typing action displaying.
 */
class BandyerChatInfoWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) : ConstraintLayout(context, attrs, defStyleAttr) {
    /**
     * View used to display contact names
     */
    var contactNameView: MaterialTextView? = null
        private set

    /**
     * View used to display contact chat status ( online, offline, typing )
     */
    var contactStatusView: MaterialTextView? = null
        private set

    /**
     * View used to display contact image
     */
    var contactImageView: BandyerAvatarImageView? = null
        private set

    /**
     * Change or get the current status of the contact
     */
    var state: BandyerChatInfoWidgetState? = BandyerChatInfoWidgetState.WAITING_FOR_NETWORK()
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            if (value !is BandyerChatInfoWidgetState.TYPING) {
                binding.bandyerSubtitleText.hideBouncingDots()
                showTyping(false)
            }
            when (value) {
                is BandyerChatInfoWidgetState.OFFLINE -> {
                    context.setTextAppearance(binding.bandyerSubtitleText, R.style.BandyerSDKDesign_TextAppearance_Subtitle_Chat_Offline)
                    binding.bandyerSubtitleText.text = resources.getString(R.string.bandyer_chat_user_status_offline)
                    if (value.lastLogin != null && !value.lastLogin.isNullOrBlank())
                        binding.bandyerSubtitleText.text = context.getString(R.string.bandyer_chat_user_status_last_login) + " " + value.lastLogin!!
                    showSubtitle(true)
                }
                is BandyerChatInfoWidgetState.ONLINE -> {
                    context.setTextAppearance(binding.bandyerSubtitleText, R.style.BandyerSDKDesign_TextAppearance_Subtitle_Chat_Online)
                    binding.bandyerSubtitleText.text = resources.getString(R.string.bandyer_chat_user_status_online)
                    showSubtitle(true)
                }
                is BandyerChatInfoWidgetState.TYPING -> {
                    if (!binding.bandyerSubtitleText.isShowingBouncingDots) binding.bandyerSubtitleText.showBouncingDots()
                    binding.bandyerSubtitleText.showBouncingDots()
                    context.setTextAppearance(binding.bandyerSubtitleText, R.style.BandyerSDKDesign_TextAppearance_Subtitle_Chat_Typing)
                    binding.bandyerSubtitleText.text = resources.getString(R.string.bandyer_chat_user_status_typing)
                    showSubtitle(true)
                    showTyping(true)
                }
                is BandyerChatInfoWidgetState.WAITING_FOR_NETWORK -> {
                    context.setTextAppearance(binding.bandyerSubtitleText, R.style.BandyerSDKDesign_TextAppearance_Subtitle_Chat_WaitingForNetwork)
                    binding.bandyerSubtitleText.text = resources.getString(R.string.bandyer_chat_state_waiting_for_network)
                    showSubtitle(true)
                }
                is BandyerChatInfoWidgetState.CONNECTING -> {
                    context.setTextAppearance(binding.bandyerSubtitleText, R.style.BandyerSDKDesign_TextAppearance_Subtitle_Chat_Connecting)
                    binding.bandyerSubtitleText.text = resources.getString(R.string.bandyer_chat_state_connecting)
                    showSubtitle(true)
                }
                else -> {
                    showSubtitle(false)
                }
            }
            field = value
        }

    private val binding: BandyerWidgetChatInfoBinding by lazy { BandyerWidgetChatInfoBinding.inflate(LayoutInflater.from(context), this) }

    private fun showSubtitle(visible: Boolean) {
        binding.bandyerSubtitleText.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showTyping(visible: Boolean) {
        binding.bandyerSubtitleText.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) binding.bandyerSubtitleText.showBouncingDots()
        else binding.bandyerSubtitleText.hideBouncingDots()
    }

    init {
        contactNameView = binding.bandyerTitle
        contactStatusView = binding.bandyerSubtitleText
        contactImageView = binding.bandyerAvatar
        binding.bandyerTitle.visibility = View.GONE
        binding.bandyerTitle.isSelected = true // activate marquee
        binding.bandyerSubtitleText?.isSelected = true // activate marquee
    }

    /**
     * Set the contact name
     * @param name the name to display
     */
    fun setName(name: String) {
        binding.bandyerTitle.text = name
    }

    /**
     * Display contact image given the url
     * @param url image
     */
    fun setImage(url: String) = binding.bandyerAvatar.setImageUrl(url)

    /**
     * Display contact image given a resource
     * @param resId image
     */
    fun setImage(@DrawableRes resId: Int) = binding.bandyerAvatar.setImageResource(resId)

    /**
     * Display contact image given a bitmap
     * @param bitmap image
     */
    fun setImage(bitmap: Bitmap) = binding.bandyerAvatar.setImageBitmap(bitmap)

    /**
     * Bandyer Chat Info Widget States
     */
    sealed class BandyerChatInfoWidgetState {

        /**
         * When the contact is online
         */
        class ONLINE : BandyerChatInfoWidgetState()

        /**
         * When the contact is offline
         * @property lastLogin last login description text
         */
        class OFFLINE(var lastLogin: String? = null) : BandyerChatInfoWidgetState()

        /**
         * When the contact is typing a message
         */
        class TYPING : BandyerChatInfoWidgetState()

        /**
         * When the contact is in an unknown state
         */
        @Suppress("ClassName")
        class WAITING_FOR_NETWORK : BandyerChatInfoWidgetState()

        /**
         * When the contact is online
         */
        class CONNECTING : BandyerChatInfoWidgetState()

        /**
         * When the contact is in an unknown state
         */
        class UNKNOWN : BandyerChatInfoWidgetState()

        /**
         * @suppress
         * @param other Any?
         * @return Boolean
         */
        override fun equals(other: Any?): Boolean {
            return this === other
        }

        /**
         * @suppress
         * @return Int
         */
        override fun hashCode(): Int {
            return System.identityHashCode(this)
        }
    }
}