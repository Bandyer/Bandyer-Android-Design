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

package com.kaleyra.collaboration_suite_glass_ui.call.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController

import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentEmptyBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.GlassDeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate

/**
 * EmptyFragment
 */
internal class EmptyFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentEmptyBinding? = null
    override val binding: KaleyraGlassFragmentEmptyBinding get() = _binding!!

    private val viewModel: CallViewModel by activityViewModels()

    /**
     * @suppress
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onHangup()
        }

        // Add view binding
        _binding = KaleyraGlassFragmentEmptyBinding
            .inflate(inflater, container, false)
            .apply { if(GlassDeviceUtils.isRealWear) kaleyraBottomNavigation.setListenersForRealwear() }

        return binding.root
    }

    override fun onServiceBound() = Unit

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToMenuFragment()) }

    override fun onSwipeDown() = true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToEndCallFragment()) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false
}
