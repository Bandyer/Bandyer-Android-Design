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

package com.kaleyra.video_glasses_sdk.call.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.video_common_ui.utils.DeviceUtils
import com.kaleyra.video_glasses_sdk.bottom_navigation.BottomNavigationView
import com.kaleyra.video_glasses_sdk.call.CallViewModel
import com.kaleyra.video_glasses_sdk.call.GlassCallActivity
import com.kaleyra.video_glasses_sdk.common.BaseFragment
import com.kaleyra.video_glasses_sdk.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.video_glasses_sdk.utils.extensions.horizontalSmoothScrollToPrevious
import com.kaleyra.video_glasses_sdk.utils.safeNavigate
import com.kaleyra.video_glasses_sdk.databinding.KaleyraGlassFragmentEmptyBinding
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * EmptyFragment
 */
internal class EmptyFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentEmptyBinding? = null
    override val binding: KaleyraGlassFragmentEmptyBinding get() = _binding!!

    private val viewModel: CallViewModel by activityViewModels()

    private val streams: RecyclerView get() = (requireActivity() as GlassCallActivity).rvStreams

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
            .apply { if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation) }

        viewModel.actions.onEach {
            if (it.isEmpty()) _binding?.kaleyraBottomNavigation?.hideSecondItem()
            else _binding?.kaleyraBottomNavigation?.showSecondItem()
        }.launchIn(lifecycleScope)

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() =
        true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToMenuFragment()) }

    override fun onSwipeDown() =
        true.also { findNavController().safeNavigate(EmptyFragmentDirections.actionEmptyFragmentToEndCallFragment()) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (!it) return@also
        val visibleItemPosition =
            (streams.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        streams.horizontalSmoothScrollToPrevious(visibleItemPosition)
    }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (!it) return@also
        val visibleItemPosition =
            (streams.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        streams.horizontalSmoothScrollToNext(visibleItemPosition)
    }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true) })
    }
}
