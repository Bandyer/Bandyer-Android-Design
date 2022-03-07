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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite_glass_ui.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.GlassViewModel
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentVolumeBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassDeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener

/**
 * VolumeFragment
 */
internal class VolumeFragment : BaseFragment(), TiltListener {

    private var _binding: KaleyraGlassFragmentVolumeBinding? = null
    override val binding: KaleyraGlassFragmentVolumeBinding get() = _binding!!

    private var deltaAzimuth = 0f

    private val args: VolumeFragmentArgs by lazy { VolumeFragmentArgs.fromBundle(requireActivity().intent!!.extras!!) }

    private val viewModel: GlassViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

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
            .inflate(inflater, container, false)
            .apply {
                if (GlassDeviceUtils.isRealWear) kaleyraBottomNavigation.setListenersForRealwear()
                root.setOnTouchListener { _, _ -> true }
            }

        return binding.root
    }

    override fun onServiceBound() {
        binding.kaleyraSlider.apply {
            val volume = viewModel.volume
            maxProgress = volume.max
            progress = volume.current
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) {
        this.deltaAzimuth += deltaAzimuth
        if (this.deltaAzimuth >= 2) onSwipeForward(true).also { this.deltaAzimuth = 0f }
        else if (this.deltaAzimuth <= -2) onSwipeBackward(true).also { this.deltaAzimuth = 0f }
    }

    override fun onTap() = true.also {
        viewModel.onSetVolume(binding.kaleyraSlider.progress)
        findNavController().popBackStack()
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = true.also { binding.kaleyraSlider.increaseProgress() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = true.also { binding.kaleyraSlider.decreaseProgress() }
}