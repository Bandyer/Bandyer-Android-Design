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
import com.bandyer.sdk_design.databinding.BandyerChatMessageLayoutBinding
import com.bandyer.sdk_design.databinding.BandyerFragmentChatBinding
import com.bandyer.sdk_design.extensions.parseToHHmm
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

abstract class SmartGlassChatFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentChatBinding? = null

    protected var itemAdapter: ItemAdapter<ChatItem>? = null
    protected var fastAdapter: FastAdapter<ChatItem>? = null

    protected var root: View? = null
    protected var rvMessages: RecyclerView? = null
    protected var counter: MaterialTextView? = null
    protected var bottomActionBar: BottomActionBarView? = null
    protected var chatMessageView: ChatMessageView? = null

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
        rvMessages!!.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMessages!!.adapter = fastAdapter
        rvMessages!!.isFocusable = true
        rvMessages!!.addItemDecoration(ChatItemIndicatorDecoration(requireContext()))

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvMessages)

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

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean

    protected fun addChatItem(data: SmartGlassChatData) =
        chatMessageView?.post {
            val binding = BandyerChatMessageLayoutBinding.bind(chatMessageView!!)
            with(binding) {
                bandyerName.text = data.name
                bandyerTime.text = data.time!!.parseToHHmm()
                bandyerMessage.text = data.message
                val pageList = bandyerMessage.paginate()
                for (i in pageList.indices) {
                    val pageData = SmartGlassChatData(
                        data.name,
                        data.userAlias,
                        pageList[i].toString(),
                        data.time,
                        data.avatar,
                        i == 0
                    )
                    itemAdapter!!.add(ChatItem(pageData))
                }
            }
        }

}