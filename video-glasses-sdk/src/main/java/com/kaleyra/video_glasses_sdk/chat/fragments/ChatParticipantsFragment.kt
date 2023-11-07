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

package com.kaleyra.video_glasses_sdk.chat.fragments

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kaleyra.video.Participant
import com.kaleyra.video.conversation.ChatParticipant
import com.kaleyra.video_common_ui.contactdetails.ContactDetailsManager.combinedDisplayName
import com.kaleyra.video_glasses_sdk.call.adapter_items.ParticipantItem
import com.kaleyra.video_glasses_sdk.call.adapter_items.ParticipantItemData
import com.kaleyra.video_glasses_sdk.chat.GlassChatViewModel
import com.kaleyra.video_glasses_sdk.common.ParticipantsFragment
import com.kaleyra.video_glasses_sdk.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ChatParticipantsFragment : ParticipantsFragment() {

    private val viewModel: GlassChatViewModel by activityViewModels()

    private val args: ChatParticipantsFragmentArgs by lazy {
        ChatParticipantsFragmentArgs.fromBundle(
            requireActivity().intent!!.extras!!
        )
    }

    private var participantJob: Job? = null

    override val participants: List<Participant> by lazy { viewModel.participants.replayCache.firstOrNull()?.list ?: listOf() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    override fun bindUI() {
        super.bindUI()
        repeatOnStarted {
            val myUserId = viewModel.participants.replayCache.firstOrNull()?.me?.userId ?: return@repeatOnStarted
            viewModel.participants
                .onEach { it ->
                    val sortedList = it.list.sortedBy { myUserId != it.userId }
                    val items = sortedList.map { part ->
                        val data =
                            ParticipantItemData(part.userId, part.combinedDisplayName.first() ?: "")
                        ParticipantItem(data)
                    }
                    FastAdapterDiffUtil[itemAdapter!!] =
                        FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                }.launchIn(lifecycleScope)
        }
    }

    override fun onParticipantScrolled(userId: String) {
        val participant: ChatParticipant = viewModel.participants.replayCache.first().list.firstOrNull { it.userId == userId } ?: return
        participantJob?.cancel()
        participantJob = participant.state.onEach { binding.kaleyraUserInfo.setState(it) }.launchIn(lifecycleScope)
    }
}