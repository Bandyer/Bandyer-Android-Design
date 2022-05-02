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
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.doOnLayout
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.android_chat_sdk.persistence.entities.ChatMessage
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * ChatFragment
 */
internal class ChatFragment : BaseFragment<GlassChatActivity>(), TiltListener {

    private var _binding: KaleyraGlassFragmentChatBinding? = null
    override val binding: KaleyraGlassFragmentChatBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<ChatMessageItem>? = null

    private var currentMsgItemIndex = 0
//    private var newMessagesCounter = ObservableInt(-1)

    //    private var lastMsgIndex = 0
//    private var pagesIds = arrayListOf<String>()

    private val viewModel: ChatViewModel by activityViewModels()

    private val args: ChatFragmentArgs by lazy {
        requireActivity().intent?.extras?.let { ChatFragmentArgs.fromBundle(it)  } ?: ChatFragmentArgs()
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
        )
            .apply {
                if(DeviceUtils.isRealWear)
                    setListenersForRealWear(kaleyraBottomNavigation)

                // Init the RecyclerView
                kaleyraMessages.apply {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!)
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = PagerSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    setHasFixedSize(true)
                    addItemDecoration(ReadProgressDecoration(requireContext()))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        private var isLoading = false

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val lastVisibleItem = layoutManager.findLastVisibleItemPosition()
                            if (!isLoading && fastAdapter.itemCount <= (lastVisibleItem + LOAD_MORE_THRESHOLD)) {
                                viewModel.channel.fetch({
                                    isLoading = false
                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        "loaded previous messages",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }, {
                                    isLoading = false
                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        it,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                })
                                isLoading = true
                            }
//                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
//                            val currentMsgIndex = layoutManager.getPosition(foundView)

//                            if (currentMsgIndex > lastMsgIndex && pagesIds[currentMsgIndex] != pagesIds[lastMsgIndex]) {
//                                newMessagesCounter?.apply { set(get() - 1) }
//                                lastMsgIndex = currentMsgIndex
//                            }
//                            currentMsgItemIndex = currentMsgIndex
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> onTouchEvent(event) }
                }
            }

        return binding.root
    }

    override fun onServiceBound() {
        repeatOnStarted {
            viewModel.channel.messages
                .onEach { msgs ->
                    val pages = toChatMessagePages(msgs)
                    val items = pages.map { ChatMessageItem(it) }
                    FastAdapterDiffUtil[itemAdapter!!] =
                        FastAdapterDiffUtil.calculateDiff(itemAdapter!!, items, true)
                }.launchIn(this)
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
//        newMessagesCounter = 0
//        pagesIds = arrayListOf()
    }


//    override fun onShow() = Unit

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.kaleyraMessages.scrollBy((deltaAzimuth * requireContext().tiltScrollFactor()).toInt(), 0)

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

    private suspend fun toChatMessagePages(messages: List<ChatMessage>): List<ChatMessagePage> {
        val allPages = mutableListOf<ChatMessagePage>()
        messages.forEach {
            val user = viewModel.usersDescription.name(listOf(it.author))
            val avatar = viewModel.usersDescription.image(listOf(it.author))
            val pages = paginateMessage(user, it.messageBody, it.timestamp ?: 0L)
            for (i in pages.indices) {
                allPages.add(
                    ChatMessagePage(
                        it.messageSid,
                        it.author,
                        user,
                        avatar,
                        pages[i].toString(),
                        it.timestamp ?: 0L,
                        i == 0
                    )
                )
//                    pagesIds.add(id)
            }

        }
        return allPages
    }

    private suspend fun paginateMessage(user: String, message: String, timestamp: Long) =
        suspendCancellableCoroutine<List<CharSequence>> { continuation ->
            with(binding.kaleyraChatMessage) {
                root.doOnLayout {
                    kaleyraName.text = user
                    kaleyraTime.text = Iso8601.parseTimestamp(requireContext(), timestamp)
                    kaleyraMessage.text = message
                    kaleyraMessage.paginate { continuation.resume(it) }
                }
            }
        }

    private companion object {
        const val LOAD_MORE_THRESHOLD = 2
    }
}