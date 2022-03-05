/*
 * Copyright 2022 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SeekBar
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassSliderLayoutBinding

/**
 *
 * @property onSliderChangeListener OnSliderChangeListener?
 * @constructor
 */
@SuppressLint("ClickableViewAccessibility")
internal abstract class SettingSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), SeekBar.OnSeekBarChangeListener {

    /**
     * Listener used to dispatch updates about slider value
     */
    interface OnSliderChangeListener {
        /**
         * Called when the slider progress value is changed
         *
         * @param progress Int
         */
        fun onProgressChanged(progress: Int)
    }

    protected abstract var binding: KaleyraGlassSliderLayoutBinding

    /**
     * The minimum progress value
     */
    open var minProgress = DEFAULT_MIN_VALUE

    /**
     * The maximum progress value
     */
    open var maxProgress = DEFAULT_MAX_VALUE
        set(value) {
            binding.kaleyraSeekbar.max = value
            field = value
        }

    /**
     * The progress value
     */
    var progress = 0
        set(value) {
            field = when {
                value <= minProgress -> minProgress
                value >= maxProgress -> maxProgress
                else -> value
            }
            binding.kaleyraSeekbar.progress = field
        }

    /**
     * Listener for the slider value
     */
    var onSliderChangeListener: OnSliderChangeListener? = null

    protected fun initSeekbar() = with(binding.kaleyraSeekbar) {
        setOnTouchListener { _, _ -> true }
        max = maxProgress
        // Apparently there is a bug in updating the visual representation of the progress if it is initialized to zero.
        // Neither the use of invalidate() and refreshDrawableState() solves the problem. The attribute android:saveEnabled="false"
        // has also been set in the xml but the result is the same. The only workaround to solve the problem is setting the progress to a value
        // different than zero, and then setting it to zero. In this way the visual representation is updated to the right value.
        progress = 1
        progress = minProgress
        setOnSeekBarChangeListener(this@SettingSlider)
    }

    /**
     * Increase the progress
     */
    fun increaseProgress() = with(binding.kaleyraSeekbar) {
        if(progress >= maxProgress) return@with
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setProgress(++progress, true)
        else progress = ++progress
    }

    /**
     * Decrease the progress
     */
    fun decreaseProgress() = with(binding.kaleyraSeekbar) {
        if(progress <= minProgress) return@with
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setProgress(--progress, true)
        else progress = --progress
    }

    /**
     * Set the slider text
     *
     * @param progress Int
     */
    protected abstract fun setSliderText(progress: Int)

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return binding.kaleyraSeekbar.onTouchEvent(event)
    }

    /**
     * @suppress
     */
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        if(progress < minProgress) {
            seekBar?.progress = minProgress
            return
        }
        
        this@SettingSlider.progress = progress
        setSliderText(progress)
        onSliderChangeListener?.onProgressChanged(progress)
    }

    /**
     * @suppress
     */
    override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit

    /**
     * @suppress
     */
    override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit

    private companion object {
        const val DEFAULT_MAX_VALUE = 10
        const val DEFAULT_MIN_VALUE = 0
    }
}