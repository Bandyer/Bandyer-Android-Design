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

package com.bandyer.sdk_design.call.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import com.bandyer.sdk_design.databinding.BandyerWidgetLivePointerBinding
import com.bandyer.sdk_design.textviews.BandyerAutoHideTextView
import com.bandyer.sdk_design.utils.KotlinConstraintSet.Companion.changeConstraints
import com.google.android.material.button.MaterialButton

/**
 * Bandyer live pointer view
 * @constructor
 */
class LivePointerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : ConstraintLayout(context, attrs, defStyleAttr) {

    /**
     * Live pointer view properties
     */
    companion object {
        /**
         * Countdown timer millis before hide label
         */
        const val AUTOHIDE_LABEL__MILLIS = 3000L
    }

    /**
     * Pointer view
     */
    var pointerView: MaterialButton? = null

    /**
     * Pointer text label
     */
    var label: BandyerAutoHideTextView? = null

    private var hasShownLabel = false

    private var horizontalGuideline: Guideline? = null
    private var verticalGuideline: Guideline? = null

    private val binding: BandyerWidgetLivePointerBinding by lazy { BandyerWidgetLivePointerBinding.inflate(LayoutInflater.from(context), this) }

    init {
        pointerView = binding.bandyerPointerButton
        label = binding.bandyerPointerLabel
        horizontalGuideline = binding.bandyerHorizontalGuideline
        verticalGuideline = binding.bandyerVerticalGuideline

        pointerView!!.setOnClickListener {
            label!!.visibility = View.VISIBLE
            label!!.hidingTimer?.cancel()
            label!!.autoHide(AUTOHIDE_LABEL__MILLIS)
        }
    }

    /**
     * Update pointer position by left and top percentage
     * @param leftPercentage Float left percentage
     * @param topPercentage Float top percentage
     */
    fun updateLivePointerPosition(@FloatRange(from = 0.0, to = 100.0) leftPercentage: Float, @FloatRange(from = 0.0, to = 100.0) topPercentage: Float) {
        show(showLabel = false)
        changeConstraints {
            transition = true
            horizontalGuideline!!.id guidePercentTo topPercentage / 100f
            verticalGuideline!!.id guidePercentTo leftPercentage / 100f
        }
        if (!hasShownLabel) {
            hasShownLabel = true
            label!!.visibility = View.VISIBLE
            label!!.autoHide(AUTOHIDE_LABEL__MILLIS)
        }
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        label!!.hidingTimer?.cancel()
    }

    /**
     * Update pointer view's label text
     * @param labelText String label text
     */
    fun updateLabelText(labelText: String) {
        label!!.text = labelText
    }

    /**
     * Show the pointer
     */
    fun show(showLabel: Boolean? = true) {
        pointerView!!.visibility = View.VISIBLE
        if (showLabel == false) return
        pointerView!!.performClick()
        label!!.visibility = View.VISIBLE
        label!!.hidingTimer?.cancel()
        label!!.autoHide(AUTOHIDE_LABEL__MILLIS)
    }

    /**
     * Hide the pointer
     */
    fun hide() {
        pointerView!!.visibility = View.GONE
        label!!.visibility = View.GONE
        label!!.hidingTimer?.cancel()
        hasShownLabel = false
    }
}