package com.bandyer.video_android_glass_ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentChatBinding
import com.bandyer.video_android_glass_ui.BandyerGlassBaseFragment
import com.bandyer.video_android_glass_ui.bottom_action_bar.BandyerBottomActionBarView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * BandyerGlassChatFragment. A base class for the chat fragment.
 */
abstract class BandyerGlassChatFragment : BandyerGlassBaseFragment() {

    private var binding: BandyerFragmentChatBinding? = null

    protected var itemAdapter: ItemAdapter<BandyerChatItem>? = null
    protected var fastAdapter: FastAdapter<BandyerChatItem>? = null
    protected var root: View? = null
    protected var rvMessages: RecyclerView? = null
    protected var counter: MaterialTextView? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null
    protected var chatMessageView: BandyerChatMessageView? = null

    protected var snapHelper: PagerSnapHelper? = null

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
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMessages!!.layoutManager = layoutManager
        rvMessages!!.adapter = fastAdapter
        rvMessages!!.isFocusable = false
        rvMessages!!.setHasFixedSize(true)

        rvMessages!!.addItemDecoration(BandyerChatProgressDecoration(requireContext()))

        snapHelper = PagerSnapHelper()
        snapHelper!!.attachToRecyclerView(rvMessages)

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
        snapHelper = null
    }
}