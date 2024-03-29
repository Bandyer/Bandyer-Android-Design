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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.call.GlassCallActivity
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.SettingSlider
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentZoomBinding
import kotlin.math.roundToInt

/**
 * ZoomFragment
 */
internal class ZoomFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentZoomBinding? = null
    override val binding: KaleyraGlassFragmentZoomBinding get() = _binding!!

    private val viewModel: CallViewModel by activityViewModels()

    private var previousValue = 0f

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
        _binding = KaleyraGlassFragmentZoomBinding
            .inflate(
                inflater,
                container,
                false
            ).apply {
                if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation)
                root.setOnTouchListener { _, _ -> true }
            }

        bindUI()
        return binding.root
    }

    fun bindUI() {
        with(binding.kaleyraSlider) {
            val zoom = viewModel.zoom.value ?: return@with
            val currentValue = zoom.value.value
            val upperValue = zoom.range.upper
            val lowerValue = zoom.range.lower
            maxProgress = MAX_ZOOM_PROGRESS
            progress = (((currentValue - lowerValue) * MAX_ZOOM_PROGRESS) / (upperValue - lowerValue)).roundToInt()
            previousValue = currentValue

            onSliderChangeListener = object : SettingSlider.OnSliderChangeListener {
                override fun onProgressChanged(progress: Int) {
                    val percentage = progress.toFloat() / MAX_ZOOM_PROGRESS
                    val value = (percentage * (upperValue - lowerValue)) + lowerValue
                    viewModel.onSetZoom(value)
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

    override fun onSwipeForward(isKeyEvent: Boolean) =
        true.also { binding.kaleyraSlider.increaseProgress() }

    override fun onSwipeBackward(isKeyEvent: Boolean) =
        true.also { binding.kaleyraSlider.decreaseProgress() }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        bottomNavView.setFirstItemListeners({ onSwipeBackward(true) }, null)
        bottomNavView.setSecondItemListener { onSwipeForward(true) }
        bottomNavView.setThirdItemListener { onSwipeDown() }
    }

    private companion object {
        const val MAX_ZOOM_PROGRESS = 20
    }
}
