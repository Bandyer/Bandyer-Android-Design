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
import com.bandyer.sdk_design.extensions.parseToColor
import com.bandyer.sdk_design.new_smartglass.bottom_action_bar.BottomActionBarView
import com.bandyer.sdk_design.new_smartglass.menu.LineItemIndicatorDecoration
import com.bandyer.sdk_design.new_smartglass.menu.OffsetItemDecoration
import com.google.android.material.imageview.ShapeableImageView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

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

    protected var currentParticipantIndex = 0

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
        rvParticipants!!.setHasFixedSize(true)

        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rvParticipants)

        rvParticipants!!.addItemDecoration(
            LineItemIndicatorDecoration(
                requireContext(),
                snapHelper
            )
        )
        rvParticipants!!.addItemDecoration(OffsetItemDecoration())

        // add scroll listener
        rvParticipants!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val foundView = snapHelper.findSnapView(layoutManager) ?: return
                currentParticipantIndex = layoutManager.getPosition(foundView)
                val data = itemAdapter!!.getAdapterItem(currentParticipantIndex).data
                avatar!!.setText(data.name.first().toString())
                avatar!!.setBackground(data.userAlias.parseToColor())
                when {
                    data.avatarImageId != null -> avatar!!.setImage(data.avatarImageId)
                    data.avatarImageUrl != null -> avatar!!.setImage(data.avatarImageUrl)
                    else -> avatar!!.setImage(null)
                }
                if (data.avatarImageId != null) avatar!!.setImage(data.avatarImageId)
                else if (data.avatarImageUrl != null) avatar!!.setImage(data.avatarImageUrl)
                contactStateDot!!.isActivated =
                    data.userState == SmartGlassParticipantData.UserState.ONLINE
                with(contactStateText!!) {
                    when (data.userState) {
                        SmartGlassParticipantData.UserState.INVITED -> setContactState(
                            ContactStateTextView.State.INVITED
                        )
                        SmartGlassParticipantData.UserState.OFFLINE -> setContactState(
                            ContactStateTextView.State.LAST_SEEN,
                            data.lastSeenTime
                        )
                        SmartGlassParticipantData.UserState.ONLINE -> setContactState(
                            ContactStateTextView.State.ONLINE
                        )
                    }
                }
            }
        })

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
    }

    override fun onSmartGlassTouchEvent(event: SmartGlassTouchEvent): Boolean = when (event.type) {
        SmartGlassTouchEvent.Type.SWIPE_FORWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY) {
                rvParticipants!!.smoothScrollToNext(currentParticipantIndex)
                true
            } else false
        }
        SmartGlassTouchEvent.Type.SWIPE_BACKWARD -> {
            if(event.source == SmartGlassTouchEvent.Source.KEY) {
                rvParticipants!!.smoothScrollToPrevious(currentParticipantIndex)
                true
            } else false
        }
        else -> super.onSmartGlassTouchEvent(event)
    }
}


