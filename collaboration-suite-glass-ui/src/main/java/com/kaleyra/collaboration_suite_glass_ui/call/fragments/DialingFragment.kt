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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate

/**
 * DialingFragment
 */
internal class DialingFragment : PreCallFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.kaleyra_dialingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onConnected() { findNavController().safeNavigate(DialingFragmentDirections.actionDialingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean, isLink: Boolean) {
        binding.kaleyraSubtitle.text = resources.getString(
            when {
                isLink -> R.string.kaleyra_glass_connecting
                isGroupCall -> R.string.kaleyra_glass_dialing_group
                else -> R.string.kaleyra_glass_dialing
            }
        )
    }

    override fun onTap() = false

    override fun onSwipeDown() = true.also { viewModel.onHangup() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(resources.displayMetrics.densityDpi / 2, 0) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(-resources.displayMetrics.densityDpi / 2, 0) }
}
