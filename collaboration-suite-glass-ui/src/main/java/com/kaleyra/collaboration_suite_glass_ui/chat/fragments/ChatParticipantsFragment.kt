package com.kaleyra.collaboration_suite_glass_ui.chat.fragments

import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.kaleyra.collaboration_suite.chatbox.ChatParticipant
import com.kaleyra.collaboration_suite_core_ui.model.UsersDescription
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItem
import com.kaleyra.collaboration_suite_glass_ui.call.adapter_items.ParticipantItemData
import com.kaleyra.collaboration_suite_glass_ui.chat.GlassChatViewModel
import com.kaleyra.collaboration_suite_glass_ui.common.ParticipantsFragment
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class ChatParticipantsFragment : ParticipantsFragment() {

    private val viewModel: GlassChatViewModel by activityViewModels()

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