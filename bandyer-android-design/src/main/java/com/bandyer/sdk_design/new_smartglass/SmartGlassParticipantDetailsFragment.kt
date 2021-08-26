package com.bandyer.sdk_design.new_smartglass

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.sdk_design.databinding.BandyerFragmentParticipantDetailsBinding
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.bandyer.sdk_design.new_smartglass.menu.ItemIndicatorDecoration
import com.bandyer.sdk_design.new_smartglass.menu.OffsetItemDecoration
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

abstract class SmartGlassParticipantDetailsFragment : SmartGlassBaseFragment() {

    private var binding: BandyerFragmentParticipantDetailsBinding? = null

    protected var itemAdapter: ItemAdapter<ParticipantDetailsItem>? = null
    protected var fastAdapter: FastAdapter<ParticipantDetailsItem>? = null

    protected var root: View? = null
    protected var avatar: ContactAvatarView? = null
    protected var contactStateDot: ShapeableImageView? = null
    protected var contactStateText: ContactStateTextView? = null
    protected var name: MaterialTextView? = null
    protected var rvActions: RecyclerView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    protected var actionIndex = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BandyerFragmentParticipantDetailsBinding.inflate(inflater, container, false)

        // set the views
        root = binding!!.root
        avatar = binding!!.bandyerAvatar
        contactStateDot = binding!!.bandyerContactStateDot
        contactStateText = binding!!.bandyerContactStateText
        name = binding!!.bandyerName
        rvActions = binding!!.bandyerParticipants
        bottomActionBar = binding!!.bandyerBottomActionBar

        // init the recycler view
        itemAdapter = ItemAdapter()
        fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvActions!!.layoutManager = layoutManager
        rvActions!!.adapter = fastAdapter
        rvActions!!.clipToPadding = false
        rvActions!!.isFocusable = true

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvActions)

        rvActions!!.addItemDecoration(
            ItemIndicatorDecoration(
                requireContext(),
                snapHelper
            )
        )
        rvActions!!.addItemDecoration(OffsetItemDecoration())

        // add scroll listener
        rvActions!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                actionIndex = layoutManager.getPosition(foundView)
            }
        })

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvActions!!.onTouchEvent(event) }
        return root!!
    }

    override fun onDestroyView() {
        super.onDestroyView()
        itemAdapter = null
        fastAdapter = null
        binding = null
        root = null
        rvActions = null
        name = null
        bottomActionBar = null
        avatar = null
        contactStateDot = null
        contactStateText = null
    }
}