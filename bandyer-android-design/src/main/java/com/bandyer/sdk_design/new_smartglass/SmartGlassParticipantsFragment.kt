package com.bandyer.sdk_design.new_smartglass

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.databinding.BandyerFragmentParticipantsBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.bandyer.sdk_design.new_smartglass.menu.LineItemIndicatorDecoration
import com.bandyer.sdk_design.new_smartglass.menu.OffsetItemDecoration
import com.google.android.material.imageview.ShapeableImageView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * SmartGlassParticipantsFragment. A base class for the participants fragment.
 */
abstract class SmartGlassParticipantsFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentParticipantsBinding? = null

    protected var itemAdapter: ItemAdapter<ParticipantItem>? = null
    protected var fastAdapter: FastAdapter<ParticipantItem>? = null

    protected var root: View? = null
    protected var avatar: ContactAvatarView? = null
    protected var contactStateDot: ShapeableImageView? = null
    protected var contactStateText: ContactStateTextView? = null
    protected var rvParticipants: RecyclerView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    protected var snapHelper: LinearSnapHelper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentParticipantsBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        avatar = binding!!.bandyerAvatar
        contactStateDot = binding!!.bandyerContactStateDot
        contactStateText = binding!!.bandyerContactStateText
        rvParticipants = binding!!.bandyerParticipants
        bottomActionBar = binding!!.bandyerBottomActionBar

        // init the recycler view
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvParticipants!!.layoutManager = layoutManager
        rvParticipants!!.adapter = fastAdapter
        rvParticipants!!.isFocusable = false

        snapHelper = LinearSnapHelper()
        snapHelper!!.attachToRecyclerView(rvParticipants)

        rvParticipants!!.addItemDecoration(
            LineItemIndicatorDecoration(
                requireContext(),
                snapHelper!!
            )
        )
        rvParticipants!!.addItemDecoration(OffsetItemDecoration())

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvParticipants!!.onTouchEvent(event) }

        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvParticipants = null
        bottomActionBar = null
        avatar = null
        contactStateDot = null
        contactStateText = null
        snapHelper = null
    }
}


