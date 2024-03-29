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

package com.kaleyra.collaboration_suite_glass_ui.chat.fragments

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItemData
import com.kaleyra.collaboration_suite_glass_ui.chat.ChatViewModel
import com.kaleyra.collaboration_suite_glass_ui.common.ParticipantsFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ChatParticipantsFragment : ParticipantsFragment() {

    private val viewModel: ChatViewModel by activityViewModels()

    private val args: ChatParticipantsFragmentArgs by lazy {
        ChatParticipantsFragmentArgs.fromBundle(
            requireActivity().intent!!.extras!!
        )
    }

    override val usersDescription: UsersDescription
        get() = viewModel.usersDescription

    private var participantJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
    }

    override fun bindUI() {
        super.bindUI()
        repeatOnStarted {
            val myUserId = viewModel.participants.replayCache.firstOrNull()?.me?.userId
                ?: return@repeatOnStarted
            viewModel.participants
                .onEach {
                    val sortedList = it.list.sortedBy { myUserId != it.userId }
                    val items = sortedList.map { part ->
                        val data = part.userId.let {
                            ParticipantItemData(
                                it,
                                viewModel.usersDescription.name(listOf(it))
                            )
                        }
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