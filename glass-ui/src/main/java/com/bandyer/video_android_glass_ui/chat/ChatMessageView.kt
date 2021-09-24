package com.bandyer.video_android_glass_ui.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.video_android_glass_ui.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.video_android_core_ui.utils.Iso8601

/**
 * ChatMessageView
 *
 * @constructor
 */
class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatMessageLayoutBinding =
        BandyerChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

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