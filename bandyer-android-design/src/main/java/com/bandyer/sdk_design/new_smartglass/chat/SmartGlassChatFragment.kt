package com.bandyer.sdk_design.new_smartglass.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.R
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.sdk_design.databinding.BandyerFragmentChatBinding
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.bandyer.sdk_design.new_smartglass.smoothScrollToNext
import com.bandyer.sdk_design.new_smartglass.smoothScrollToPrevious
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * SmartGlassChatFragment
 */
abstract class SmartGlassChatFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentChatBinding? = null

    protected var itemAdapter: ItemAdapter<ChatItem>? = null
    protected var fastAdapter: FastAdapter<ChatItem>? = null
    protected var root: View? = null
    protected var rvMessages: RecyclerView? = null
    protected var counter: MaterialTextView? = null
    protected var bottomActionBar: BottomActionBarView? = null
    protected var chatMessageView: ChatMessageView? = null

    protected var currentMsgItemIndex = 0
    private var newMessagesCounter = 0
        set(value) {
            field = value
            val counterValue = value - 1
            counter?.text = resources.getString(
                R.string.bandyer_smartglass_message_counter_pattern,
                counterValue
            )
            counter?.visibility = if (counterValue > 0) View.VISIBLE else View.GONE
        }
    private var lastMsgIndex = 0
    private var pagesIds = arrayListOf<String>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentChatBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        rvMessages = binding!!.bandyerMessages
        counter = binding!!.bandyerCounter
        bottomActionBar = binding!!.bandyerBottomActionBar
        chatMessageView = binding!!.bandyerChatMessage

        // init the recycler view
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMessages!!.layoutManager = layoutManager
        rvMessages!!.adapter = fastAdapter
        rvMessages!!.isFocusable = false
        rvMessages!!.setHasFixedSize(true)

        rvMessages!!.addItemDecoration(ChatItemIndicatorDecoration(requireContext()))

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvMessages)

        rvMessages!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                val currentMsgIndex = layoutManager.getPosition(foundView)
                if (currentMsgIndex > lastMsgIndex && pagesIds[currentMsgIndex] != pagesIds[lastMsgIndex]) {
                    newMessagesCounter--
                    lastMsgIndex = currentMsgIndex
                }
                currentMsgItemIndex = currentMsgIndex
            }
        })

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvMessages!!.onTouchEvent(event) }
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvMessages = null
        counter = null
        bottomActionBar = null
        chatMessageView = null
    }

    /**
     * Add a chat item to the recycler view. If the text is too long to fit in one screen, more than a chat item will be added
     *
     * @param data The [SmartGlassMessageData]
     */
    protected fun addChatItem(data: SmartGlassMessageData) {
        newMessagesCounter++
        chatMessageView?.post {
            val binding = BandyerChatMessageLayoutBinding.bind(chatMessageView!!)
            with(binding) {
                bandyerName.text = data.sender
                bandyerTime.text = data.time!!.parseToHHmm()
                bandyerMessage.text = data.message
                val pageList = bandyerMessage.paginate()
                for (i in pageList.indices) {
                    val pageData = SmartGlassMessageData(
                        data.id,
                        data.sender,
                        data.userAlias,
                        pageList[i].toString(),
                        data.time,
                        data.userAvatarId,
                        data.userAvatarUrl,
                        i == 0
                    )
                    itemAdapter!!.add(ChatItem(pageData))
                    pagesIds.add(data.id)
                }
            }
        }
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY) {
                rvMessages!!.smoothScrollToNext(currentMsgItemIndex)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY) {
                rvMessages!!.smoothScrollToPrevious(currentMsgItemIndex)
                true
            } else false
        }
        else -> super.onSmartGlassTouchEvent(event)
    }

}