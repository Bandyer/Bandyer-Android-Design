package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerContactAvatarLayoutBinding
import com.bandyer.sdk_design.new_smartglass.utils.parseToColor
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*
import kotlin.random.Random

class ContactAvatarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding: BandyerContactAvatarLayoutBinding =
        BandyerContactAvatarLayoutBinding.inflate(
            LayoutInflater.from(context), this, true
        )

    private var text: String? = null
    private val defaultAvatar = R.drawable.ic_bandyer_avatar
    private val defaultBackgroundColor = Color.GRAY

    fun setAvatar(@DrawableRes resId: Int?) {
        binding.bandyerAvatarText.visibility = if (resId == null && text == null) VISIBLE else GONE
        binding.bandyerAvatarImage.setImageResource(resId ?: defaultAvatar)
    }
    
    fun setText(text: String?) {
        this.text = text
        binding.bandyerAvatarText.text = text?.toUpperCase(Locale.getDefault())
    }

    fun setBackground(text: String?) = binding.bandyerAvatarImage.setBackgroundColor(text?.parseToColor() ?: defaultBackgroundColor)
}

