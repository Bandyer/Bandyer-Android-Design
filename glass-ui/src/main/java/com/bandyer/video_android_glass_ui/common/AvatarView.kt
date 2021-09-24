package com.bandyer.video_android_glass_ui.common

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import com.bandyer.video_android_core_ui.extensions.ColorIntExtensions.requiresLightColor
import com.bandyer.video_android_glass_ui.databinding.BandyerContactAvatarLayoutBinding
import com.squareup.picasso.Picasso
import java.util.*

/**
 * The participant avatar view.
 *
 * @constructor
 */
class AvatarView @JvmOverloads constructor(
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

    /**
     * Set a local resource as avatar. The background and the text became no longer visible.
     *
     * @param resId The resource id. If null, any avatar resource previously set (either local or remote) is removed and the background color and the text became visible again.
     */
    fun setImage(@DrawableRes resId: Int?) = with(binding) {
        bandyerAvatarImage.setImageResource(resId ?: defaultAvatar)
        bandyerAvatarText.visibility = if (resId == null) VISIBLE else GONE
    }

    /**
     * Set a remote url resource as avatar. The background and the text became no longer visible.
     *
     * @param url The avatar's url
     */
    fun setImage(url: String) = with(binding) {
        Picasso.get().load(url).placeholder(defaultAvatar).error(defaultAvatar).into(bandyerAvatarImage)
        bandyerAvatarText.visibility = GONE
    }

    /**
     * Set the avatar text
     *
     * @param text String?
     */
    fun setText(text: String?) {
        binding.bandyerAvatarText.text = text?.uppercase(Locale.getDefault())
    }

    /**
     * Set the avatar background color. The avatar text adjust it's color on the defined value.
     *
     * @param color The color int resource. If null Color.GRAY is set as default.
     */
    fun setBackground(@ColorInt color: Int?) = with(binding) {
        bandyerAvatarImage.setBackgroundColor(
            color ?: defaultBackgroundColor
        )
        bandyerAvatarText.setTextColor(if (color?.requiresLightColor() == true) Color.WHITE else Color.BLACK)
    }
}

