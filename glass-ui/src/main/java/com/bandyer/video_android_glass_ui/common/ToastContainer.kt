package com.bandyer.video_android_glass_ui.common

import android.content.Context
import android.util.AttributeSet
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import com.bandyer.video_android_core_ui.extensions.ContextExtensions.getThemeAttribute
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassToastLayoutBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getCallThemeAttribute
import java.util.*

class ToastContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val theme = context.getThemeAttribute(
        context.getCallThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Glass_Call_bandyer_toastContainerStyle),
        R.styleable.BandyerSDKDesign_Theme_GlassCall_ToastContainerStyle,
        R.styleable.BandyerSDKDesign_Theme_GlassCall_ToastContainerStyle_bandyer_toastStyle
    )

    /**
     * It shows a toast. The default toast duration is 3000 ms, if it is set to 0 the toast is showed until manually cancelled.
     *
     * @param id The toast id
     * @param text The toast's text
     * @param icon The toast's icon
     * @param duration The toast duration
     * @return String The id of the toast
     */
    fun show(
        id: String = UUID.randomUUID().toString(),
        text: String,
        @DrawableRes icon: Int? = null,
        duration: Long = 3000L
    ): String {
        cancel(id)
        Toast(ContextThemeWrapper(context, theme)).apply {
            tag = id
            setText(text)
            setIcon(icon)
            this@ToastContainer.addView(this, 0)

            if (duration == 0L) return@apply
            postDelayed({ this@ToastContainer.removeView(this) }, duration)
        }
        return id
    }

    /**
     * Cancel the toast with the given id
     *
     * @param id String
     */
    fun cancel(id: String) { findViewWithTag<Toast>(id)?.also { removeView(it) } }

    /**
     * Clear all the toasts
     */
    fun clear() = removeAllViews()

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