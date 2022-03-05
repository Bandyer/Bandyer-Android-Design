/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_phone_ui.feedback

import android.content.Context
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.annotation.FloatRange
import androidx.annotation.Px
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraRatingBarElementBinding

/**
 * A BaseRatingBarElement
 */
internal class BaseRatingBarElement : FrameLayout {

    private var binding = KaleyraRatingBarElementBinding.inflate(LayoutInflater.from(context), this, true)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    constructor(context: Context, progressDrawable: Drawable, backgroundDrawable: Drawable, iconSize: Int, @Px padding: Int) : super(context) {
        layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, 1f)

        val size = if (iconSize == 0) LayoutParams.WRAP_CONTENT else iconSize
        val params = LayoutParams(size, size).apply { gravity = Gravity.CENTER_HORIZONTAL }

        with(binding.kaleyraProgressImage) {
            layoutParams = params
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            if(progressDrawable.constantState == null) return@with
            setImageDrawable(ClipDrawable(progressDrawable.constantState!!.newDrawable(), Gravity.START, ClipDrawable.HORIZONTAL))
        }

        with(binding.kaleyraBackgroundImage)  {
            layoutParams = params
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            if(backgroundDrawable.constantState == null) return@with
            setImageDrawable(ClipDrawable(backgroundDrawable.constantState!!.newDrawable(), Gravity.END, ClipDrawable.HORIZONTAL))
        }

        setProgress(0f)
        setPadding(padding, padding, padding, padding)
    }

    /**
     * Set the percentage progress for the item
     *
     * @param rating Float
     */
    fun setProgress(@FloatRange(from = 0.0, to = 1.0) rating: Float) = with(binding) {
        val level = (MAX_IMAGE_LEVEL * rating).toInt()
        kaleyraProgressImage.setImageLevel(level)
        kaleyraBackgroundImage.setImageLevel(MAX_IMAGE_LEVEL - level)
    }

    private companion object {
        const val MAX_IMAGE_LEVEL = 10000
    }
}