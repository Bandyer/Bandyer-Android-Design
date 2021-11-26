package com.bandyer.video_android_glass_ui

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getThemeAttribute
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassToastLayoutBinding

class ToastContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private companion object {
        const val MAX_CHILD_COUNT = 3
    }

    private val containerTheme = context.getThemeAttribute(R.style.BandyerSDKDesign_Theme_GlassCall, R.styleable.BandyerSDKDesign_Theme_Glass_Call, R.styleable.BandyerSDKDesign_Theme_Glass_Call_bandyer_toastContainerStyle)
    private val toastTheme = context.getThemeAttribute(containerTheme, R.styleable.BandyerSDKDesign_Theme_GlassCall_ToastContainerStyle, R.styleable.BandyerSDKDesign_Theme_GlassCall_ToastContainerStyle_bandyer_toastStyle)

    fun show(text: String, @DrawableRes icon: Int? = null, duration: Long = 3000L) {
        if(childCount >= MAX_CHILD_COUNT) this.removeViewAt(0)
        Toast(ContextThemeWrapper(context, toastTheme)).apply {
            setText(text)
            setIcon(icon)
            this@ToastContainer.addView(this)
            postDelayed({ this@ToastContainer.removeView(this) }, duration)
        }
    }

    private class Toast @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : LinearLayout(context, attrs, defStyleAttr)  {

        private val binding = BandyerGlassToastLayoutBinding.inflate(LayoutInflater.from(context), this, true)

        fun setIcon(@DrawableRes resId: Int? = null) = with(binding.bandyerIcon) {
            resId?.also { setImageResource(it) } ?: kotlin.run { visibility = View.GONE }
        }

        fun setText(text: String) { binding.bandyerText.text = text }
    }
}