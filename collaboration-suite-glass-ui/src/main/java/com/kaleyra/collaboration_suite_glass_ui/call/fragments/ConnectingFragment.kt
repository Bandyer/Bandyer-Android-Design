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
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import com.kaleyra.collaboration_suite.phonebox.Call

import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.call.GlassCallActivity
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentFullScreenLogoDialogBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile

internal abstract class ConnectingFragment : BaseFragment() {

    private var _binding: KaleyraGlassFragmentFullScreenLogoDialogBinding? = null
    override val binding: KaleyraGlassFragmentFullScreenLogoDialogBinding get() = _binding!!

    protected val viewModel: CallViewModel by activityViewModels()

    abstract val themeResId: Int

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
        _binding = KaleyraGlassFragmentFullScreenLogoDialogBinding
            .inflate(
                inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
                container,
                false
            ).apply {
                if (DeviceUtils.isRealWear) setListenersForRealWear(kaleyraBottomNavigation)
            }

        bindUI()
        return binding.root
    }

    open fun bindUI() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            viewModel.onHangup()
        }

        repeatOnStarted {
            combine(viewModel.callState, viewModel.amIAlone) { state, alone ->
                if (state !is Call.State.Connected || alone) return@combine
                onConnected()
            }.launchIn(this@repeatOnStarted)

            viewModel.inCallParticipants
                .takeWhile { it.count() < 2 }
                .onCompletion {
                    binding.kaleyraSubtitle.text =
                        resources.getString(R.string.kaleyra_glass_connecting)
                }.launchIn(this@repeatOnStarted)

            viewModel.participants
                .onEach {
                    binding.kaleyraCounter.visibility =
                        if (it.others.count() + 1 > 2) View.VISIBLE else View.GONE
                }
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

    abstract fun onConnected()

    abstract fun setSubtitle(isGroupCall: Boolean, isLink: Boolean)
}