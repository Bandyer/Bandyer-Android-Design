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

package com.kaleyra.collaboration_suite_core_ui.call.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Guideline
import com.google.android.material.button.MaterialButton
import com.kaleyra.collaboration_suite_core_ui.databinding.KaleyraWidgetLivePointerBinding
import com.kaleyra.collaboration_suite_core_ui.textview.KaleyraAutoHideTextView
import com.kaleyra.collaboration_suite_core_ui.utils.Constraints
import com.kaleyra.collaboration_suite_core_ui.utils.KotlinConstraintSet.Companion.changeConstraints

/**
 * Kaleyra live pointer view
 * @constructor
 */
class LivePointerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

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
    var label: KaleyraAutoHideTextView? = null

    private var hasShownLabel = false

    private var horizontalGuideline: Guideline? = null
    private var verticalGuideline: Guideline? = null

    private var currentEdge: Edge = Edge.NONE

    private val binding: KaleyraWidgetLivePointerBinding by lazy {
        KaleyraWidgetLivePointerBinding.inflate(
            LayoutInflater.from(context),
            this
        )
    }

    init {
        pointerView = binding.kaleyraPointerButton
        label = binding.kaleyraPointerLabel
        horizontalGuideline = binding.kaleyraHorizontalGuideline
        verticalGuideline = binding.kaleyraVerticalGuideline

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
    fun updateLivePointerPosition(
        @FloatRange(from = 0.0, to = 100.0) leftPercentage: Float,
        @FloatRange(from = 0.0, to = 100.0) topPercentage: Float,
        enableAutoHide: Boolean = true,
        adjustTextOnEdge: Boolean = false
    ) {
        show(showLabel = false)
        changeConstraints {
            transition = true
            horizontalGuideline!!.id guidePercentTo topPercentage / 100f
            verticalGuideline!!.id guidePercentTo leftPercentage / 100f
        }
        if (!hasShownLabel) {
            hasShownLabel = true
            label!!.visibility = View.VISIBLE
            if (enableAutoHide) label!!.autoHide(AUTOHIDE_LABEL__MILLIS)
            else label!!.disableAutoHide()
        }

        if (adjustTextOnEdge)
            adjustTextOnEdge()
    }

    private fun adjustTextOnEdge() {
        val targetEdge = when {
            label!!.right > (right - label!!.width) -> Edge.RIGHT
            label!!.left < (left + label!!.width) -> Edge.LEFT
            pointerView!!.bottom + label!!.height > (bottom - label!!.height) -> Edge.BOTTOM
            else -> Edge.NONE
        }

        if (currentEdge == targetEdge) return
        currentEdge = targetEdge

        when (targetEdge) {
            Edge.RIGHT -> changeConstraints {
                transition = true
                label!!.id.clear(Constraints.START)
                label!!.id topToTopOf pointerView!!.id
                label!!.id bottomToBottomOf pointerView!!.id
                label!!.id endToStartOf pointerView!!.id
            }
            Edge.LEFT -> changeConstraints {
                transition = true
                label!!.id.clear(Constraints.END)
                label!!.id topToTopOf pointerView!!.id
                label!!.id bottomToBottomOf pointerView!!.id
                label!!.id startToEndOf pointerView!!.id
            }
            Edge.BOTTOM -> changeConstraints {
                transition = true
                label!!.id.clear(Constraints.TOP)
                label!!.id bottomToTopOf pointerView!!.id
                label!!.id startToStartOf pointerView!!.id
                label!!.id endToEndOf pointerView!!.id
            }
            else -> changeConstraints {
                transition = true
                label!!.id.clear(Constraints.BOTTOM)
                label!!.id topToBottomOf pointerView!!.id
                label!!.id startToStartOf pointerView!!.id
                label!!.id endToEndOf pointerView!!.id
            }
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

private enum class Edge {
    TOP,
    BOTTOM,
    RIGHT,
    LEFT,
    NONE
}