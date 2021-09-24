package com.bandyer.video_android_glass_ui.call.participants

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentParticipantsBinding
import com.bandyer.video_android_glass_ui.bottom_action_bar.BottomActionBarView
import com.bandyer.video_android_glass_ui.common.AvatarView
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.participants.ParticipantStateTextView
import com.google.android.material.imageview.ShapeableImageView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * SmartGlassParticipantsFragment. A base class for the participants fragment.
 */
abstract class ParticipantsFragment : BaseFragment() {

    private var binding: BandyerFragmentParticipantsBinding? = null

    protected var itemAdapter: ItemAdapter<CallParticipantItem>? = null
    protected var fastAdapter: FastAdapter<CallParticipantItem>? = null

    protected var root: View? = null
    protected var avatar: AvatarView? = null
    protected var contactStateDot: ShapeableImageView? = null
    protected var contactStateText: ParticipantStateTextView? = null
    protected var rvParticipants: RecyclerView? = null
    protected var bottomActionBar: BottomActionBarView? = null

    protected var snapHelper: LinearSnapHelper? = null

    /**
     * @suppress
     */
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
        rvParticipants!!.apply {
            this.layoutManager = layoutManager
            this.adapter = fastAdapter
            this.isFocusable = false
            this.addItemDecoration(HorizontalCenterItemDecoration())
        }

        snapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(rvParticipants)
        }

        rvParticipants!!.addItemDecoration(
            MenuProgressIndicator(
                requireContext(),
                snapHelper!!
            )
        )

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvParticipants!!.onTouchEvent(event) }

        return root!!
    }

    /**
     * @suppress
     */
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


