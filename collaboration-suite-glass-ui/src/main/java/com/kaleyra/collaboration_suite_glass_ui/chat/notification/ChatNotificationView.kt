/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.chat.notification

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager

import com.kaleyra.collaboration_suite_core_ui.extensions.StringExtensions.parseToColor
import com.kaleyra.collaboration_suite_core_ui.extensions.ViewExtensions.animateViewHeight
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassChatNotificationLayoutBinding

/**
 * A chat notification view
 *
 * @constructor
 */
internal class ChatNotificationView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var binding: KaleyraGlassChatNotificationLayoutBinding =
        KaleyraGlassChatNotificationLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Show the notification
     *
     * @param data List<NotificationData>
     */
    fun show(data: List<ChatNotificationData>) = with(binding) {
        if (data.size < 2) {
            kaleyraTitle.text = data[0].name
            kaleyraMessage.text = data[0].message
            kaleyraMessage.maxLines = 2
            kaleyraTime.visibility = View.VISIBLE
        } else {
            kaleyraTitle.text = resources.getString(R.string.kaleyra_glass_new_messages_pattern, data.size)
            kaleyraMessage.text = null
            kaleyraMessage.maxLines = 0
            kaleyraTime.visibility = View.GONE
        }

        kaleyraAvatars.clean()
        data.forEachIndexed { index, item ->
            if (index > 1) return@forEachIndexed

            with(kaleyraAvatars) {
                addAvatar(
                    item.name.first().uppercase(),
                    item.userAlias.parseToColor()
                )
                if (item.imageRes != null) addAvatar(item.imageRes)
                else if (item.imageUrl != null) addAvatar(item.imageUrl)
            }
        }

        val distinctUsers = data.distinctBy { it.userAlias }.size
        if (distinctUsers > 2)
            kaleyraAvatars.addAvatar(
                resources.getString(
                    R.string.kaleyra_glass_group_contacts_pattern,
                    distinctUsers
                ),
                null
            )

        setVisibility(true, View.VISIBLE)
    }

    /**
     * Hide the view, either with or without slide to top animation
     *
     * @param withAnimation True to perform the hide animation, false otherwise
     */
    fun hide(withAnimation: Boolean = true, onHidden: (() -> Unit)? = null) = setVisibility(withAnimation, View.GONE, onHidden)

    /**
     * Expand the view from its current height to the parent's height
     *
     * @param onExpanded Callback to be executed on end of the animation
     */
    fun expand(onExpanded: ((Animator) -> Unit)? = null) {
        setVisibility(true, View.VISIBLE)
        binding.root.animateViewHeight(
            height,
            (parent as ViewGroup).height,
            ANIMATION_DURATION,
            AccelerateDecelerateInterpolator()
        ) { onExpanded?.invoke(it) }
    }

    /**
     * Set the navigation bar's [View.OnClickListener] for both the swipe down and tap actions. Needed for realwear voice commands.
     *
     * @param swipeDownCallback Function0<Unit>
     * @param tapCallback Function0<Unit>
     */
    fun setNavigationBarOnClickListeners(
        swipeDownCallback: () -> Unit,
        tapCallback: () -> Unit
    ) = with(binding.kaleyraBottomNavigation) {
        setSwipeDownOnClickListener(swipeDownCallback)
        setTapOnClickListener(tapCallback)
    }

    private fun setVisibility(withAnimation: Boolean, visibility: Int, doOnEnd: (() -> Unit)? = null) {
        if (withAnimation) setVisibilityWithAnimation(visibility, doOnEnd)
        else this.visibility = visibility
    }

    private fun setVisibilityWithAnimation(visibility: Int, doOnEnd: (() -> Unit)?) {
        TransitionManager.beginDelayedTransition(
            parent as ViewGroup,
            Slide(Gravity.TOP).apply { duration = ANIMATION_DURATION }
                .addListener(object: Transition.TransitionListener {
                    override fun onTransitionStart(transition: Transition) = Unit

                    override fun onTransitionEnd(transition: Transition) {
                        doOnEnd?.invoke()
                    }

                    override fun onTransitionCancel(transition: Transition) = Unit

                    override fun onTransitionPause(transition: Transition) = Unit

                    override fun onTransitionResume(transition: Transition) = Unit
                })
        )
        this.visibility = visibility
    }

    private companion object {
        // Duration of the show/hide animations
        const val ANIMATION_DURATION = 300L
    }
}