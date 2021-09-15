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

package com.bandyer.video_android_phone_ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import com.bandyer.video_android_phone_ui.extensions.scanForFragmentActivity
import com.bandyer.video_android_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.bandyer.video_android_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import kotlin.math.max
import kotlin.math.min

/**
 *
 * @author kristiyan
 */
open class SystemControlsAwareHorizontalGuideline @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : Guideline(context, attrs, defStyleAttr), SystemViewLayoutObserver {


    ///////////////////////////////////////// SYSTEM CONTROLS AWARE OBSERVER //////////////////////////////////////////////////////////////////////////////////

    private var initialGuidePercentage = -1f
    private var adjustedGuidePercentage = -1f

    private var currentLeftInset = 0
    private var currentRightInset = 0

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (initialGuidePercentage == -1f) {
            initialGuidePercentage = (layoutParams as? ConstraintLayout.LayoutParams)?.guidePercent
                    ?: 0f
        } else setGuidelinePercent(adjustedGuidePercentage)
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.addObserver(it, this)
        }
    }


    /**
     * @suppress
     * @param visibility Int
     */
    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.getOffsets(it)
        }
    }


    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        context.scanForFragmentActivity()?.let {
            SystemViewLayoutOffsetListener.removeObserver(it as AppCompatActivity, this)
        }
    }

    /**
     * @suppress
     */
    override fun onTopInsetChanged(pixels: Int) {

    }

    /**
     * @suppress
     */
    override fun onBottomInsetChanged(pixels: Int) {

    }

    /**
     * @suppress
     */
    override fun onRightInsetChanged(pixels: Int) {
        currentRightInset = pixels
        onLeftRightInsetChanged()
    }

    /**
     * @suppress
     */
    override fun onLeftInsetChanged(pixels: Int) {
        currentLeftInset = pixels
        onLeftRightInsetChanged()
    }


    private fun adjustGuideline(pixels: Int, signed: Int) {
        val parentWidth = (parent as View).width
        if (parentWidth == 0) return
        adjustedGuidePercentage =
                if (pixels == 0) initialGuidePercentage
                else initialGuidePercentage + signed * (pixels.toFloat() / parentWidth) / 2f

        setGuidelinePercent(adjustedGuidePercentage)
    }

    private fun onLeftRightInsetChanged() {
        val deltaTranslation = max(currentLeftInset, currentRightInset) - min(currentLeftInset, currentRightInset)
        val signed = if (max(currentLeftInset, currentRightInset) == currentRightInset) -1 else 1
        adjustGuideline(deltaTranslation, signed)
    }
}