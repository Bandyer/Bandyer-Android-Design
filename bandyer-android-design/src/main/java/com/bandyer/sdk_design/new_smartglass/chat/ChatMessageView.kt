package com.bandyer.sdk_design.new_smartglass.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.sdk_design.extensions.parseToHHmm

class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatMessageLayoutBinding =
        BandyerChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun setAvatar(@DrawableRes resId: Int?) = binding.bandyerAvatar.setImage(resId)

    fun setName(text: String?) = with(binding) {
        bandyerName.text = text
        bandyerAvatar.setText(text?.first().toString())
    }

    fun setNameVisibility(visibility: Int) {
        binding.bandyerName.visibility = visibility
    }

    fun setAvatarBackground(@ColorInt color: Int?) = binding.bandyerAvatar.setBackground(color)

    fun setMessage(text: String?) {
        binding.bandyerMessage.text = text
    }

    fun setTime(millis: Long?) {
        binding.bandyerTime.text = millis?.parseToHHmm()
    }

    private fun setMessageCollapsed(isMessageCollapsed: Boolean) {
        binding.bandyerMessage.maxLines =
            if (isMessageCollapsed) COLLAPSED_MAX_LINES else Int.MAX_VALUE
    }

    private companion object {
        const val COLLAPSED_MAX_LINES = 2
    }
}