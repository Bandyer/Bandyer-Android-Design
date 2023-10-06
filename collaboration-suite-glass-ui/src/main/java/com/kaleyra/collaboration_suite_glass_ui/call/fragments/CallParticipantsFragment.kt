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
import androidx.fragment.app.activityViewModels
import com.kaleyra.collaboration_suite.Participant
import com.kaleyra.collaboration_suite_core_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.collaboration_suite_glass_ui.call.CallViewModel
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItemData
import com.kaleyra.collaboration_suite_glass_ui.common.ParticipantsFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.takeWhile

/**
 * CallParticipantsFragment
 */
internal class CallParticipantsFragment : ParticipantsFragment() {

    private val viewModel: CallViewModel by activityViewModels()

    override val participants: List<Participant> by lazy { viewModel.participants.replayCache.firstOrNull()?.list ?: listOf() }

    private val args: CallParticipantsFragmentArgs by lazy {
        CallParticipantsFragmentArgs.fromBundle(
            requireActivity().intent!!.extras!!
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    override fun bindUI() {
        super.bindUI()
        binding.kaleyraUserInfo.hideState(true)

        repeatOnStarted {
            combine(
                viewModel.inCallParticipants,
                viewModel.participants
            ) { inCallParticipants, participants -> Pair(inCallParticipants, participants) }
                .takeWhile { it.first.isNotEmpty() }
                .collect { pair ->
                    val sortedList =
                        pair.first.sortedBy { pair.second.me?.userId != it.userId }
                    val items = sortedList.map { part ->
                        val data = ParticipantItemData(part.userId, part.combinedDisplayName.first() ?: "")
                        ParticipantItem(data)
                    }
                    FastAdapterDiffUtil[itemAdapter!!] =
                        FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                }
        }
    }
}


