package com.bandyer.video_android_glass_ui.settings.zoom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.common.SettingSlider
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassSliderLayoutBinding

/**
 *  Slider for the zoom fragment
 *
 * @constructor
 */
internal class ZoomSlider @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SettingSlider(context, attrs, defStyleAttr)  {

    override var binding: BandyerGlassSliderLayoutBinding = BandyerGlassSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initSeekbar()
        setSliderText(minProgress)
    }

    override fun setSliderText(progress: Int) {
        binding.bandyerPercentage.text = resources.getString(R.string.bandyer_glass_slider_zoom_pattern, progress * 10)
    }
}