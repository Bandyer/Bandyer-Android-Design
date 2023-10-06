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

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentChatMenuBinding
import com.kaleyra.collaboration_suite.conference.Call
import com.kaleyra.collaboration_suite_core_ui.ChatUI
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_glass_ui.*
import com.kaleyra.collaboration_suite_glass_ui.bottom_navigation.BottomNavigationView
import com.kaleyra.collaboration_suite_glass_ui.chat.ChatAction
import com.kaleyra.collaboration_suite_glass_ui.chat.GlassChatViewModel
import com.kaleyra.collaboration_suite_glass_ui.chat.menu.ChatMenuItem
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.kaleyra.collaboration_suite_glass_ui.common.item_decoration.MenuProgressIndicator
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getChatThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.kaleyra.collaboration_suite_glass_ui.utils.safeNavigate
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * ChatMenuFragment
 */
internal class ChatMenuFragment : BaseFragment(), TiltListener {

    private var _binding: KaleyraGlassFragmentChatMenuBinding? = null
    override val binding: KaleyraGlassFragmentChatMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMenuItem>? = null

    private val viewModel: GlassChatViewModel by activityViewModels()

    private val args: ChatMenuFragmentArgs by navArgs()

    private var actionIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(args.enableTilt) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Apply theme wrapper and add view binding
        val themeResId = requireContext().getChatThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Chat_kaleyra_chatMenuStyle)
        _binding = KaleyraGlassFragmentChatMenuBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireActivity(), themeResId)),
            container,
            false
        ).apply {
            if(DeviceUtils.isRealWear)
                setListenersForRealWear(kaleyraBottomNavigation)

            // Init the RecyclerView
            with(kaleyraActions) {
                itemAdapter = ItemAdapter()
                val fastAdapter = FastAdapter.with(itemAdapter!!)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                this.layoutManager = layoutManager
                adapter = fastAdapter
                isFocusable = false
                setHasFixedSize(true)

                addItemDecoration(HorizontalCenterItemDecoration())
                addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper))

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val foundView = snapHelper.findSnapView(layoutManager) ?: return
                        actionIndex = layoutManager.getPosition(foundView)
                    }
                })

                // Forward the root view's touch event to the recycler view
                root.setOnTouchListener { _, event -> onTouchEvent(event) }
            }
        }

        getActions(viewModel.actions.replayCache[0]).map { ChatMenuItem(it) }.also { itemAdapter!!.add(it) }
        return binding.root
    }

    private fun getActions(actions: Set<ChatUI.Action>): List<ChatAction> = ChatAction.getActions(
        withParticipants = actions.any { it is ChatUI.Action.ShowParticipants },
        withVideoCall = actions.any { it is ChatUI.Action.CreateCall && it.preferredType.isVideoEnabled() },
        withCall = actions.any { it is ChatUI.Action.CreateCall  && !it.preferredType.isVideoEnabled() },
    )

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraActions.scrollBy((deltaAzimuth * requireContext().tiltScrollFactor()).toInt(), 0)

    override fun onTap() = onTap(itemAdapter!!.getAdapterItem(actionIndex).action)

    private fun onTap(action: ChatAction) = when (action) {
        is ChatAction.PARTICIPANTS -> true.also {
            findNavController().safeNavigate(
                ChatMenuFragmentDirections.actionChatMenuFragmentToChatParticipantsFragment()
            )
        }
        is ChatAction.VIDEOCALL, is ChatAction.CALL -> true.also {
            val userId = viewModel.chat.replayCache.first().participants.value.others.first().userId
            val conference = viewModel.conference.replayCache.firstOrNull() ?: return@also
            conference.call(listOf(userId)) {
                if (action is ChatAction.CALL) preferredType = Call.PreferredType.audioUpgradable()
            }
            findNavController().popBackStack()
        }
        else -> false
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraActions.horizontalSmoothScrollToNext(actionIndex) }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also { if(it) binding.kaleyraActions.horizontalSmoothScrollToPrevious(actionIndex) }

    override fun setListenersForRealWear(bottomNavView: BottomNavigationView) {
        super.setListenersForRealWear(bottomNavView)
        bottomNavView.setFirstItemListeners({ onSwipeForward(true) }, { onSwipeBackward(true) })
    }
}