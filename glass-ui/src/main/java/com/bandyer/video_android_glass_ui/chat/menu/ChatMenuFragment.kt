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
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.bottom_navigation.BottomNavigationView
import com.bandyer.video_android_glass_ui.common.AvatarView
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.participants.ParticipantStateTextView
import com.bandyer.video_android_glass_ui.databinding.BandyerFragmentChatMenuBinding
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getChatThemeAttribute
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * ChatMenuFragment. A base class for the chat menu options.
 */
abstract class ChatMenuFragment : BaseFragment() {

    private var binding: BandyerFragmentChatMenuBinding? = null

    protected var itemAdapter: ItemAdapter<ChatMenuItem>? = null
    protected var fastAdapter: FastAdapter<ChatMenuItem>? = null

    protected var root: View? = null
    protected var avatar: AvatarView? = null
    protected var contactStateDot: ShapeableImageView? = null
    protected var contactStateText: ParticipantStateTextView? = null
    protected var name: MaterialTextView? = null
    protected var rvActions: RecyclerView? = null
    protected var bottomNavigation: BottomNavigationView? = null

    protected var snapHelper: LinearSnapHelper? = null

    /**
     * @suppress
     */
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
        bottomNavigation = binding!!.bandyerBottomActionBar

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
            MenuProgressIndicator(
                requireContext(),
                snapHelper!!
            )
        )
        rvActions!!.addItemDecoration(HorizontalCenterItemDecoration())

        // pass the root view's touch event to the recycler view
        root!!.setOnTouchListener { _, event -> rvActions!!.onTouchEvent(event) }

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
        rvActions = null
        name = null
        bottomNavigation = null
        avatar = null
        contactStateDot = null
        contactStateText = null
        snapHelper = null
    }

}