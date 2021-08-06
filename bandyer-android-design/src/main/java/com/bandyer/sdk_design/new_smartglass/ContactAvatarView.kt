package com.bandyer.sdk_design.new_smartglass

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.bandyer.sdk_design.databinding.BandyerContactAvatarLayoutBinding
import com.bandyer.sdk_design.extensions.requiresLightColor
import com.squareup.picasso.Picasso
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

    fun setImage(@DrawableRes resId: Int?) = with(binding) {
        bandyerAvatarImage.setImageResource(resId ?: defaultAvatar)
        bandyerAvatarText.visibility = if (resId == null) VISIBLE else GONE
    }

    fun setImage(url: String) = with(binding) {
        Picasso.get().load(url).placeholder(defaultAvatar).error(defaultAvatar).into(bandyerAvatarImage)
        bandyerAvatarText.visibility = GONE
    }

    fun setText(text: String?) {
        binding.bandyerAvatarText.text = text?.toUpperCase(Locale.getDefault())
    }

    fun setBackground(@ColorInt color: Int?) = with(binding) {
        bandyerAvatarImage.setBackgroundColor(
            color ?: defaultBackgroundColor
        )
        bandyerAvatarText.setTextColor(if (color?.requiresLightColor() == true) Color.WHITE else Color.BLACK)
    }
}

