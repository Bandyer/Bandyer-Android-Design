package com.bandyer.sdk_design.new_smartglass.chat

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.sdk_design.extensions.parseToHHmm

class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatMessageLayoutBinding =
        BandyerChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ChatMessageView).apply {
            setName(getString(R.styleable.ChatMessageView_bandyer_nameText))
            binding.bandyerTime.text = getString(R.styleable.ChatMessageView_bandyer_timeText)
            setMessage(getString(R.styleable.ChatMessageView_bandyer_messageText))
            setMessageCollapsed(
                getBoolean(
                    R.styleable.ChatMessageView_bandyer_messageCollapsed,
                    false
                )
            )
            setAvatar(
                getResourceId(
                    R.styleable.ChatMessageView_bandyer_avatarSrc,
                    android.R.color.transparent
                )
            )
            recycle()
        }
    }

    fun setAvatar(@DrawableRes resId: Int?) = binding.bandyerAvatar.setImage(resId)

    fun setName(text: String?) {
        binding.bandyerName.text = text
        binding.bandyerAvatar.setText(text?.first().toString())
    }

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