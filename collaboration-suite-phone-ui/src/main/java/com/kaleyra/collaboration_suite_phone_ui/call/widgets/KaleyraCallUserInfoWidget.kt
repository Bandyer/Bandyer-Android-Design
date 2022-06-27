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

package com.kaleyra.collaboration_suite_phone_ui.call.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater

import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraWidgetCallUserInfoBinding
import com.kaleyra.collaboration_suite_phone_ui.extensions.scanForFragmentActivity
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingTop
import com.kaleyra.collaboration_suite_phone_ui.layout.SystemControlsAwareHorizontalConstraintLayout
import com.kaleyra.collaboration_suite_phone_ui.utils.ToggleableVisibilityInterface
import com.kaleyra.collaboration_suite_phone_ui.utils.VisibilityToggle
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textview.MaterialTextView

/**
 * Kaleyra user info widget that will be displayed from the top
 * @property fullscreenActionButton MaterialButton?
 * @property displayName MaterialTextView?
 * @property currentTopPixels Int pixels from the top of the screen
 * @property considerSystemAwareHints Boolean
 * @property text String?
 * @constructor
 */
class KaleyraCallUserInfoWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle) :
        SystemControlsAwareHorizontalConstraintLayout(context, attrs, defStyleAttr), ToggleableVisibilityInterface {

    /**
     * Fullscreen action button
     */
    var fullscreenActionButton: MaterialButton? = null

    /**
     * Back button
     */
    var backButton: MaterialButton? = null

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
            binding.kaleyraDisplayName.text = value
            field = value
        }

    private var visibilityToggle: VisibilityToggle? = VisibilityToggle(this)

    private var attrs: AttributeSet? = null

    private val binding: KaleyraWidgetCallUserInfoBinding by lazy { KaleyraWidgetCallUserInfoBinding.inflate(LayoutInflater.from(context), this) }

    init {
        fullscreenActionButton = binding.kaleyraFullscreenActionButton
        displayName = binding.kaleyraDisplayName
        backButton = binding.kaleyraBackButton

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