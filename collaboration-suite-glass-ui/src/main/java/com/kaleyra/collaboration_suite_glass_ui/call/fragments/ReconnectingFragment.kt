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
 * ConnectingFragment
 */
internal class ReconnectingFragment : ConnectingFragment() {

    override var themeResId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        themeResId = requireActivity().theme.getAttributeResourceId(R.attr.kaleyra_reconnectingStyle)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onConnected() { findNavController().safeNavigate(ReconnectingFragmentDirections.actionReconnectingFragmentToEmptyFragment()) }

    override fun setSubtitle(isGroupCall: Boolean, isLink: Boolean) { binding.kaleyraSubtitle.text = resources.getString(R.string.kaleyra_glass_connecting) }

    override fun onTap() = false

    override fun onSwipeDown() = true.also { findNavController().safeNavigate(ReconnectingFragmentDirections.actionReconnectingFragmentToEndCallFragment()) }

    override fun onSwipeForward(isKeyEvent: Boolean) = false

    override fun onSwipeBackward(isKeyEvent: Boolean) = false
}