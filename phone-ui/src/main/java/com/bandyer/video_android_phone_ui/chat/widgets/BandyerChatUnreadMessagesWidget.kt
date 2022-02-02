/*
 * Copyright 2021-2022 Bandyer @ https://www.bandyer.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bandyer.video_android_phone_ui.chat.widgets

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewAnimationUtils
import com.bandyer.video_android_phone_ui.R
import com.google.android.material.button.MaterialButton
import kotlin.math.hypot

/**
 * @suppress
 */
class BandyerChatUnreadMessagesWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_chatUnreadMessagesWidgetStyle): MaterialButton(context, attrs, defStyleAttr) {

    var callback: BandyerChatUnreadMessagesScrollDownClickedListener? = null

    private var counter = 0
    private var styleIconPadding = 0

    init {
        setOnClickListener {
            hide()
            callback?.onScrollDownClicked()
        }
        styleIconPadding = iconPadding
        updateText(counter)
    }

    fun incrementUnreadMessages(count: Int = 1) {
        if(counter > 99) return
        counter += count
        updateText(counter)
        show()
    }

    private fun updateText(counter: Int) {
        text = when {
            counter <= 0 -> ""
            counter < 99 -> "$counter"
            else -> "99+"
        }
        updateIconPadding(counter)
    }

    private fun updateIconPadding(counter: Int) {
        iconPadding = when {
            counter <= 0 -> 0
            else -> styleIconPadding
        }
    }

    fun updatePosition(position: Int) {
        if (position == 0) {
            hide()
            counter = 0
        } else  if (position < counter) --counter
        updateText(counter)
    }

    fun show() = circularVisibilityAnimation(View.VISIBLE)

    fun hide() = circularVisibilityAnimation(View.INVISIBLE)

    private fun circularVisibilityAnimation(vis: Int, duration: Long = 100) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP) this.visibility = vis
        else post {
            if(visibility == vis) return@post

            val cx = width / 2
            val cy = height / 2
            val radius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

            val anim = if(vis == View.VISIBLE) ViewAnimationUtils.createCircularReveal(this, cx, cy, 0f, radius)
            else ViewAnimationUtils.createCircularReveal(this, cx, cy, radius, 0f)

            anim.duration = duration

            anim.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    visibility = vis
                }
            })

            anim.start()
        }
    }
}

/**
 * @suppress
 */
interface BandyerChatUnreadMessagesScrollDownClickedListener {
    fun onScrollDownClicked()
}