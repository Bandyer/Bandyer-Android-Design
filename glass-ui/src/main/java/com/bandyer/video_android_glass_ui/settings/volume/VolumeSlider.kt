package com.bandyer.video_android_glass_ui.settings.volume

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.SeekBar
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.common.SettingSlider
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassSliderLayoutBinding

/**
 *  Slider for the volume fragment
 *
 * @constructor
 */
internal class VolumeSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SettingSlider(context, attrs, defStyleAttr) {

    override var binding: BandyerGlassSliderLayoutBinding = BandyerGlassSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override var minProgress: Int = MIN_VALUE

    init {
        initSeekbar()
        setSliderText(minProgress)
    }

    override fun setSliderText(progress: Int) { binding.bandyerPercentage.text = resources.getString(R.string.bandyer_glass_slider_volume_pattern, progress) }

    companion object {
        const val MIN_VALUE = 1
    }
}