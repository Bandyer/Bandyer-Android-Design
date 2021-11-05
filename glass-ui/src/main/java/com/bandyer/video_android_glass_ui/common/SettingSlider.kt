package com.bandyer.video_android_glass_ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SeekBar
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassSliderLayoutBinding

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

    protected abstract var binding: BandyerGlassSliderLayoutBinding

    /**
     * The minimum progress value
     */
    open var minProgress = DEFAULT_MIN_VALUE

    /**
     * The maximum progress value
     */
    open var maxProgress = DEFAULT_MAX_VALUE
        set(value) {
            binding.bandyerSeekbar.max = value
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
            binding.bandyerSeekbar.progress = field
        }

    /**
     * Listener for the slider value
     */
    var onSliderChangeListener: OnSliderChangeListener? = null

    protected fun initSeekbar() = with(binding.bandyerSeekbar) {
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
    fun increaseProgress() = with(binding.bandyerSeekbar) {
        if(progress >= maxProgress) return@with
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) setProgress(++progress, true)
        else progress = ++progress
    }

    /**
     * Decrease the progress
     */
    fun decreaseProgress() = with(binding.bandyerSeekbar) {
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
        return binding.bandyerSeekbar.onTouchEvent(event)
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