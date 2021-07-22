package com.bandyer.sdk_design.new_smartglass.chat

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.databinding.BandyerFragmentChatBinding
import com.bandyer.sdk_design.new_smartglass.SmartGlassBaseFragment
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

abstract class SmartGlassChatFragment : SmartGlassBaseFragment() {

    private lateinit var binding: BandyerFragmentChatBinding

    protected val itemAdapter = ItemAdapter<ChatItem>()
    protected val fastAdapter = FastAdapter.with(itemAdapter)

    protected lateinit var root: View
    protected lateinit var rvMessages: RecyclerView
    protected lateinit var bottomActionBar: BottomActionBarView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentChatBinding.inflate(inflater, container, false)

        // set the views
        root = binding.root
        rvMessages = binding.messages
        bottomActionBar = binding.bottomActionBar

        rvMessages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvMessages.adapter = fastAdapter
        rvMessages.isFocusable = true
        // TODO add decoration height through style
        rvMessages.addItemDecoration(
            ChatItemIndicatorDecoration(
                requireContext(),
                Resources.getSystem().displayMetrics.density * 4
            )
        )

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rvMessages)

        // pass the root view's touch event to the recycler view
        root.setOnTouchListener { _, event -> rvMessages.onTouchEvent(event) }
        return root
    }

    abstract override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean
}