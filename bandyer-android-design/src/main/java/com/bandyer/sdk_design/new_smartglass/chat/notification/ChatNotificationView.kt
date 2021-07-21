package com.bandyer.sdk_design.new_smartglass.chat.notification

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bandyer.sdk_design.databinding.BandyerChatNotificationLayoutBinding
import com.bandyer.sdk_design.extensions.animateViewHeight

/**
 * A chat notification view
 *
 * @constructor
 */
class ChatNotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatNotificationLayoutBinding =
        BandyerChatNotificationLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun show(text: String) {
        binding.message.setMessageText(text)
        setVisibility(true, View.VISIBLE)
    }

    fun hide(withAnimation: Boolean = true) = setVisibility(withAnimation, View.GONE)

    fun expand(onExpanded: ((Animator) -> Unit)? = null) {
        setVisibility(true, View.VISIBLE)
        binding.message.animateViewHeight(
            height,
            (parent as ViewGroup).height,
            ANIMATION_DURATION,
            AccelerateDecelerateInterpolator()
        ) { onExpanded?.invoke(it) }
    }

    private fun setVisibility(withAnimation: Boolean, visibility: Int) {
        if (withAnimation) setVisibilityWithAnimation(visibility)
        else this.visibility = visibility
    }

    private fun setVisibilityWithAnimation(visibility: Int) {
        TransitionManager.beginDelayedTransition(
            parent as ViewGroup,
            Slide(Gravity.TOP).apply { duration = ANIMATION_DURATION })
        this.visibility = visibility
    }

    companion object {
        private const val ANIMATION_DURATION = 300L
    }
}