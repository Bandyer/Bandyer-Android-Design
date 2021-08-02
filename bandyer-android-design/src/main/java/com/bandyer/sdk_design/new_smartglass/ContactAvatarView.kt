package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.bandyer.sdk_design.databinding.BandyerContactAvatarLayoutBinding
import com.bandyer.sdk_design.extensions.parseToColor
import java.util.*

class ContactAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: BandyerContactAvatarLayoutBinding =
        BandyerContactAvatarLayoutBinding.inflate(
            LayoutInflater.from(context), this, true
        )

    private val defaultAvatar = android.R.color.transparent
    private val defaultBackgroundColor = Color.GRAY

    fun setImage(@DrawableRes resId: Int?) {
        binding.bandyerAvatarImage.setImageResource(resId ?: defaultAvatar)
        binding.bandyerAvatarText.visibility = if (resId == null) VISIBLE else GONE
    }

    fun setText(text: String?) {
        binding.bandyerAvatarText.text = text?.toUpperCase(Locale.getDefault())
    }

    fun setBackground(@ColorInt color: Int?) = binding.bandyerAvatarImage.setBackgroundColor(
        color ?: defaultBackgroundColor
    )
}

