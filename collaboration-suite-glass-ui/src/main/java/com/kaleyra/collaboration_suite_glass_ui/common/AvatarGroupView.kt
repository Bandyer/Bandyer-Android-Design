/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.common

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.dp2px

/**
 * A view aggregator of [AvatarView]
 *
 * @constructor
 */
internal class AvatarGroupView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * Add an avatar
     *
     * @param imageResId The local resource for the avatar
     */
    fun addAvatar(@DrawableRes imageResId: Int) = addAvatar().setImage(imageResId)

    /**
     * Add an avatar
     *
     * @param uri The uri for the avatar
     */
    fun addAvatar(uri: Uri) = addAvatar().setImage(uri)

    /**
     * Add an avatar
     *
     * @param text The avatar text, usually a letter
     * @param color The background color
     */
    fun addAvatar(text: String, @ColorInt color: Int?) {
        addAvatar().also {
            it.setText(text)
            it.setBackground(color)
        }
    }

    private fun addAvatar(): AvatarView {
        val set = ConstraintSet()

        val elevation = - childCount
        val marginStart = if (childCount == 0) 0 else CHILD_START_MARGIN
        val startId = this.getChildAt(childCount - 1)?.id ?: this.id

        // Create the child view
        val child = AvatarView(context)
        child.id = generateViewId()
        child.clipToPadding = false
        child.elevation = context.dp2px(elevation.toFloat()).toFloat()
        this.addView(child, childCount)

        // Set the constraints
        set.clone(this)
        set.connect(
            child.id, ConstraintSet.START, startId, ConstraintSet.START, context.dp2px(
                marginStart.toFloat()
            )
        )
        set.connect(
            child.id, ConstraintSet.TOP, this.id, ConstraintSet.TOP, 0
        )
        set.applyTo(this)

        return child
    }

    fun clean() = removeAllViews()

    private companion object {
        // Gap between the left side of each avatar
        const val CHILD_START_MARGIN = 16
    }
}