package com.bandyer.video_android_glass_ui.chat

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.*
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentChatBinding
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * BandyerGlassChatFragment. A base class for the chat fragment.
 */
abstract class ChatFragment : BaseFragment() {

    private var binding: BandyerFragmentChatBinding? = null

    protected var messageItemAdapter: ItemAdapter<ChatMessageItem>? = null
    protected var fastAdapter: FastAdapter<ChatMessageItem>? = null
    protected var root: View? = null
    protected var rvMessages: RecyclerView? = null
    protected var counter: MaterialTextView? = null
    protected var bottomNavigation: BottomNavigationView? = null
    protected var chatMessageView: ChatMessageView? = null

    protected var snapHelper: PagerSnapHelper? = null

    /**
     * @suppress
     */
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
        bottomNavigation = binding!!.bandyerBottomActionBar
        chatMessageView = binding!!.bandyerChatMessage

        // init the recycler view
        messageItemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(messageItemAdapter!!)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMessages!!.apply {
            this.layoutManager = layoutManager
            this.adapter = fastAdapter
            this.isFocusable = false
            this.setHasFixedSize(true)
            this.addItemDecoration(ChatReadProgressDecoration(requireContext()))
        }

        snapHelper = PagerSnapHelper().apply {
            attachToRecyclerView(rvMessages)
        }
        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvMessages!!.onTouchEvent(event) }
        return root!!
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        messageItemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvMessages = null
        counter = null
        bottomNavigation = null
        chatMessageView = null
        snapHelper = null
    }
}