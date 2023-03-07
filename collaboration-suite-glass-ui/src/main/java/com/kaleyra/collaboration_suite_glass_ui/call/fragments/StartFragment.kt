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
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.kaleyra.collaboration_suite.phonebox.Call
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentStartBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.takeWhile

/**
 * StartFragment, used as start destination in the nav graph
 */
internal class StartFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentStartBinding? = null
    override val binding: KaleyraGlassFragmentStartBinding get() = _binding!!

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

        _binding = KaleyraGlassFragmentStartBinding.inflate(inflater, container, false)
        bindUI()
        return binding.root
    }

    fun bindUI() {
        repeatOnStarted {
            viewModel.callState
                .combine(viewModel.participants) { state, participants ->
                    when {
                        state is Call.State.Connecting && participants.me == participants.creator()  ->
                            findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToDialingFragment())
                        state == Call.State.Disconnected && participants.me != participants.creator() ->
                            findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToRingingFragment())
                        state is Call.State.Connected ->
                            findNavController().safeNavigate(StartFragmentDirections.actionStartFragmentToEmptyFragment())
                        else -> Unit
                    }
                }
                .takeWhile { it != Call.State.Connected }
                .launchIn(this@repeatOnStarted)
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onTap(): Boolean = false

    override fun onSwipeDown(): Boolean = false

    override fun onSwipeForward(isKeyEvent: Boolean): Boolean = false

    override fun onSwipeBackward(isKeyEvent: Boolean): Boolean = false
}