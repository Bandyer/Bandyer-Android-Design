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

package com.kaleyra.collaboration_suite_glass_ui.call.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.view.postDelayed
import androidx.navigation.fragment.navArgs
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentFullScreenLogoDialogBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId

/**
 * CallEndedFragment
 */
internal class CallEndedFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: KaleyraGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    private val args: CallEndedFragmentArgs by navArgs()

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finishAndRemoveTask()
        }

        // Apply theme wrapper and add view binding
        val themeResId =
            requireActivity().theme.getAttributeResourceId(R.attr.kaleyra_callEndedStyle)
        _binding = KaleyraGlassFragmentFullScreenLogoDialogBinding.inflate(
            inflater.cloneInContext(android.view.ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        ).apply {
            if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation)
            kaleyraTitle.text = args.title
            kaleyraSubtitle.text = args.subtitle
            root.setOnTouchListener { _, _ -> true }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.postDelayed(AUTO_FINISH_TIME) { activity?.finishAndRemoveTask() }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap() = true.also { requireActivity().finishAndRemoveTask() }

    override fun onSwipeDown() = true.also { requireActivity().finishAndRemoveTask() }

    override fun onSwipeBackward(isKeyEvent: Boolean) = false

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    private companion object {
        const val AUTO_FINISH_TIME = 2000L
    }
}