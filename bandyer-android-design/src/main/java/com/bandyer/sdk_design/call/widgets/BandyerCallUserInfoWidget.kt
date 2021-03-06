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
import androidx.constraintlayout.widget.ConstraintLayout
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerWidgetCallUserInfoBinding
import com.bandyer.sdk_design.extensions.scanForFragmentActivity
import com.bandyer.sdk_design.extensions.setPaddingTop
import com.bandyer.sdk_design.layout.SystemControlsAwareHorizontalConstraintLayout
import com.bandyer.sdk_design.utils.ToggleableVisibilityInterface
import com.bandyer.sdk_design.utils.VisibilityToggle
import com.bandyer.sdk_design.utils.systemviews.SystemViewLayoutOffsetListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * Bandyer user info widget that will be displayed from the top
 * @property fullscreenActionButton MaterialButton?
 * @property displayName MaterialTextView?
 * @property currentTopPixels Int pixels from the top of the screen
 * @property considerSystemAwareHints Boolean
 * @property text String?
 * @constructor
 */
class BandyerCallUserInfoWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.bandyer_rootLayoutStyle) :
        SystemControlsAwareHorizontalConstraintLayout(context, attrs, defStyleAttr), ToggleableVisibilityInterface {

    /**
     * Fullscreen action button
     */
    var fullscreenActionButton: MaterialButton? = null

    /**
     * Display name label
     */
    var displayName: MaterialTextView? = null

    private var currentTopPixels = 0
    private var customTopPadding = 0

    var considerSystemAwareHints = false
        set(value) {
            field = value
            if (value) {
                onTopInsetChanged(currentTopPixels)
                context.scanForFragmentActivity()?.let { fragmentActivity ->
                    SystemViewLayoutOffsetListener.getOffsets(fragmentActivity)
                }
            } else setTopPadding(customTopPadding)
        }

    var text: String? = null
        set(value) {
            binding.bandyerDisplayName.text = value
            field = value
        }

    private var visibilityToggle: VisibilityToggle? = VisibilityToggle(this)

    private var attrs: AttributeSet? = null

    private val binding: BandyerWidgetCallUserInfoBinding by lazy { BandyerWidgetCallUserInfoBinding.inflate(LayoutInflater.from(context), this) }

    init {
        fullscreenActionButton = binding.bandyerFullscreenActionButton
        displayName = binding.bandyerDisplayName

        this.attrs = attrs

        setFullscreenStyle(false)
    }

    override fun toggleVisibility(show: Boolean, animationEndCallback: (shown: Boolean) -> Unit) {
        visibilityToggle?.toggleVisibility(show, animationEndCallback)
    }

    /**
     * Enables fullscreen action button style
     * @param fullscreen Boolean true if fullscreen, false otherwise
     */
    fun setFullscreenStyle(fullscreen: Boolean) {
        fullscreenActionButton?.isActivated = fullscreen
    }

    override fun cancelTimer() {
        visibilityToggle?.cancelTimer()
    }

    /**
     * Set alpha value
     * @param alpha alpha
     */
    override fun setAlpha(alpha: Float) {
        super.setAlpha(alpha)
        if (alpha != 0f) return
        cancelTimer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelTimer()
    }

    /**
     * Set top padding that will be consider above system aware
     * @param pixels Int
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setTopPadding(pixels: Int) {
        customTopPadding = pixels
        binding.root.setPaddingTop(pixels)
    }

    /**
     * @suppress
     */
    override fun onTopInsetChanged(pixels: Int) {
        if (!considerSystemAwareHints) return
        if (paddingTop == pixels) return
        currentTopPixels = pixels
        val pixelToBeSet = when {
            customTopPadding != 0 && customTopPadding != currentTopPixels -> customTopPadding
            else -> currentTopPixels
        }
        binding.root.setPaddingTop(pixelToBeSet)
    }
}