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
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.takeWhile

/**
 * RingingFragment
 */
internal class RingingFragment : PreCallFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.kaleyra_ringingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onServiceBound() {
        super.onServiceBound()
        repeatOnStarted {
            viewModel.callState
                .takeWhile { it !is Call.State.Connecting }
                .onCompletion {
                    binding.kaleyraSubtitle.text = resources.getString(R.string.kaleyra_glass_connecting)
                    binding.kaleyraBottomNavigation.visibility = View.INVISIBLE
                }
                .launchIn(this)
        }
    }

    override fun onConnected() { findNavController().safeNavigate(RingingFragmentDirections.actionRingingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean) { binding.kaleyraSubtitle.text =  resources.getString(if(isGroupCall) R.string.kaleyra_glass_ringing_group else R.string.kaleyra_glass_ringing) }

    override fun onTap() = true.also { viewModel.onAnswer() }

    override fun onSwipeDown() = true.also { viewModel.onHangup() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(resources.displayMetrics.densityDpi / 2, 0) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraParticipantsScrollView.smoothScrollByWithAutoScroll(-resources.displayMetrics.densityDpi / 2, 0) }
}