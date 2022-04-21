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

package com.kaleyra.collaboration_suite_glass_ui.settings.volume

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.GlassViewModel
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.settings.SliderFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * VolumeFragment
 */
internal class VolumeFragment :  SliderFragment() {

    private val viewModel: GlassViewModel by activityViewModels()

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(if (DeviceUtils.isRealWear) R.attr.kaleyra_volumeRealWearStyle else R.attr.kaleyra_volumeStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onServiceBound() {
        binding.kaleyraSlider.apply {
            val volume = viewModel.volume
            maxProgress = volume.max
            progress = volume.current
        }
    }

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also {
        with(binding.kaleyraSlider) {
            increaseProgress()
            viewModel.onSetVolume(progress)
        }
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also {
        with(binding.kaleyraSlider) {
            decreaseProgress()
            viewModel.onSetVolume(progress)
        }
    }
}
