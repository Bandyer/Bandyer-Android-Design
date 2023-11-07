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

package com.kaleyra.video_common_ui.call.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.FloatRange
import androidx.constraintlayout.widget.ConstraintLayout
import com.kaleyra.video_common_ui.databinding.KaleyraWidgetLivePointerBinding
import com.kaleyra.video_common_ui.utils.Constraints
import com.kaleyra.video_common_ui.utils.KotlinConstraintSet.Companion.changeConstraints

/**
 * Live pointer view
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

    private var hasShownLabel = false

    private var currentEdge: Edge = Edge.NONE

    private val binding: KaleyraWidgetLivePointerBinding by lazy {
        KaleyraWidgetLivePointerBinding.inflate(
            LayoutInflater.from(context),
            this
        )
    }

    init {
        with(binding) {
            kaleyraPointerButton.setOnClickListener {
                kaleyraPointerLabel.apply {
                    visibility = View.VISIBLE
                    disableAutoHide()
                    autoHide(AUTOHIDE_LABEL__MILLIS)
                }
            }
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
    ) = with(binding) {
        changeConstraints {
            transition = true
            kaleyraHorizontalGuideline.id guidePercentTo topPercentage / 100f
            kaleyraVerticalGuideline.id guidePercentTo leftPercentage / 100f
        }
        updateLivePointerInternal(enableAutoHide, adjustTextOnEdge)
    }

    /**
     * Update pointer position by left percentage
     * @param leftPercentage Float left percentage
     */
    fun updateLivePointerHorizontalPosition(
        @FloatRange(from = 0.0, to = 100.0) leftPercentage: Float,
        enableAutoHide: Boolean = true,
        adjustTextOnEdge: Boolean = false
        ) {
        changeConstraints {
            binding.kaleyraVerticalGuideline.id guidePercentTo leftPercentage / 100f
        }
        updateLivePointerInternal(enableAutoHide, adjustTextOnEdge)
    }

    private fun updateLivePointerInternal(
        enableAutoHide: Boolean,
        adjustTextOnEdge: Boolean
    ) = with(binding) {
        show(showLabel = false)
        if (!hasShownLabel) {
            hasShownLabel = true
            kaleyraPointerLabel.visibility = View.VISIBLE
            if (enableAutoHide) kaleyraPointerLabel.autoHide(AUTOHIDE_LABEL__MILLIS)
            else kaleyraPointerLabel.disableAutoHide()
        }

        if (adjustTextOnEdge)
            adjustTextOnEdge()
    }

    private fun adjustTextOnEdge() {
        val label = binding.kaleyraPointerLabel
        val pointer = binding.kaleyraPointerButton
        val targetEdge = when {
            label.right > (right - label.width) -> Edge.RIGHT
            label.left < (left + label.width) -> Edge.LEFT
            pointer.bottom + label.height > (bottom - label.height) -> Edge.BOTTOM
            else -> Edge.NONE
        }

        if (currentEdge == targetEdge) return
        currentEdge = targetEdge

        when (targetEdge) {
            Edge.RIGHT -> changeConstraints {
                transition = true
                label.id.clear(Constraints.START)
                label.id topToTopOf pointer.id
                label.id bottomToBottomOf pointer.id
                label.id endToStartOf pointer.id
            }
            Edge.LEFT -> changeConstraints {
                transition = true
                label.id.clear(Constraints.END)
                label.id topToTopOf pointer.id
                label.id bottomToBottomOf pointer.id
                label.id startToEndOf pointer.id
            }
            Edge.BOTTOM -> changeConstraints {
                transition = true
                label.id.clear(Constraints.TOP)
                label.id bottomToTopOf pointer.id
                label.id startToStartOf pointer.id
                label.id endToEndOf pointer.id
            }
            else -> changeConstraints {
                transition = true
                label.id.clear(Constraints.BOTTOM)
                label.id topToBottomOf pointer.id
                label.id startToStartOf pointer.id
                label.id endToEndOf pointer.id
            }
        }
    }

    /**
     * Update pointer view's label text
     * @param labelText String label text
     */
    fun updateLabelText(labelText: String) {
        binding.kaleyraPointerLabel.text = labelText
    }

    /**
     * Show the pointer
     */
    fun show(showLabel: Boolean? = true) = with(binding) {
        kaleyraPointerButton.visibility = View.VISIBLE
        if (showLabel == false) return
        kaleyraPointerButton.performClick()
        kaleyraPointerLabel.visibility = View.VISIBLE
        kaleyraPointerLabel.disableAutoHide()
        kaleyraPointerLabel.autoHide(AUTOHIDE_LABEL__MILLIS)
    }

    /**
     * Hide the pointer
     */
    fun hide() = with(binding) {
        kaleyraPointerButton.visibility = View.GONE
        kaleyraPointerLabel.visibility = View.GONE
        kaleyraPointerLabel.disableAutoHide()
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