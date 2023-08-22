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

package com.kaleyra.collaboration_suite_phone_ui.chat.widgets

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.call.imageviews.KaleyraAvatarImageView
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraWidgetChatInfoBinding
import com.kaleyra.collaboration_suite_phone_ui.extensions.setTextAppearance
import com.kaleyra.collaboration_suite_phone_ui.textviews.KaleyraTextViewBouncingDots
import com.google.android.material.textview.MaterialTextView

/**
 * This class represent a widget used to display in-chat informations.
 * It has a tile, a subtitle and bouncing dots for typing action displaying.
 */
class KaleyraChatInfoWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle)
    : ConstraintLayout(context, attrs, defStyleAttr) {
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
    var contactImageView: KaleyraAvatarImageView? = null
        private set

    /**
     * View effect used in typing status
     */
    var typingDotsView: KaleyraTextViewBouncingDots? = null
        private set

    /**
     * Change or get the current status of the contact
     */
    var state: KaleyraChatInfoWidgetState? = KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK()
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            if (value !is KaleyraChatInfoWidgetState.TYPING) {
                typingDotsView?.hideAndStop()
                showTyping(false)
            }
            contactStatusView ?: return
            when (value) {
                is KaleyraChatInfoWidgetState.OFFLINE -> {
                    context.setTextAppearance(binding.kaleyraSubtitleText, R.style.KaleyraCollaborationSuiteUI_TextAppearance_Subtitle_Chat_Offline)
                    binding.kaleyraSubtitleText.text = resources.getString(R.string.kaleyra_chat_user_status_offline)
                    if (value.lastLogin != null && !value.lastLogin.isNullOrBlank())
                        binding.kaleyraSubtitleText.text = context.getString(R.string.kaleyra_chat_user_status_last_login) + " " + value.lastLogin!!
                    showSubtitle(true)
                }
                is KaleyraChatInfoWidgetState.ONLINE -> {
                    context.setTextAppearance(binding.kaleyraSubtitleText, R.style.KaleyraCollaborationSuiteUI_TextAppearance_Subtitle_Chat_Online)
                    binding.kaleyraSubtitleText.text = resources.getString(R.string.kaleyra_chat_user_status_online)
                    showSubtitle(true)
                }
                is KaleyraChatInfoWidgetState.TYPING -> {
                    if (typingDotsView?.isPlaying == false) typingDotsView?.showAndPlay()
                    context.setTextAppearance(binding.kaleyraSubtitleText, R.style.KaleyraCollaborationSuiteUI_TextAppearance_Subtitle_Chat_Typing)
                    binding.kaleyraSubtitleText.text = resources.getString(R.string.kaleyra_chat_user_status_typing)
                    showSubtitle(true)
                    showTyping(true)
                }
                is KaleyraChatInfoWidgetState.WAITING_FOR_NETWORK -> {
                    context.setTextAppearance(binding.kaleyraSubtitleText, R.style.KaleyraCollaborationSuiteUI_TextAppearance_Subtitle_Chat_WaitingForNetwork)
                    binding.kaleyraSubtitleText.text = resources.getString(R.string.kaleyra_chat_state_waiting_for_network)
                    showSubtitle(true)
                }
                is KaleyraChatInfoWidgetState.CONNECTING -> {
                    context.setTextAppearance(binding.kaleyraSubtitleText, R.style.KaleyraCollaborationSuiteUI_TextAppearance_Subtitle_Chat_Connecting)
                    binding.kaleyraSubtitleText.text = resources.getString(R.string.kaleyra_chat_state_connecting)
                    showSubtitle(true)
                }
                else -> {
                    showSubtitle(false)
                }
            }
            field = value
        }

    private val binding: KaleyraWidgetChatInfoBinding by lazy { KaleyraWidgetChatInfoBinding.inflate(LayoutInflater.from(context), this) }

    private fun showSubtitle(visible: Boolean) {
        binding.kaleyraSubtitleText.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun showTyping(visible: Boolean) {
        binding.kaleyraSubtitleBouncingDots.visibility = if (visible) View.VISIBLE else View.GONE
    }

    init {
        contactNameView = binding.kaleyraTitle
        contactNameView?.visibility = View.GONE
        contactStatusView = binding.kaleyraSubtitleText
        contactImageView = binding.kaleyraAvatar
        typingDotsView = binding.kaleyraSubtitleBouncingDots

        contactNameView?.isSelected = true // activate marquee
        contactStatusView?.isSelected = true // activate marquee
    }

    /**
     * Set the contact name
     * @param name the name to display
     */
    fun setName(name: String) {
        contactNameView?.text = name
    }

    /**
     * Display contact image given the url
     * @param url image
     */
    fun setImage(url: String) {
        contactImageView?.setImageUrl(url)
    }

    /**
     * Display contact image given a resource
     * @param resId image
     */
    fun setImage(@DrawableRes resId: Int) {
        contactImageView?.setImageResource(resId)
    }

    /**
     * Display contact image given a bitmap
     * @param bitmap image
     */
    fun setImage(bitmap: Bitmap) {
        contactImageView?.setImageBitmap(bitmap)
    }

    /**
     * Kaleyra Chat Info Widget States
     */
    sealed class KaleyraChatInfoWidgetState {

        /**
         * When the contact is online
         */
        class ONLINE : KaleyraChatInfoWidgetState()

        /**
         * When the contact is offline
         * @property lastLogin last login description text
         */
        class OFFLINE(var lastLogin: String? = null) : KaleyraChatInfoWidgetState()

        /**
         * When the contact is typing a message
         */
        class TYPING : KaleyraChatInfoWidgetState()

        /**
         * When the contact is in an unknown state
         */
        @Suppress("ClassName")
        class WAITING_FOR_NETWORK : KaleyraChatInfoWidgetState()

        /**
         * When the contact is online
         */
        class CONNECTING : KaleyraChatInfoWidgetState()

        /**
         * When the contact is in an unknown state
         */
        class UNKNOWN : KaleyraChatInfoWidgetState()

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