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


import android.animation.Animator
import android.content.Context
import android.os.Build
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout


import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ContextExtensions.getActivity
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingLeft
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingRight
import com.kaleyra.collaboration_suite_core_ui.utils.extensions.ViewExtensions.setPaddingTop
import com.kaleyra.collaboration_suite_core_ui.layout.KaleyraCallWatermarkLayout
import com.kaleyra.collaboration_suite_phone_ui.R
import com.kaleyra.collaboration_suite_phone_ui.databinding.KaleyraWidgetCallInfoBinding
import com.kaleyra.collaboration_suite_phone_ui.extensions.*
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutObserver
import com.kaleyra.collaboration_suite_phone_ui.utils.systemviews.SystemViewLayoutOffsetListener
import com.kaleyra.collaboration_suite_core_ui.widget.HideableWidget
import com.google.android.material.textview.MaterialTextView
import com.kaleyra.collaboration_suite_phone_ui.recording.RecordingWidget

/**
 * This class represent a widget used to display in-call informations.
 * It has a tile, a subtitle and an icon that can be used to display the recording state of the video/audio call.
 */
class KaleyraCallInfoWidget @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = R.attr.kaleyra_rootLayoutStyle)
    : ConstraintLayout(context, attrs, defStyleAttr), SystemViewLayoutObserver, HideableWidget {

    /**
     * Representation of call info ui components
     */
    enum class CallInfoComponents {
        /**
         * Watermark ui component
         */
        WATERMARK,

        /**
         * Title ui component
         */
        TITLE,

        /**
         * Subtitle ui component
         */
        SUBTITLE,

        /**
         * Recording ui component
         */
        RECORDING
    }

    /**
     * @suppress
     */
    override var hidingTimer: CountDownTimer? = null

    /**
     * @suppress
     */
    override var millisUntilTimerFinish: Long = 0

    /**
     * Determines if the view is showing or shown
     */
    var isShowing = true

    private var AUTO_HIDE_ANIMATION_DURATION_MS = 250L
    private var AUTO_SHOW_ANIMATION_DURATION_MS = 125L

    private var currentTopPixels = 0
    private var currentRightPixels = 0
    private var currentLeftPixels = 0
    private var customTopPadding = 0
    private var customLeftPadding = 0
    private var customRightPadding = 0

    /**
     * Represents the capability of observe safe area layout by adding paddings
     */
    var considerSystemAwareHints = true
        set(value) {
            field = value
            if (value) {
                onTopInsetChanged(currentTopPixels)
                onRightInsetChanged(currentRightPixels)
                onLeftInsetChanged(currentLeftPixels)
                context.scanForFragmentActivity()?.let { fragmentActivity ->
                    SystemViewLayoutOffsetListener.getOffsets(fragmentActivity)
                }
            } else {
                setTopPadding(customTopPadding)
                setLeftPadding(customLeftPadding)
                setRightPadding(customRightPadding)
            }
        }

    /**
     * Title view
     */
    var titleView: MaterialTextView? = null
        private set

    /**
     * Subtitle view
     */
    var subtitleView: MaterialTextView? = null
        private set

    /**
     * Recording view
     */
    var recordingView: RecordingWidget? = null
        private set


    /**
     * Recording view
     */
    var watermarkView: KaleyraCallWatermarkLayout? = null
        private set

    private var initialPaddingStart = -1
    private var initialPaddingEnd = -1
    private var initialPaddingLeft = -1
    private var initialPaddingRight = -1
    private var initialPaddingTop = -1
    private var initialPaddingBottom = -1

    private val binding: KaleyraWidgetCallInfoBinding by lazy { KaleyraWidgetCallInfoBinding.inflate(LayoutInflater.from(context), this) }

    init {
        titleView = binding.kaleyraTitle
        subtitleView = binding.kaleyraSubtitle
        recordingView = binding.kaleyraRecording

        titleView?.isSelected = true // activate marquee
        watermarkView = binding.kaleyraWatermark

        initialPaddingLeft = paddingLeft
        initialPaddingTop = paddingTop
        initialPaddingRight = paddingRight
        initialPaddingBottom = paddingBottom
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) initialPaddingEnd = paddingEnd
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) initialPaddingStart = paddingStart
    }

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        SystemViewLayoutOffsetListener.addObserver(context.getActivity()!!, this)
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        SystemViewLayoutOffsetListener.removeObserver(context.getActivity()!!, this)
    }

    /**
     * @suppress
     */
    override fun onHidingTimerFinished() = animateHide()

    private fun animateHide(onHidden: (() -> Unit)? = null) {
        isShowing = false
        animate().translationY(-height.toFloat()).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                visibility = View.INVISIBLE
                onHidden?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {}
        }).setDuration(AUTO_HIDE_ANIMATION_DURATION_MS).start()
    }

    /**
     * Method to show & hide the widget
     */
    fun toggle() {
        disableAutoHide()
        if (visibility == View.VISIBLE) {
            onHidingTimerFinished()
        } else {
            show()
        }
        hidingTimer?.start()
    }

    /**
     * Indefinitely shows the widget.
     * @param onShown Function0<Unit>? on shown callback
     */
    fun show(onShown: (() -> Unit)? = null) {
        isShowing = true
        animate().translationY(0f).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) {}
            override fun onAnimationEnd(animation: Animator?) {
                onShown?.invoke()
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationStart(animation: Animator?) {
                visibility = View.VISIBLE
            }
        }).setDuration(AUTO_SHOW_ANIMATION_DURATION_MS).start()
    }

    /**
     * Indefinitely hides the widget.
     */
    fun hide() {
        isShowing = false
        hidingTimer?.cancel()
        onHidingTimerFinished()
    }

    /**
     * Display title text
     * @param titleText title
     */
    fun setTitle(titleText: String) {
        titleView?.text = titleText
    }

    /**
     * Display subtitle text
     * @param subtitleText subtitle
     */
    fun setSubtitle(subtitleText: String) {
        subtitleView?.text = subtitleText
        if (subtitleText.isEmpty())
            hideSubtitle()
    }

    /**
     * Hide subtitle view
     */
    fun hideSubtitle() {
        subtitleView?.visibility = View.GONE
    }

    /**
     * Show subtitle view
     */
    fun showSubtitle() {
        subtitleView?.visibility = View.VISIBLE
    }

    /**
     * Show subtitle view
     */
    fun showTitle() {
        titleView?.visibility = View.VISIBLE
    }

    /**
     * Show watermark view
     */
    fun showWatermark() {
        watermarkView?.visibility = View.VISIBLE
    }

    /**
     * Hide watermark view
     */
    fun hideWatermark() {
        watermarkView?.visibility = View.GONE
    }

    /**
     * Hide title view
     */
    fun hideTitle() {
        titleView?.visibility = View.GONE
    }

    /**
     * Display specified call info components
     * @param components Array<out CallInfoComponents> Call info components to be shown
     * @param onShown Function0<Unit>? Optional callback after called after showing process
     */
    fun show(vararg components: CallInfoComponents, onShown: (() -> Unit)? = null) {
        watermarkView?.visibility = if (components.contains(CallInfoComponents.WATERMARK)) View.VISIBLE else View.GONE
        titleView?.visibility = if (components.contains(CallInfoComponents.TITLE)) View.VISIBLE else View.GONE
        recordingView?.visibility = if (components.contains(CallInfoComponents.RECORDING)) View.VISIBLE else View.GONE
        subtitleView?.visibility = if (components.contains(CallInfoComponents.SUBTITLE)) View.VISIBLE else View.GONE
        if (components.isEmpty()) {
            animateHide(onShown)
        } else show(onShown)
    }

    /**
     * Show or hide recording icon
     * @param enabled enabled state
     */
    fun setRecording(enabled: Boolean) {
        if (enabled) recordingView?.visibility = View.VISIBLE
        else recordingView?.visibility = View.GONE
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
     * Set right padding that will be consider above system aware
     * @param pixels Int
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setRightPadding(pixels: Int) {
        currentRightPixels = pixels
        binding.root.setPaddingRight(pixels)
    }

    /**
     * Set left padding that will be consider above system aware
     * @param pixels Int
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun setLeftPadding(pixels: Int) {
        currentLeftPixels = pixels
        binding.root.setPaddingLeft(pixels)
    }

    /**
     * @suppress
     */
    override fun onTopInsetChanged(pixels: Int) {
        post {
            if (!considerSystemAwareHints) return@post
            if (paddingTop == pixels) return@post
            currentTopPixels = pixels
            val pixelToBeSet = when {
                customTopPadding != 0 && customTopPadding != currentTopPixels -> customTopPadding
                else -> currentTopPixels
            }
            binding.root.setPaddingTop(pixelToBeSet)
        }
    }

    /**
     * @suppress
     */
    override fun onBottomInsetChanged(pixels: Int) {}

    /**
     * @suppress
     */
    override fun onRightInsetChanged(pixels: Int) {
        post {
            if (!considerSystemAwareHints) return@post
            if (paddingRight == pixels) return@post
            currentRightPixels = pixels
            val pixelToBeSet = when {
                customRightPadding != 0 && customRightPadding != currentRightPixels -> customRightPadding
                else -> currentRightPixels
            }
            binding.root.setPaddingRight(pixelToBeSet)
        }
    }

    /**
     * @suppress
     */
    override fun onLeftInsetChanged(pixels: Int) {
        post {
            if (!considerSystemAwareHints) return@post
            if (paddingLeft == pixels) return@post
            currentLeftPixels = pixels
            val pixelToBeSet = when {
                customLeftPadding != 0 && customLeftPadding != currentLeftPixels -> customLeftPadding
                else -> currentLeftPixels
            }
            binding.root.setPaddingLeft(pixelToBeSet)
        }
    }
}