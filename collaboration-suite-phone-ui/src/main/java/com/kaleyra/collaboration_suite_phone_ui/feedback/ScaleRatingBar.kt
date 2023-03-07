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

package com.kaleyra.collaboration_suite_phone_ui.feedback

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.Interpolator
import androidx.core.view.children
import kotlin.math.floor
import kotlin.math.roundToInt

internal class ScaleRatingBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : BaseRatingBar(context, attrs, defStyleAttr) {

    private val childrenScales: MutableMap<View, Float> = mutableMapOf()

    init {
        updateChildrenScale()
    }

    override fun setProgress(rating: Float) {
        super.setProgress(rating)
        children.forEachIndexed { index, item ->
            val flooredRating = floor(rating.toDouble()).toInt()
            val targetScale = when {
                index > flooredRating - 1 -> SCALE_DOWN_VALUE
                else -> 1f
            }
            if(childrenScales[item] == targetScale) return@forEachIndexed
            childrenScales[item] = targetScale
            item.scale(targetScale, ANIMATION_DURATION, DecelerateInterpolator())
        }
    }

    override fun updateChildren(numLevels: Int) {
        super.updateChildren(numLevels)
        updateChildrenScale()
    }

    private fun updateChildrenScale() {
        children.forEachIndexed { index, child ->
            if(index < getRating().roundToInt()) return@forEachIndexed
            child.scaleX = SCALE_DOWN_VALUE
            child.scaleY = SCALE_DOWN_VALUE
        }
    }

    private fun View.scale(value: Float, duration: Long, interpolator: Interpolator) =
        animate()
            .scaleX(value)
            .scaleY(value)
            .setDuration(duration)
            .setInterpolator(interpolator)

    private companion object {
        const val ANIMATION_DURATION = 100L
        const val SCALE_DOWN_VALUE = .75f
    }
}