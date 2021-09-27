package com.bandyer.video_android_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.ContextThemeWrapper
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.BaseFragment
import com.bandyer.video_android_glass_ui.R
import com.bandyer.video_android_glass_ui.TouchEvent
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentMenuBinding
import com.bandyer.video_android_glass_ui.utils.TiltController
import com.bandyer.video_android_glass_ui.utils.extensions.ContextExtensions.getAttributeResourceId
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter

/**
 * BandyerGlassMenuFragment
 */
class MenuFragment : BaseFragment(), TiltController.TiltListener {

    private var _binding: BandyerGlassFragmentMenuBinding? = null
    override val binding: BandyerGlassFragmentMenuBinding get() = _binding!!

    private var snapHelper: LinearSnapHelper? = null

//    private val activity by lazy { requireActivity() as SmartGlassActivity }

    private var tiltController: TiltController? = null

    private var currentMenuItemIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tiltController = TiltController(requireContext(), this)
    }

    override fun onResume() {
        super.onResume()
        tiltController!!.requestAllSensors()
    }

    override fun onPause() {
        super.onPause()
        tiltController!!.releaseAllSensors()
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        activity.addNotificationListener(this)

        val themeResId = requireActivity().theme.getAttributeResourceId(R.attr.bandyer_menuStyle)
        _binding = BandyerGlassFragmentMenuBinding.inflate(
            inflater.cloneInContext(ContextThemeWrapper(requireContext(), themeResId)),
            container,
            false
        )

        with(binding.bandyerBottomNavigation) {
            setTapOnClickListener { onTap(currentMenuItemIndex) }
            setSwipeHorizontalOnClickListener { onSwipeForward(true) }
            setSwipeDownOnClickListener { onSwipeDown() }
        }

        // Init the recycler view
        val itemAdapter = ItemAdapter<BaandyeMenuItem>()
        val fastAdapter = FastAdapter.with(itemAdapter!!)
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        with(binding.bandyerMenu) {
            this.layoutManager = layoutManager
            adapter = fastAdapter
            isFocusable = false
            setHasFixedSize(true)
            addItemDecoration(HorizontalCenterItemDecoration())
        }

        fastAdapter!!.onClickListener = { _, _, _, position -> onTap(position) }


        snapHelper = LinearSnapHelper().apply {
            this.attachToRecyclerView(binding)
        }

        rvMenu!!.addItemDecoration(
            MenuProgressIndicator(
                requireContext(),
                snapHelper!!
            )
        )

        // add scroll listener
        binding.bandyerMenu.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                val foundView = snapHelper!!.findSnapView(layoutManager) ?: return
                currentMenuItemIndex = layoutManager!!.getPosition(foundView)
            }
        })

        itemAdapter!!.add(MenuItem("Attiva microfono", "Muta microfono"))
        itemAdapter!!.add(MenuItem("Attiva camera", "Muta camera"))
        itemAdapter!!.add(MenuItem("Volume"))
        itemAdapter!!.add(MenuItem("Zoom"))
        itemAdapter!!.add(MenuItem("Utenti"))
        itemAdapter!!.add(MenuItem("Chat"))


        return with(binding) {
            // pass the root view's touch event to the recycler view
            root.setOnTouchListener { _, event -> bandyerMenu.onTouchEvent(event) }
            root
        }
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
        fastAdapter = null
        snapHelper = null
//        activity.removeNotificationListener(this)
    }

    override fun onTouch(event: TouchEvent): Boolean =
        when (event.type) {
            TouchEvent.Type.TAP            -> onTap(currentMenuItemIndex)
            TouchEvent.Type.SWIPE_DOWN     -> onSwipeDown()
            TouchEvent.Type.SWIPE_FORWARD  -> onSwipeForward(event.source == TouchEvent.Source.KEY)
            TouchEvent.Type.SWIPE_BACKWARD -> onSwipeBackward(event.source == TouchEvent.Source.KEY)
            else                           -> super.onTouch(event)
        }

    private fun onTap(itemIndex: Int): Boolean = when (itemIndex) {
        0, 1 -> {
            val isActivated = itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActivated
            itemAdapter!!.getAdapterItem(currentMenuItemIndex).isActivated = !isActivated
            true
        }
        2 -> {
            findNavController().navigate(R.id.action_menuFragment_to_volumeFragment)
            true
        }
        3 -> {
            findNavController().navigate(R.id.action_menuFragment_to_zoomFragment)
            true
        }
        4 -> {
            findNavController().navigate(R.id.action_menuFragment_to_participantsFragment)
            true
        }
        5 -> {
            findNavController().navigate(R.id.action_menuFragment_to_smartglass_nav_graph_chat)
            true
        }
        else -> false
    }

    private fun onSwipeDown(): Boolean {
        findNavController().popBackStack()
        return true
    }

    private fun onSwipeForward(isKeyEvent: Boolean): Boolean {
        if (isKeyEvent)
            binding.bandyerMenu.horizontalSmoothScrollToNext(currentMenuItemIndex)
        return isKeyEvent
    }

    private fun onSwipeBackward(isKeyEvent: Boolean): Boolean {
        if (isKeyEvent)
            binding.bandyerMenu.horizontalSmoothScrollToPrevious(currentMenuItemIndex)
        return isKeyEvent
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerMenu.scrollBy((deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(), 0)

}