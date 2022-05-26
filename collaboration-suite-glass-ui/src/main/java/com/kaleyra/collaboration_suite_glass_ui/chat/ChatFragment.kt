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

package com.kaleyra.collaboration_suite_glass_ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.doOnLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kaleyra.collaboration_suite.chatbox.Message
import com.kaleyra.collaboration_suite_core_ui.utils.DeviceUtils
import com.kaleyra.collaboration_suite_core_ui.utils.Iso8601
import com.kaleyra.collaboration_suite_glass_ui.R
import com.kaleyra.collaboration_suite_glass_ui.common.BaseFragment
import com.kaleyra.collaboration_suite_glass_ui.common.ReadProgressDecoration
import com.kaleyra.collaboration_suite_glass_ui.databinding.KaleyraGlassFragmentChatBinding
import com.kaleyra.collaboration_suite_glass_ui.utils.TiltListener
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.getChatThemeAttribute
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.ContextExtensions.tiltScrollFactor
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.kaleyra.collaboration_suite_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import com.mikepenz.fastadapter.diff.FastAdapterDiffUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ChatFragment
 */
internal class ChatFragment : BaseFragment<GlassChatActivity>(), TiltListener {

    private var _binding: KaleyraGlassFragmentChatBinding? = null
    override val binding: KaleyraGlassFragmentChatBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMessageItem>? = null

    private var currentMsgItemIndex = -1
    private var unreadMessagesIds = listOf<String>()
        set(value) {
            field = value
            updateCounter(value.count())
        }

    private val viewModel: ChatViewModel by activityViewModels()

    private val args: ChatFragmentArgs by lazy {
        requireActivity().intent?.extras?.let { ChatFragmentArgs.fromBundle(it) }
            ?: ChatFragmentArgs()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (args.enableTilt) tiltListener = this
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

        val themeResId =
            requireContext().getChatThemeAttribute(R.styleable.KaleyraCollaborationSuiteUI_Theme_Glass_Chat_kaleyra_chatStyle)
        // Add view binding
        _binding = KaleyraGlassFragmentChatBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireActivity(), themeResId)),
            container,
            false
        ).apply {

            if (DeviceUtils.isRealWear)
                setListenersForRealWear(kaleyraBottomNavigation)

            // Init the RecyclerView
            kaleyraMessages.apply {
                val snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(this) }
                itemAdapter = ItemAdapter()
                val fastAdapter = FastAdapter.with(itemAdapter!!)
                val layoutManager =
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    private var isLoading = false

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val foundView = snapHelper.findSnapView(layoutManager) ?: return
                        val position = layoutManager.getPosition(foundView)
                        if (currentMsgItemIndex == position) return
                        currentMsgItemIndex = position

//                        if (!isLoading && fastAdapter.itemCount <= (currentPosition + LOAD_MORE_THRESHOLD)) {
//                            viewModel.chat.fetch(LOAD_MORE_THRESHOLD) { isLoading = false }
//                            isLoading = true
//                        }

                        val messageId =
                            itemAdapter!!.getAdapterItem(currentMsgItemIndex).page.messageId
                        viewModel.chat.messages.value.other.firstOrNull { it.id == messageId }
                            ?.markAsRead()
                        unreadMessagesIds = unreadMessagesIds - messageId
                    }
                })

                this.layoutManager = layoutManager
                adapter = fastAdapter
                isFocusable = false
                setHasFixedSize(true)
                addItemDecoration(ReadProgressDecoration(requireContext()))

                // Forward the root view's touch event to the recycler view
                root.setOnTouchListener { _, event -> onTouchEvent(event) }
            }
        }

        return binding.root
    }

    override fun onServiceBound() {
        unreadMessagesIds =
            viewModel.chat.messages.value.other.filter { it.state.value is Message.State.Received }
                .map { it.id }

        repeatOnStarted {
            viewModel.chat.messages
                .onEach { msgs ->
                    binding.kaleyraNoMessages.visibility =
                        if (msgs.list.isEmpty()) View.VISIBLE else View.GONE
                    toChatMessagePages(this@repeatOnStarted, msgs.list) { pages ->
                        val items = pages.map { ChatMessageItem(it) }
                        FastAdapterDiffUtil[itemAdapter!!] =
                            FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                    }
                }.launchIn(this@repeatOnStarted)
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraMessages.scrollBy(
            (deltaAzimuth * requireContext().tiltScrollFactor()).toInt(),
            0
        )

    override fun onTap() = true.also {
//        val username = itemAdapter!!.adapterItems[currentMsgItemIndex].page.userId
//        val action = ChatFragmentDirections.actionChatFragmentToChatMenuFragment(args.enableTilt)
//        findNavController().navigate(action)
    }

    override fun onSwipeDown() = true.also { requireActivity().finishAndRemoveTask() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMessages.horizontalSmoothScrollToNext(currentMsgItemIndex)
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.kaleyraMessages.horizontalSmoothScrollToPrevious(currentMsgItemIndex)
    }

    private fun toChatMessagePages(
        scope: CoroutineScope,
        messages: List<Message>,
        callback: (List<ChatMessagePage>) -> Unit
    ) {
        binding.kaleyraChatMessage.root.doOnLayout {
            scope.launch {
                val allPages = mutableListOf<ChatMessagePage>()
                messages.forEach {
                    val user = viewModel.usersDescription.name(listOf(it.creator.userId))
                    val avatar = viewModel.usersDescription.image(listOf(it.creator.userId))
                    val pages = paginateMessage(user, it.content, it.creationDate.time)
                    for (i in pages.indices) {
                        allPages.add(
                            ChatMessagePage(
                                it.id,
                                it.creator.userId,
                                user,
                                avatar,
                                pages[i].toString(),
                                it.creationDate.time,
                                i == 0
                            )
                        )
//                    pagesIds.add(id)
                    }
                }
                callback.invoke(allPages)
            }
        }
    }

    private fun paginateMessage(
        user: String,
        content: Message.Content,
        timestamp: Long
    ): List<CharSequence> =
        with(binding.kaleyraChatMessage) {
            kaleyraName.text = user
            kaleyraTime.text = Iso8601.parseTimestamp(requireContext(), timestamp)
            kaleyraMessage.text = if (content is Message.Content.Text) content.message else ""
            return kaleyraMessage.paginate()
        }

    private fun updateCounter(count: Int) = with(binding.kaleyraCounter) {
        visibility = if (count > 0) {
            text = resources.getString(R.string.kaleyra_glass_message_counter_pattern, count)
            View.VISIBLE
        } else View.GONE
    }

    private companion object {
        const val LOAD_MORE_THRESHOLD = 3
    }
}