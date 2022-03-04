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

package com.bandyer.video_android_glass_ui.chat.notification

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

/**
 * A ChatNotificationViewAttacher is used to attach a view to a layout
 *
 * @property layout ViewGroup
 * @property view View
 */
internal interface ChatNotificationViewAttacher {

    companion object Factory {
        /**
         * Create an instance of a [ChatNotificationViewAttacher]
         *
         * @param layout The layout to which attach the view
         * @param view The view to attach
         * @return ChatNotificationViewAttacher
         */
        fun create(layout: ViewGroup, view: View): ChatNotificationViewAttacher {
            view.apply {
                id = View.generateViewId()
                visibility = View.GONE
            }
            return when (layout) {
                is ConstraintLayout -> ChatNotificationConstraintAttacher(layout, view)
                is FrameLayout      -> ChatNotificationFrameAttacher(layout, view)
                is RelativeLayout   -> ChatNotificationRelativeAttacher(layout, view)
                else                -> throw IllegalArgumentException("Unsupported layout type")
            }
        }
    }

    /**
     * The view group to which attach the view
     */
    val layout: ViewGroup

    /**
     * The view to attach
     */
    val view: View

    /**
     * Attach the [view] to the [layout]
     */
    fun attach()

    /**
     *  Detach the [view] from the previously passed layout
     */
    fun detach() = layout.removeView(view)
}

/**
 * ChatNotificationConstraintAttacher
 */
internal class ChatNotificationConstraintAttacher(
    override val layout: ConstraintLayout,
    override val view: View
) : ChatNotificationViewAttacher {

    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        layout.addView(view)

        ConstraintSet().apply {
            clone(layout)
            connect(view.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            connect(view.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
            connect(view.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
            constrainWidth(view.id, ConstraintSet.MATCH_CONSTRAINT)
            applyTo(layout)
        }
    }
}

/**
 * ChatNotificationFrameAttacher
 */
private class ChatNotificationFrameAttacher(
    override val layout: FrameLayout,
    override val view: View
) : ChatNotificationViewAttacher {

    /**
     * Attach the [view] to a [FrameLayout]
     */
    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT,
            Gravity.TOP
        )
        layout.addView(view, params)
    }
}

/**
 * ChatNotificationRelativeAttacher
 */
private class ChatNotificationRelativeAttacher(
    override val layout: RelativeLayout,
    override val view: View
) : ChatNotificationViewAttacher {

    override fun attach() {
        if (layout.findViewById<View>(view.id) != null) return
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        ).apply { addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE) }
        layout.addView(view, params)
    }
}