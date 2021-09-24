package com.bandyer.video_android_glass_ui.settings

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SeekBar
import com.bandyer.video_android_glass_ui.databinding.BandyerSliderLayoutBinding

/**
 *
 * @property onSliderChangeListener OnSliderChangeListener?
 * @constructor
 */
@SuppressLint("ClickableViewAccessibility")
abstract class BandyerSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

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

    protected var binding: BandyerSliderLayoutBinding =
        BandyerSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    /**
     * Listener for the slider value
     */
    var onSliderChangeListener: OnSliderChangeListener? = null

    init {
        with(binding.bandyerSeekbar) {
            setOnTouchListener { _, _ -> true }
            max = MAX_VALUE
            // Apparently there is a bug is updating the visual representation of the progress if it is initialized to zero.
            // Neither the use of invalidate() and refreshDrawableState() solves the problem. The attribute android:saveEnabled="false"
            // has also been set in the xml but the result is the same. The only workaround to solve the problem is setting the progress to a value
            // different than zero, and then setting it to zero. In this way the visual representation is updated to the right value.
            progress = 1
            progress = 0
            binding.bandyerSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    setSliderText(progress * 10)
                    onSliderChangeListener?.onProgressChanged(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
    }

    /**
     * Set the slider progress value
     *
     * @param value Int
     */
    fun setProgress(value: Int) {
        binding.bandyerSeekbar.progress = value
    }

    /**
     * Increase the progress by the specified percentage
     *
     * @param percentage Float
     */
    fun increaseProgress(percentage: Float) = with(binding.bandyerSeekbar) {
        val deltaValue = (percentage * MAX_VALUE).coerceAtLeast(0f).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            setProgress(progress + deltaValue, true)
        else
            progress += deltaValue
    }

    /**
     * Decrease the progress by the specified percentage
     *
     * @param percentage Float
     */
    fun decreaseProgress(percentage: Float) = with(binding.bandyerSeekbar) {
        val deltaValue = (percentage * MAX_VALUE).coerceAtLeast(0f).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            setProgress(progress - deltaValue, true)
        else
            progress -= deltaValue
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

    private companion object {
        const val MAX_VALUE = 10
    }
}