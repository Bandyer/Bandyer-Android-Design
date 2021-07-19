package com.bandyer.sdk_design.new_smartglass.chat

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.google.android.material.color.MaterialColors

class ChatMessageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatMessageLayoutBinding =
        BandyerChatMessageLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val readLineColor = Color.WHITE
    private val unreadLineColor = MaterialColors.getColor(context, R.attr.colorSecondary, Color.BLACK)

    init {
        context.obtainStyledAttributes(attrs, R.styleable.ChatMessageView).apply {
            setMessageText(getString(R.styleable.ChatMessageView_messageText))
            setMessageRead(getBoolean(R.styleable.ChatMessageView_messageRead, false))
            setMessageCollapsed(getBoolean(R.styleable.ChatMessageView_messageCollapsed, false))
            recycle()
        }
    }

    fun setMessageText(text: String?) {
        binding.message.text = text
    }

    fun setMessageRead(isMessageRead: Boolean) {
        binding.line.setBackgroundColor(if (isMessageRead) readLineColor else unreadLineColor)
    }

    private fun setMessageCollapsed(isMessageCollapsed: Boolean) {
        binding.message.maxLines = if (isMessageCollapsed) COLLAPSED_MAX_LINES else Int.MAX_VALUE
        binding.message.ellipsize =
            if (isMessageCollapsed) TextUtils.TruncateAt.END else TextUtils.TruncateAt.MARQUEE
    }

    private companion object {
        const val COLLAPSED_MAX_LINES = 2
    }
}