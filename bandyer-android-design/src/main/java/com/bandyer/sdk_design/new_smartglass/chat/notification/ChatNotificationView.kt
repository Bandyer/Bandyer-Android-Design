package com.bandyer.sdk_design.new_smartglass.chat.notification

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.Slide
import androidx.transition.TransitionManager
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatNotificationLayoutBinding
import com.bandyer.sdk_design.extensions.animateViewHeight
import com.bandyer.sdk_design.extensions.parseToColor

/**
 * A chat notification view
 *
 * @constructor
 */
class ChatNotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: BandyerChatNotificationLayoutBinding =
        BandyerChatNotificationLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    fun show(data: List<NotificationData>) = with(binding) {
        if (data.size < 2) {
            bandyerTitle.text = data[0].name
            bandyerMessage.text = data[0].message
        } else {
            bandyerTitle.text = resources.getString(R.string.bandyer_smartglass_new_messages_pattern, data.size)
            bandyerMessage.maxLines = 0
            bandyerTime.visibility = View.GONE
        }

        data.forEachIndexed { index, item ->
            if (index > 1) return@forEachIndexed

            with(bandyerAvatars) {
                if (item.imageRes != null) addAvatar(item.imageRes)
                else addAvatar(
                    item.name.first().toUpperCase().toString(),
                    item.userAlias.parseToColor()
                )
            }
        }

        val distinctUsers = data.distinctBy { it.userAlias }.size
        if (distinctUsers > 2)
            bandyerAvatars.addAvatar(
                resources.getString(
                    R.string.bandyer_smartglass_group_contacts_pattern,
                    distinctUsers
                ),
                null
            )

        setVisibility(true, View.VISIBLE)
    }

    fun hide(withAnimation: Boolean = true) = setVisibility(withAnimation, View.GONE)

    fun expand(onExpanded: ((Animator) -> Unit)? = null) {
        setVisibility(true, View.VISIBLE)
        binding.root.animateViewHeight(
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