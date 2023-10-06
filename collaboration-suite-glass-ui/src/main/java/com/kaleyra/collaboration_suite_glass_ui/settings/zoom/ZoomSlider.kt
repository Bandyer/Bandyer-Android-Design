/*
 * Copyright 2023 Kaleyra @ https://www.kaleyra.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kaleyra.collaboration_suite_glass_ui.settings.zoom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassSliderLayoutBinding
import com.kaleyra.collaboration_suite_glass_ui.common.SettingSlider
import kotlin.math.roundToInt

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

    override var binding: KaleyraGlassSliderLayoutBinding = KaleyraGlassSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    init {
        initSeekbar()
        setSliderText(minProgress)
    }

    override fun setSliderText(progress: Int) {
        binding.kaleyraPercentage.text = resources.getString(R.string.kaleyra_glass_slider_zoom_pattern, ((progress.toFloat() / maxProgress) * 100).roundToInt())
    }
}