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

package com.bandyer.sdk_design.textviews

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.util.AttributeSet
import android.view.View
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.extensions.getBouncingDotsBooleanAttribute
import com.bandyer.sdk_design.extensions.getBouncingDotsDimensionAttribute
import com.bandyer.sdk_design.extensions.getBouncingDotsIntAttribute
import com.bandyer.sdk_design.utils.JumpingSpan
import com.google.android.material.textview.MaterialTextView
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Animated view with dots going up and down
 * Created by federicomarin on 02/12/15.
 */

class BandyerTextViewBouncingDots @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : MaterialTextView(context, attrs, defStyleAttr) {

    private var mAnimatorSet: AnimatorSet? = null
    private var dotOne: JumpingSpan? = null
    private var dotTwo: JumpingSpan? = null
    private var dotThree: JumpingSpan? = null

    private val showSpeed = resources.getInteger(android.R.integer.config_longAnimTime)
    private var jumpHeight: Int = 0
    private var autoPlay: Boolean = false

    /**
     * return true if the bouncing dots animation is playing
     */
    var isShowingBouncingDots: Boolean = false
        private set

    private var dotChar = "•"
    private var showDots = false
    private var period: Int = 0
    private var textWidth: Float = 0.toFloat()

    init {
        autoPlay = context.getBouncingDotsBooleanAttribute(R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots_bandyer_autoplay)
        period = context.getBouncingDotsIntAttribute(R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots_bandyer_period)
        jumpHeight = context.getBouncingDotsDimensionAttribute(R.styleable.BandyerSDKDesign_TextView_Subtitle_BouncingDots_bandyer_animationHeight).roundToInt()
        if (dotOne == null) dotOne = JumpingSpan()
        if (dotTwo == null) dotTwo = JumpingSpan()
        if (dotThree == null) dotThree = JumpingSpan()
    }

    /**
     * Show dots and play bouncing dots animation
     */
    fun showBouncingDots() {
        showDots = true
        visibility = View.VISIBLE
        text = text

        val dotThreeMoveRightToLeft = createDotShowAnimator(dotThree, 2)
        dotThreeMoveRightToLeft.addUpdateListener { invalidate() }
        dotThreeMoveRightToLeft.start()

        val dotTwoMoveRightToLeft = createDotShowAnimator(dotTwo, 1)
        dotTwoMoveRightToLeft.addUpdateListener { invalidate() }
        dotTwoMoveRightToLeft.start()

        start()
    }

    /**
     * Hide dots and stop bouncing dots animation
     */
    fun hideBouncingDots() {
        stop()
        showDots = false

        val dotThreeMoveRightToLeft = createDotHideAnimator(dotThree, 2f)
        dotThreeMoveRightToLeft.addUpdateListener { invalidate() }
        dotThreeMoveRightToLeft.start()

        val dotTwoMoveRightToLeft = createDotHideAnimator(dotTwo, 1f)
        dotTwoMoveRightToLeft.addUpdateListener { invalidate() }
        dotTwoMoveRightToLeft.start()

        text = text.toString().replace(dotChar, "").trim()
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if (text.toString() == this.text.toString()) return
        if (!showDots) {
            super.setText(text, type)
            return
        }
        val spannable = SpannableString("${text.toString().replace(dotChar, "")} $dotChar$dotChar$dotChar")
        spannable.setSpan(dotOne, spannable.length - 3, spannable.length - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(dotTwo, spannable.length - 2, spannable.length - 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(dotThree, spannable.length - 1, spannable.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        super.setText(spannable, BufferType.SPANNABLE)
        textWidth = paint.measureText(dotChar, 0, 1)
    }

    /**
     * @suppress
     */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isShowingBouncingDots = autoPlay
        if (autoPlay) start()
    }

    /**
     * @suppress
     */
    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    private fun start() {
        stop()
        isShowingBouncingDots = true
        mAnimatorSet = AnimatorSet()
        mAnimatorSet?.playTogether(
                createDotJumpAnimator(dotOne!!, 0).apply { this.addUpdateListener { this@BandyerTextViewBouncingDots.invalidate() } },
                createDotJumpAnimator(dotTwo!!, (period / 6).toLong()),
                createDotJumpAnimator(dotThree!!, (period * 2 / 6).toLong()))
        setAllAnimationsRepeatCount(ValueAnimator.INFINITE)
        mAnimatorSet?.start()
    }

    private fun stop() {
        isShowingBouncingDots = false
        setAllAnimationsRepeatCount(0)
        mAnimatorSet?.cancel()
        mAnimatorSet?.removeAllListeners()
    }

    private fun setAllAnimationsRepeatCount(repeatCount: Int) {
        mAnimatorSet ?: return
        for (animator in mAnimatorSet!!.childAnimations) {
            if (animator is ObjectAnimator) {
                animator.repeatCount = repeatCount
            }
        }
    }

    private fun createDotHideAnimator(span: JumpingSpan?, widthMultiplier: Float): ObjectAnimator {
        return createDotHorizontalAnimator(span, 0f, -textWidth * widthMultiplier)
    }

    private fun createDotShowAnimator(span: JumpingSpan?, widthMultiplier: Int): ObjectAnimator {
        return createDotHorizontalAnimator(span, -textWidth * widthMultiplier, 0f)
    }

    private fun createDotHorizontalAnimator(span: JumpingSpan?, from: Float, to: Float): ObjectAnimator {
        val dotThreeMoveRightToLeft = ObjectAnimator.ofFloat(span, "translationX", from, to)
        dotThreeMoveRightToLeft.duration = showSpeed.toLong()
        return dotThreeMoveRightToLeft
    }

    private fun createDotJumpAnimator(jumpingSpan: JumpingSpan, delay: Long): ObjectAnimator {
        val jumpAnimator = ObjectAnimator.ofFloat(jumpingSpan, "translationY", 0f, -jumpHeight.toFloat())
        jumpAnimator.setEvaluator(TypeEvaluator<Number> { fraction, from: Number, to: Number -> max(0.0, sin(fraction.toDouble() * Math.PI * 2.0)) * (to.toFloat() - from.toFloat()) })
        jumpAnimator.duration = period.toLong()
        jumpAnimator.startDelay = delay
        jumpAnimator.repeatCount = ValueAnimator.INFINITE
        jumpAnimator.repeatMode = ValueAnimator.RESTART
        return jumpAnimator
    }
}
