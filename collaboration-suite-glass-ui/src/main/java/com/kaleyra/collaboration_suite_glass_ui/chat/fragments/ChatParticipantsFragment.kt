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

    private val usersStates: HashMap<String, ChatParticipant.State> = hashMapOf()

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

            val stateJobs: MutableList<Job> = mutableListOf()
            viewModel.participants
                .onEach { parts ->
                    stateJobs.forEach {
                        it.cancel()
                        it.join()
                    }
                    stateJobs.clear()
                    parts.list.forEach { part ->
                        val job = part.state.onEach {usersStates[part.userId] = it }.launchIn(lifecycleScope)
                        stateJobs.add(job)
                    }
                }
                .launchIn(lifecycleScope)
        }
    }

    override fun onParticipantScrolled(userId: String) {
        usersStates[userId]?.let { binding.kaleyraUserInfo.setState(it) }
    }
}