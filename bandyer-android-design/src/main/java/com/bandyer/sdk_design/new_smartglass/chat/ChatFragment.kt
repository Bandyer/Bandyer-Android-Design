package com.bandyer.sdk_design.new_smartglass.chat

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.bandyer.sdk_design.databinding.BandyerFragmentChatBinding
import com.bandyer.sdk_design.new_smartglass.BottomBarHolder
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEvent
import com.bandyer.sdk_design.new_smartglass.SmartGlassTouchEventListener
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

class ChatFragment : Fragment(), SmartGlassTouchEventListener, BottomBarHolder {

    //    private val activity by lazy { requireActivity() as MainActivity }
    private lateinit var binding: BandyerFragmentChatBinding

    // recycler view
    private val itemAdapter = ItemAdapter<ChatItem>()
    private val fastAdapter = FastAdapter.with(itemAdapter)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentChatBinding.inflate(inflater, container, false)

        // TODO handle this
//        activity.hideNotification()

        val messages = binding.messages
        messages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        messages.adapter = fastAdapter
        messages.isFocusable = true
        // TODO add decoration height through style
        messages.addItemDecoration(
            ChatItemIndicatorDecoration(
                requireContext(),
                Resources.getSystem().displayMetrics.density * 4
            )
        )

        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(messages)

        itemAdapter.add(ChatItem("Mario: Il numero seriale del macchinario dovrebbe essere AR56000TY7-1824\\nConfermi?"))
        itemAdapter.add(ChatItem("Francesco: La scatola è sulla sinistra"))
        itemAdapter.add(ChatItem("Gianfranco: Mi piacciono i treni"))

        return binding.root
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent.Event): Boolean =
        when (event) {
            SmartGlassTouchEvent.Event.SWIPE_DOWN -> {
                findNavController().popBackStack()
                true
            }
            else -> false
        }

    override fun showBottomBar() {
        binding.bottomActionBar.visibility = View.VISIBLE
    }

    override fun hideBottomBar() {
        binding.bottomActionBar.visibility = View.GONE
    }
}