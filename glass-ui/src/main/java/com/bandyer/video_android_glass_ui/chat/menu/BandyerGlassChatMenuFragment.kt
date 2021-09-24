package com.bandyer.video_android_glass_ui.chat.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.BandyerGlassBaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.bottom_action_bar.BandyerBottomActionBarView
import com.bandyer.video_android_glass_ui.common.BandyerContactAvatarView
import com.bandyer.video_android_glass_ui.common.item_decoration.BandyerCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.BandyerMenuLineDecoration
import com.bandyer.video_android_glass_ui.contact.BandyerContactStateTextView
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentChatMenuBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getChatThemeAttribute
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * SmartGlassParticipantDetailsFragment. A base class for the participant details fragment.
 */
abstract class BandyerGlassChatMenuFragment : BandyerGlassBaseFragment() {

    private var binding: BandyerFragmentChatMenuBinding? = null

    protected var itemAdapter: ItemAdapter<BandyerChatMenuItem>? = null
    protected var fastAdapter: FastAdapter<BandyerChatMenuItem>? = null

    protected var root: View? = null
    protected var avatar: BandyerContactAvatarView? = null
    protected var contactStateDot: ShapeableImageView? = null
    protected var contactStateText: BandyerContactStateTextView? = null
    protected var name: MaterialTextView? = null
    protected var rvActions: RecyclerView? = null
    protected var bottomActionBar: BandyerBottomActionBarView? = null

    protected var snapHelper: LinearSnapHelper? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val style =
            requireContext().getChatThemeAttribute(R.styleable.BandyerSDKDesign_Theme_Glass_Chat_bandyer_chatMenuStyle)
        val wrapper = ContextThemeWrapper(requireActivity(), style)
        binding = BandyerFragmentChatMenuBinding.inflate(
            inflater.cloneInContext(wrapper),
            container,
            false
        )

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
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        rvActions!!.apply {
            this.layoutManager = layoutManager
            this.adapter = fastAdapter
            this.isFocusable = false
            this.setHasFixedSize(true)
        }

        snapHelper = LinearSnapHelper().apply {
            attachToRecyclerView(rvActions)
        }

        rvActions!!.addItemDecoration(
            BandyerMenuLineDecoration(
                requireContext(),
                snapHelper!!
            )
        )
        rvActions!!.addItemDecoration(BandyerCenterItemDecoration())

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
        snapHelper = null
    }

}