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

package com.kaleyra.video_glasses_sdk.settings.volume

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import com.kaleyra.video_glasses_sdk.common.SettingSlider
import com.kaleyra.video_glasses_sdk.R
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassSliderLayoutBinding

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

    override var binding: KaleyraGlassSliderLayoutBinding = KaleyraGlassSliderLayoutBinding.inflate(LayoutInflater.from(context), this, true)

    override var minProgress: Int = MIN_VALUE

    init {
        initSeekbar()
        setSliderText(minProgress)
    }

    override fun setSliderText(progress: Int) { binding.kaleyraPercentage.text = resources.getString(R.string.kaleyra_glass_slider_volume_pattern, progress) }

    companion object {
        const val MIN_VALUE = 1
    }
}