package com.bandyer.sdk_design.new_smartglass

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SeekBar
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerSliderLayoutBinding

@SuppressLint("ClickableViewAccessibility")
class BandyerSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    interface OnSliderChangeListener {
        fun onProgressChanged(progress: Int)
    }

    private var binding: BandyerSliderLayoutBinding =
        BandyerSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    private val pattern = resources.getString(R.string.bandyer_smartglass_slider_pattern)
    private var percentageText: String

    var onSliderChangeListener: OnSliderChangeListener? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.BandyerSlider)
        percentageText =
            attributes.getString(R.styleable.BandyerSlider_bandyer_percentageText) ?: ""
        attributes.recycle()

        setPercentageText(0)

        with(binding.seekbar) {
            setOnTouchListener { _, _ -> true }
            max = MAX_VALUE
            // Apparently there is a bug is updating the visual representation of the progress if it is initialized to zero.
            // Neither the use of invalidate() and refreshDrawableState() solves the problem. The attribute android:saveEnabled="false"
            // has also been set in the xml but the result is the same. The only workaround to solve the problem is setting the progress to a value
            // different than zero, and then setting it to zero. In this way the visual representation is updated to the right value.
            progress = 1
            progress = 0
            binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    setPercentageText(progress * 10)
                    onSliderChangeListener?.onProgressChanged(progress)
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
                override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
            })
        }
    }

    fun setProgress(value: Int) {
        binding.seekbar.progress = value
    }

    fun increaseProgress() = with(binding.seekbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            setProgress(progress + PROGRESS_DELTA, true)
        else
            progress += PROGRESS_DELTA
    }

    fun decreaseProgress() = with(binding.seekbar) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            setProgress(progress - PROGRESS_DELTA, true)
        else
            progress -= PROGRESS_DELTA
    }

    private fun setPercentageText(progress: Int) {
        binding.percentage.text = String.format(
            pattern,
            percentageText,
            progress
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return binding.seekbar.onTouchEvent(event)
    }

    private companion object {
        const val PROGRESS_DELTA = 1
        const val MAX_VALUE = 10
    }
}