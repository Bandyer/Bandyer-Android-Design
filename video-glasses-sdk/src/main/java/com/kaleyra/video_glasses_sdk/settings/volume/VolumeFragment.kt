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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.video_glasses_sdk.common.BaseFragment
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_glasses_sdk.bottom_navigation.BottomNavigationView
import com.kaleyra.video_glasses_sdk.call.CallViewModel
import com.kaleyra.video_glasses_sdk.common.SettingSlider
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassFragmentVolumeBinding

/**
 * VolumeFragment
 */
internal class VolumeFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentVolumeBinding? = null
    override val binding: KaleyraGlassFragmentVolumeBinding get() = _binding!!

    private val viewModel: CallViewModel by activityViewModels()

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Add view binding
        _binding = KaleyraGlassFragmentVolumeBinding
            .inflate(
                inflater,
                container,
                false
            )
            .apply {
                if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation)
                root.setOnTouchListener { _, _ -> true }
            }

        bindUI()
        return binding.root
    }

    fun bindUI() {
        binding.kaleyraSlider.apply {
            val volume = viewModel.volume
            maxProgress = volume.max
            progress = volume.current

            onSliderChangeListener = object : SettingSlider.OnSliderChangeListener {
                override fun onProgressChanged(progress: Int) {
                    viewModel.onSetVolume(progress)
                }
            }
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also {
        binding.kaleyraSlider.increaseProgress()
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also {
        binding.kaleyraSlider.decreaseProgress()
    }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setFirstItemListeners({ onSwipeBackward(true) }, null)
        bottomNavView.setSecondItemListener { onSwipeForward(true) }
        bottomNavView.setThirdItemListener { onSwipeDown() }
    }
}
