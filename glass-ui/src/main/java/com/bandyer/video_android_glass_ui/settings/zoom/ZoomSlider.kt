package com.bandyer.video_android_glass_ui.settings.zoom

import android.content.Context
import android.util.AttributeSet
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.common.SettingSlider

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

    init {
        setSliderText(0)
    }

    override fun setSliderText(progress: Int) {
        binding.bandyerPercentage.text = resources.getString(R.string.bandyer_glass_slider_zoom_pattern, progress)
    }
}