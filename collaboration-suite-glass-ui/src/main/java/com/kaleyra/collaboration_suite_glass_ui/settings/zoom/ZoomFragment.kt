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

package com.kaleyra.collaboration_suite_glass_ui.settings.zoom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.GlassViewModel
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.SettingSlider
import com.kaleyra.collaboration_suite_glass_ui.settings.SliderFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import kotlin.math.roundToInt

/**
 * ZoomFragment
 */
internal class ZoomFragment : SliderFragment() {

    private val viewModel: GlassViewModel by activityViewModels()

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(if (DeviceUtils.isRealWear) R.attr.kaleyra_zoomRealWearStyle else R.attr.kaleyra_zoomStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onServiceBound() {
        with(binding.kaleyraSlider) {
            val currentValue = viewModel.zoom!!.value
            val upperValue = viewModel.zoom!!.range.upper
            val lowerValue = viewModel.zoom!!.range.lower
            maxProgress = MAX_ZOOM_PROGRESS
            progress =
                (((currentValue - lowerValue) * MAX_ZOOM_PROGRESS) / (upperValue - lowerValue)).roundToInt()

            onSliderChangeListener = object : SettingSlider.OnSliderChangeListener {
                override fun onProgressChanged(progress: Int) {
                    val percentage = progress.toFloat() / MAX_ZOOM_PROGRESS
                    val value = (percentage * (upperValue - lowerValue)) + lowerValue
                    viewModel.onSetZoom(value)
                }
            }
        }
    }

    override fun onTap() = true.also { findNavController().popBackStack() }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) =
        true.also { binding.kaleyraSlider.increaseProgress() }

    override fun onSwipeBackward(isKeyEvent: Boolean) =
        true.also { binding.kaleyraSlider.decreaseProgress() }

    private companion object {
        const val MAX_ZOOM_PROGRESS = 20
    }
}
