package com.bandyer.video_android_glass_ui.menu

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bandyer.video_android_glass_ui.*
import com.bandyer.video_android_glass_ui.common.item_decoration.HorizontalCenterItemDecoration
import com.bandyer.video_android_glass_ui.common.item_decoration.MenuProgressIndicator
import com.bandyer.video_android_glass_ui.databinding.BandyerGlassFragmentMenuBinding
import com.bandyer.video_android_glass_ui.utils.GlassDeviceUtils
import com.bandyer.video_android_glass_ui.call.CallAction
import com.bandyer.video_android_glass_ui.utils.TiltListener
import com.bandyer.video_android_glass_ui.utils.extensions.LifecycleOwnerExtensions.repeatOnStarted
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToNext
import com.bandyer.video_android_glass_ui.utils.extensions.horizontalSmoothScrollToPrevious
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.ItemAdapter
import kotlinx.coroutines.flow.collect

/**
 * BandyerGlassMenuFragment
 */
class MenuFragment : BaseFragment(), TiltListener {

    private var _binding: BandyerGlassFragmentMenuBinding? = null
    override val binding: BandyerGlassFragmentMenuBinding get() = _binding!!

    private var itemAdapter: ItemAdapter<MenuItem>? = null

    private var currentMenuItemIndex = 0

    private val navGraphViewModel: NavGraphViewModel by navGraphViewModels(R.id.smartglass_nav_graph) { NavGraphViewModelFactory }

    private val activityViewModel: GlassViewModel by activityViewModels { GlassViewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (activityViewModel.tiltEnabled) tiltListener = this
    }

    /**
     * @suppress
     */
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        // Add view binding
        _binding = BandyerGlassFragmentMenuBinding
            .inflate(inflater, container, false)
            .apply {
                if (GlassDeviceUtils.isRealWear)
                    bandyerBottomNavigation.setListenersForRealwear()

                // Init the RecyclerView
                with(bandyerMenu) {
                    itemAdapter = ItemAdapter()
                    val fastAdapter = FastAdapter.with(itemAdapter!!).apply {
                        onClickListener = { _, _, item, _ -> onTap(item.action); false }
                    }
                    val layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    val snapHelper = LinearSnapHelper().also { it.attachToRecyclerView(this) }

                    this.layoutManager = layoutManager
                    adapter = fastAdapter
                    isFocusable = false
                    setHasFixedSize(true)

                    addItemDecoration(HorizontalCenterItemDecoration())
                    addItemDecoration(MenuProgressIndicator(requireContext(), snapHelper))

                    addOnScrollListener(object : RecyclerView.OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            val foundView = snapHelper.findSnapView(layoutManager) ?: return
                            currentMenuItemIndex = layoutManager.getPosition(foundView)
                        }
                    })

                    // Forward the root view's touch event to the recycler view
                    root.setOnTouchListener { _, event -> this.onTouchEvent(event) }

                    repeatOnStarted {
                        navGraphViewModel.callState.collect { state ->
                            when (state) {
                                is Call.State.Disconnected -> requireActivity().finish()
                                else -> Unit
                            }
                        }
                    }
                }
            }

        CallAction
            .getActions(requireContext(), false, false, true, true, true)
            .forEach { itemAdapter!!.add(MenuItem(it)) }

        return binding.root
    }

    /**
     * @suppress
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        itemAdapter = null
    }

    override fun onDismiss() = Unit

    override fun onTap() = onTap(itemAdapter!!.getAdapterItem(currentMenuItemIndex).action)

    private fun onTap(action: CallAction) = when (action) {
        is CallAction.MICROPHONE -> {
            action.toggle(true)
            navGraphViewModel.disableMic(true)
            true
        }
        is CallAction.CAMERA -> {
            action.toggle(true)
            navGraphViewModel.disableCamera(true)
            true
        }
        is CallAction.VOLUME -> {
            findNavController().navigate(R.id.action_menuFragment_to_volumeFragment)
            true
        }
        is CallAction.ZOOM -> {
            findNavController().navigate(R.id.action_menuFragment_to_zoomFragment)
            true
        }
        is CallAction.PARTICIPANTS -> {
            findNavController().navigate(R.id.action_menuFragment_to_participantsFragment)
            true
        }
        is CallAction.CHAT -> {
            findNavController().navigate(R.id.action_menuFragment_to_smartglass_nav_graph_chat)
            true
        }
        else -> false
    }

    override fun onSwipeDown() = true.also { findNavController().popBackStack() }

    override fun onSwipeForward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.bandyerMenu.horizontalSmoothScrollToNext(currentMenuItemIndex)
    }

    override fun onSwipeBackward(isKeyEvent: Boolean) = isKeyEvent.also {
        if (it) binding.bandyerMenu.horizontalSmoothScrollToPrevious(currentMenuItemIndex)
    }

    override fun onTilt(deltaAzimuth: Float, deltaPitch: Float, deltaRoll: Float) =
        binding.bandyerMenu.scrollBy(
            (deltaAzimuth * resources.displayMetrics.densityDpi / 5).toInt(),
            0
        )
}