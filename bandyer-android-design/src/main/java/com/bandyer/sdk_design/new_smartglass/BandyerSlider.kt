package com.bandyer.sdk_design.new_smartglass

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.SeekBar
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerSliderLayoutBinding

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

    private val pattern = resources.getString(R.string.bandyer_slider_pattern)
    private var percentageText: String

    var onSliderChangeListener: OnSliderChangeListener? = null

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.BandyerSlider)
        percentageText =
            attributes.getString(R.styleable.BandyerSlider_bandyer_percentageText) ?: ""
        attributes.recycle()

        setPercentageText(0)

        binding.seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                setPercentageText(progress * 10)
                onSliderChangeListener?.onProgressChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
    }

    fun setProgress(value: Int) {
        binding.seekbar.progress = value
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
}